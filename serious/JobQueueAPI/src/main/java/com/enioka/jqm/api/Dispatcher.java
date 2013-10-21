/**
 * Copyright © 2013 enioka. All rights reserved
 * Authors: Pierre COPPEE (pierre.coppee@enioka.com)
 * Contributors : Marc-Antoine GOUILLART (marc-antoine.gouillart@enioka.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.enioka.jqm.api;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.enioka.jqm.hash.Cryptonite;
import com.enioka.jqm.jpamodel.Deliverable;
import com.enioka.jqm.jpamodel.DeploymentParameter;
import com.enioka.jqm.jpamodel.History;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobDefParameter;
import com.enioka.jqm.jpamodel.JobHistoryParameter;
import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.jpamodel.JobParameter;
import com.enioka.jqm.jpamodel.Message;
import com.enioka.jqm.jpamodel.Queue;
import com.enioka.jqm.tools.CreationTools;

/**
 *
 * @author pierre.coppee
 */
public class Dispatcher {

	public static EntityManagerFactory emf = Persistence.createEntityManagerFactory("jobqueue-api-pu");
	public static EntityManager em = emf.createEntityManager();

	//----------------------------- JOBDEFINITIONTOJOBDEF --------------------------------------

	private static JobDef jobDefinitionToJobDef(JobDefinition jd) {

		 JobDef job = em.createQuery("SELECT j FROM JobDef j WHERE j.applicationName = :name", JobDef.class)
		.setParameter("name", jd.getApplicationName())
		.getSingleResult();

		 return job;
	}

	private static com.enioka.jqm.api.JobDefinition jobDefToJobDefinition(JobDef jd) {

		com.enioka.jqm.api.JobDefinition job = new com.enioka.jqm.api.JobDefinition();
		 Map<String, String> h = new HashMap<String, String>();

		 if(jd.getParameters() != null) {
			 for (JobDefParameter i : jd.getParameters()) {

				 h.put(i.getKey(), i.getValue());
			 }
		 }

		 job.setParameters(h);
		 job.setApplicationName(jd.getApplicationName());
		 job.setSessionID(jd.getSessionID());
		 job.setApplication(jd.getApplication());
		 job.setModule(jd.getModule());
		 job.setOther1(jd.getOther1());
		 job.setOther2(jd.getOther2());
		 job.setOther3(jd.getOther3());

		 return job;
	}

	private static com.enioka.jqm.api.Queue getQueue(Queue queue) {

		com.enioka.jqm.api.Queue q = new com.enioka.jqm.api.Queue();

		q.setDescription(queue.getDescription());
		q.setId(queue.getId());
		q.setName(queue.getName());

		return q;
	}

    private static com.enioka.jqm.api.JobInstance getJobInstance(JobInstance job) {

		Map<String, String> parameters = new HashMap<String, String>();
		com.enioka.jqm.api.JobInstance j = new com.enioka.jqm.api.JobInstance();

		for (JobParameter i : job.getParameters()) {

			parameters.put(i.getKey(), i.getValue());
        }

		j.setId(job.getId());
		j.setJd(jobDefToJobDefinition(job.getJd()));
		j.setParameters(parameters);
		j.setParent(job.getParent().getId());
		j.setPosition(job.getPosition());
		j.setQueue(getQueue(job.getQueue()));
		j.setSessionID(job.getSessionID());
		j.setState(job.getState());
		j.setUser(job.getUser());

		return j;
	}

    public static List<JobParameter> overrideParameter(JobDef jdef, JobDefinition jdefinition) {

    	Map<String, String> m = jdefinition.getParameters();
    	List<JobParameter> res = new ArrayList<JobParameter>();

    	if (m.isEmpty()) {

    		if (jdef.getParameters() == null)
    			return res;

    		for (JobDefParameter i : jdef.getParameters()) {

    			res.add(CreationTools.createJobParameter(i.getKey(), i.getValue(), em));
            }

    		return res;
    	}
    	else {

    		for( Iterator<String> i = m.keySet().iterator(); i.hasNext();) {

    			String key = i.next();
    			String value = m.get(key);

    			res.add(CreationTools.createJobParameter(key, value, em));
    		}

    		return res;
    	}
    }
	// ----------------------------- ENQUEUE --------------------------------------

	public static int enQueue(JobDefinition jd) {

		// On a exactement la classe qui est en base
		System.out.println("DEBUT ENQUEUE");
		JobDef job = jobDefinitionToJobDef(jd); // Catch l'erreur si le nom de l'appli est mauvais

		Calendar enqueueDate = GregorianCalendar.getInstance(Locale.getDefault());

		History h = null;

		Integer p = em.createQuery("SELECT MAX (j.position) FROM JobInstance j " +
				"WHERE j.jd.queue.name = :queue", Integer.class).setParameter("queue", (job.getQueue().getName())).getSingleResult();

		System.out.println("POSITION: " + p);

		em.getTransaction().begin();

		JobInstance ji = CreationTools.createJobInstance(job, overrideParameter(job, jd), jd.getUser(), 42, "SUBMITTED", (p == null) ? 1 : p + 1, job.queue, em);

		//CreationTools.em.createQuery("UPDATE JobParameter jp SET jp.jobInstance = :j WHERE").executeUpdate();

		// Update status in the history table
		//System.exit(0);
		Query q = em.createQuery("SELECT h FROM History h WHERE h.jobInstance.id = :j", History.class).setParameter("j", ji.getId());

		if (!q.equals(null)) {

			Message m = null;
			ArrayList<JobHistoryParameter> jhp = new ArrayList<JobHistoryParameter>();

			for (JobParameter j : ji.getParameters()) {

				JobHistoryParameter jp = new JobHistoryParameter();

				jp.setKey(j.getKey());
				jp.setValue(j.getValue());

				em.persist(jp);

				jhp.add(jp);
			}

			ArrayList<Message> msgs = new ArrayList<Message>();

			h = CreationTools.createhistory(1, (Calendar) null, "History of the Job --> ID = " + (ji.getId()),
					msgs, ji, enqueueDate, (Calendar) null, (Calendar) null, jhp, em);

			m = CreationTools.createMessage("Status updated: SUBMITTED", h, em);
			msgs.add(m);

			em.getTransaction().commit();

		}
		return ji.getId();
	}

	//----------------------------- GETJOB --------------------------------------

	public static com.enioka.jqm.api.JobInstance getJob(int idJob) {

		return getJobInstance(em.createQuery(
				"SELECT j FROM JobInstance j WHERE j.id = :job",
				JobInstance.class)
				.setParameter("job", idJob)
				.getSingleResult());
	}

	//----------------------------- DELJOBINQUEUE --------------------------------------

	public static void delJobInQueue(int idJob) {

		EntityTransaction transac = em.getTransaction();
		transac.begin();

		Query q = em.createQuery("DELETE FROM JobInstance j WHERE j.id = :idJob").setParameter("idJob", idJob);
		int res = q.executeUpdate();

		if (res != 1)
			System.err.println("delJobInQueueError: Job ID or Queue ID doesn't exists.");

		transac.commit();

	}

	//----------------------------- CANCELJOBINQUEUE --------------------------------------

	public static void cancelJobInQueue(int idJob) {

		@SuppressWarnings("unused")
        History h = em.createQuery("SELECT h FROM History h WHERE h.jobInstance.id = :j", History.class).setParameter("j", idJob).getSingleResult();

		EntityTransaction transac = em.getTransaction();
		transac.begin();

		Query q = em.createQuery("UPDATE JobInstance j SET j.state = 'CANCELLED' WHERE j.id = :idJob").setParameter("idJob", idJob);
		int res = q.executeUpdate();

		em
        .createQuery(
                "UPDATE Message m SET m.textMessage = :msg WHERE m.history.id = "
                        + "(SELECT h.id FROM History h WHERE h.jobInstance.id = :j)")
        .setParameter("j", idJob)
        .setParameter("msg", "Status updated: CANCELLED")
        .executeUpdate();

		if (res != 1)
			System.err.println("CancelJobInQueueError: Job ID or Queue ID doesn't exists.");

		transac.commit();

	}

	//----------------------------- STOPJOB --------------------------------------

	public static void stopJob(int idJob) {

	}

	//----------------------------- KILLJOB --------------------------------------

	public static void killJob(int idJob) {

	}

	public static void restartCrashedJob(int idJob) {

	}

	public static int restartJob(int idJob) {

		return 0;
	}

	//----------------------------- SETPOSITION --------------------------------------

	public static void setPosition(int idJob, int position) {

		if (position < 1)
			position = 1;

		List<JobInstance> q = em.createQuery("SELECT j FROM JobInstance j WHERE j.state = :s " +
				"ORDER BY j.position", JobInstance.class).setParameter("s", "SUBMITTED").getResultList();

		EntityTransaction transac = em.getTransaction();
		transac.begin();

		Query query = em.createQuery("UPDATE JobInstance j SET j.position = :pos WHERE " +
				"j.id = (SELECT ji.id FROM JobInstance ji WHERE ji.id = :idJob)").setParameter("idJob", idJob).setParameter("pos", position);

		@SuppressWarnings("unused")
        int result = query.executeUpdate();

		for (int i = 0; i < q.size(); i++)
		{
			if (q.get(i).getId() == idJob)
				continue;
			else if (i + 1 == position)
			{
				Query queryEg = em.createQuery("UPDATE JobInstance j SET j.position = :i WHERE j.id = :job").setParameter("i",
						position + 2).setParameter("job", q.get(i).getId());
				@SuppressWarnings("unused")
                int res = queryEg.executeUpdate();
				i++;
			}
			else
			{
				Query qq = em.createQuery("UPDATE JobInstance j SET j.position = :i WHERE j.id = :job").setParameter("i",
						i + 1).setParameter("job", q.get(i).getId());
				@SuppressWarnings("unused")
                int res = qq.executeUpdate();

			}
		}

		transac.commit();
	}

	//----------------------------- GETDELIVERABLES --------------------------------------

	public static List<InputStream> getDeliverables(int idJob) throws IOException, NoSuchAlgorithmException {

		URL url = null;
		File file = null;
		ArrayList<InputStream> streams = new ArrayList<InputStream>();
		List<Deliverable> tmp = new ArrayList<Deliverable>();
		Logger jqmlogger = Logger.getLogger(Dispatcher.class);

		try {

			tmp = em.createQuery(
					"SELECT d FROM Deliverable d WHERE d.jobId = :idJob",
					Deliverable.class)
					.setParameter("idJob", idJob)
					.getResultList();

			System.out.println("idJob: " + idJob);
			System.out.println("sizeTMP: " + tmp.size());

			JobInstance job = em.createQuery(
					"SELECT j FROM JobInstance j WHERE j.id = :job",
					JobInstance.class)
					.setParameter("job", idJob)
					.getSingleResult();

			DeploymentParameter dp = em.createQuery(
					"SELECT dp FROM DeploymentParameter dp WHERE dp.queue.id = :q", DeploymentParameter.class)
					.setParameter("q", job.getJd().getQueue().getId())
					.getSingleResult();

			for (int i = 0; i < tmp.size(); i++) {

				url = new URL(
						"http://" +
								dp.getNode().getListeningInterface() +
								":" +
								dp.getNode().getPort() +
								"/getfile?file=" +
								tmp.get(i).getFilePath() + tmp.get(i).getFileName());

				if (tmp.get(i).getHashPath().equals(Cryptonite.sha1(tmp.get(i).getFilePath() + tmp.get(i).getFileName()))) {
					// mettre en base le repertoire de dl
					System.out.println("dlRepo: " + dp.getNode().getDlRepo() + tmp.get(i).getFileFamily() + "/" + job.getId() + "/");
					File dlRepo = new File(dp.getNode().getDlRepo() + tmp.get(i).getFileFamily() + "/" + job.getId() + "/");
					dlRepo.mkdirs();
					file = new File(dp.getNode().getDlRepo() + tmp.get(i).getFileFamily() + "/" + job.getId() + "/" + tmp.get(i).getFileName());


					FileUtils.copyURLToFile(url, file);
					streams.add(new FileInputStream(file));
				}
			}
		} catch (FileNotFoundException e) {

			jqmlogger.info(e);
		} catch (Exception e) {

			jqmlogger.info("No deliverable available", e);
		}

		return streams;
	}

	//----------------------------- GETALLDELIVERABLES --------------------------------------

	public static List<Deliverable> getAllDeliverables(int idJob) {

		ArrayList<Deliverable> deliverables = new ArrayList<Deliverable>();

		deliverables = (ArrayList<Deliverable>) em.createQuery(
				"SELECT d FROM Deliverable d WHERE d.jobInstance = :idJob",
				Deliverable.class)
				.setParameter("idJob", idJob)
				.getResultList();

		return deliverables;
	}

	//----------------------------- GETONEDELIVERABLE --------------------------------------

	public static InputStream getOneDeliverable(com.enioka.jqm.api.Deliverable d) throws NoSuchAlgorithmException, IOException {

		URL url = null;
		File file = null;

		Deliverable deliverable = em.createQuery("SELECT d FROM Deliverable d WHERE d.filePath = :f AND d.fileName = :fn", Deliverable.class)
				.setParameter("f", d.getFilePath())
				.setParameter("fn", d.getFileName())
				.getSingleResult();

		JobInstance job = em.createQuery(
				"SELECT j FROM JobInstance j WHERE j.id = :job",
				JobInstance.class)
				.setParameter("job", deliverable.getJobId())
				.getSingleResult();

		DeploymentParameter dp = em.createQuery(
				"SELECT dp FROM DeploymentParameter dp WHERE dp.queue.id = :q", DeploymentParameter.class)
				.setParameter("q", job.getJd().getQueue().getId())
				.getSingleResult();

		url = new URL(
				"http://" +
						dp.getNode().getListeningInterface() +
						":" + dp.getNode().getPort() +
						"/getfile?file=" +
						deliverable.getFilePath());

		if (deliverable.getHashPath().equals(Cryptonite.sha1(deliverable.getFilePath()))) {

			FileUtils.copyURLToFile(url, file = new File("./testprojects/JobGenADeliverable/deliverable" + job.getId()));
		}
		return new FileInputStream(file);
	}

	//----------------------------- GETUSERDELIVERABLES --------------------------------------

	public static List<Deliverable> getUserDeliverables(String user) {

		ArrayList<Deliverable> d = new ArrayList<Deliverable>();
		ArrayList<Deliverable> res = new ArrayList<Deliverable>();

		ArrayList<JobInstance> j = (ArrayList<JobInstance>) em.createQuery(
				"SELECT j FROM JobInstance j WHERE j.user = :u",
				JobInstance.class)
				.setParameter("u", user)
				.getResultList();

		for (int i = 0; i < j.size(); i++) {

			d = (ArrayList<Deliverable>) em.createQuery(
					"SELECT d FROM Deliverable d WHERE d.jobId = :idJob",
					Deliverable.class)
					.setParameter("idJob", j.get(i).getId())
					.getResultList();

			res.addAll(d);
		}
		return res;
	}

	//----------------------------- GETMSG --------------------------------------

	public static List<String> getMsg(int idJob) { // -------------TODO------------

		ArrayList<String> msgs = new ArrayList<String>();

		return msgs;
	}

	//----------------------------- GETUSERJOBS --------------------------------------

	public static List<JobInstance> getUserJobs(String user) {

		ArrayList<JobInstance> jobs = (ArrayList<JobInstance>) em.createQuery("SELECT j FROM JobInstance j WHERE j.user = :u", JobInstance.class).setParameter("u", user).getResultList();

		return jobs;
	}

	//----------------------------- GETJOBS --------------------------------------

	public static List<com.enioka.jqm.api.JobInstance> getJobs() {

		ArrayList<com.enioka.jqm.api.JobInstance> res = new ArrayList<com.enioka.jqm.api.JobInstance>();
		ArrayList<JobInstance> jobs = (ArrayList<JobInstance>) em.createQuery("SELECT j FROM JobInstance j", JobInstance.class).getResultList();

		for (JobInstance j : jobs) {
	        com.enioka.jqm.api.JobInstance tmp = new com.enioka.jqm.api.JobInstance();

	        tmp.setId(j.getId());
	        tmp.setJd(jobDefToJobDefinition(j.getJd()));
	        if (j.getParent() != null)
	        	tmp.setParent(j.getParent().getId());
	        else
	        	tmp.setParent(null);

	        res.add(tmp);
        }

		return res;
	}

	//----------------------------- GETQUEUES --------------------------------------

	public static List<com.enioka.jqm.api.Queue> getQueues() {

		List<com.enioka.jqm.api.Queue> res = new ArrayList<com.enioka.jqm.api.Queue>();
		ArrayList<Queue> queues = (ArrayList<Queue>) em.createQuery("SELECT j FROM Queue j", Queue.class).getResultList();

		for (Queue queue : queues) {

			com.enioka.jqm.api.Queue q = new com.enioka.jqm.api.Queue();

			q = getQueue(queue);

			res.add(q);
        }

		return res;
	}

	//----------------------------- CHANGEQUEUE --------------------------------------

	public static void changeQueue(int idJob, int idQueue) {

		EntityTransaction transac = em.getTransaction();
		transac.begin();

		Queue q = em.createQuery("SELECT Queue FROM Queue queue " +
				"WHERE queue.id = :q", Queue.class).setParameter("q", idQueue).getSingleResult();


		Query query = em.createQuery("UPDATE JobInstance j SET j.queue = :q WHERE j.id = :jd").setParameter("q", q).setParameter("jd", idJob);
		int result = query.executeUpdate();

		if (result != 1)
			System.err.println("changeQueueError: Job ID or Queue ID doesn't exists.");

		transac.commit();
	}

	//----------------------------- CHANGEQUEUE --------------------------------------

	public static void changeQueue(int idJob, Queue queue) {

		EntityTransaction transac = em.getTransaction();
		transac.begin();

		Query query = em.createQuery("UPDATE JobInstance j SET j.queue = :q WHERE j.id = :jd").setParameter("q", queue).setParameter("jd", idJob);
		int result = query.executeUpdate();

		if (result != 1)
			System.err.println("changeQueueError: Job ID or Queue ID doesn't exists.");

		transac.commit();
	}
}
