/**
 * Copyright © 2013 enioka. All rights reserved
 * Authors: Marc-Antoine GOUILLART (marc-antoine.gouillart@enioka.com)
 *          Pierre COPPEE (pierre.coppee@enioka.com)
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

package com.enioka.jqm.tools;

import java.util.ArrayList;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.junit.Assert;
import org.junit.Test;

import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.jpamodel.GlobalParameter;
import com.enioka.jqm.jpamodel.History;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobDefParameter;
import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.jpamodel.Message;
import com.enioka.jqm.jpamodel.State;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

public class MultiNodeTest extends JqmBaseTest
{
    @Test
    public void testOneQueueTwoNodes() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testOneQueueTwoNodes");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        @SuppressWarnings("unused")
        JobDef jd11 = CreationTools.createJobDef(null, true, "App", jdargs, null, "jqm-tests/jqm-test-datetimesendmsg/target/test.jar",
                TestHelpers.qVip, 42, "AppliNode1-1", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        JobRequest j11 = new JobRequest("AppliNode1-1", "MAG");

        JqmClientFactory.getClient().enqueue(j11);
        JqmClientFactory.getClient().enqueue(j11);
        JqmClientFactory.getClient().enqueue(j11);
        JqmClientFactory.getClient().enqueue(j11);
        JqmClientFactory.getClient().enqueue(j11);
        JqmClientFactory.getClient().enqueue(j11);
        JqmClientFactory.getClient().enqueue(j11);
        JqmClientFactory.getClient().enqueue(j11);
        JqmClientFactory.getClient().enqueue(j11);

        JqmEngine engine1 = new JqmEngine();
        JqmEngine engine2 = new JqmEngine();
        engine1.start("localhost");
        engine2.start("localhost4");

        int i = 0;
        while (i < 3)
        {
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j11);

            Thread.sleep(1000);
            i++;
        }
        TestHelpers.waitFor(45, 30000, em);

        Assert.assertEquals(45, em.createQuery("SELECT j FROM History j ORDER BY j.executionDate ASC", History.class).getResultList()
                .size());
        Assert.assertEquals(180, em.createQuery("SELECT j FROM Message j", Message.class).getResultList().size());

        engine1.stop();
        engine2.stop();

        TypedQuery<History> query = em.createQuery("SELECT j FROM History j ORDER BY j.executionDate ASC", History.class);
        ArrayList<History> res = (ArrayList<History>) query.getResultList();

        for (History jobInstance : res)
        {
            Assert.assertEquals(State.ENDED, jobInstance.getState());
        }
    }

    @Test
    public void testOneQueueThreeNodes() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testOneQueueThreeNodes");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        @SuppressWarnings("unused")
        JobDef jd11 = CreationTools.createJobDef(null, true, "App", jdargs, null, "jqm-tests/jqm-test-datetimesendmsg/target/test.jar",
                TestHelpers.qVip, 42, "AppliNode1-1", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        JobRequest j11 = new JobRequest("AppliNode1-1", "MAG");

        JqmClientFactory.getClient().enqueue(j11);
        JqmClientFactory.getClient().enqueue(j11);
        JqmClientFactory.getClient().enqueue(j11);
        JqmClientFactory.getClient().enqueue(j11);
        JqmClientFactory.getClient().enqueue(j11);
        JqmClientFactory.getClient().enqueue(j11);
        JqmClientFactory.getClient().enqueue(j11);
        JqmClientFactory.getClient().enqueue(j11);
        JqmClientFactory.getClient().enqueue(j11);

        JqmEngine engine1 = new JqmEngine();
        JqmEngine engine2 = new JqmEngine();
        JqmEngine engine3 = new JqmEngine();
        engine1.start("localhost");
        engine2.start("localhost4");
        engine3.start("localhost5");

        int i = 0;
        while (i < 3)
        {
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j11);

            Thread.sleep(1000);
            i++;
        }

        TestHelpers.waitFor(45, 10000, em);
        engine1.stop();
        engine2.stop();
        engine3.stop();

        TypedQuery<History> query = em.createQuery("SELECT j FROM History j ORDER BY j.executionDate ASC", History.class);
        ArrayList<History> res = (ArrayList<History>) query.getResultList();

        for (History jobInstance : res)
        {
            Assert.assertEquals(State.ENDED, jobInstance.getState());
        }

        Assert.assertEquals((Long) 180L, em.createQuery("SELECT COUNT(m) from Message m", Long.class).getSingleResult());
    }

    @Test
    public void testTwoNodesTwoQueues() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testTwoNodesTwoQueues");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        @SuppressWarnings("unused")
        JobDef jd11 = CreationTools.createJobDef(null, true, "App", jdargs, null, "jqm-tests/jqm-test-datetimesendmsg/target/test.jar",
                TestHelpers.qVip, 42, "AppliNode1-1", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        @SuppressWarnings("unused")
        JobDef jd21 = CreationTools.createJobDef(null, true, "App", jdargs, null, "jqm-tests/jqm-test-datetimesendmsg/target/test.jar",
                TestHelpers.qVip2, 42, "AppliNode2-1", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        JobRequest j11 = new JobRequest("AppliNode1-1", "MAG");
        JobRequest j21 = new JobRequest("AppliNode2-1", "MAG");

        JqmClientFactory.getClient().enqueue(j11);
        JqmClientFactory.getClient().enqueue(j11);
        JqmClientFactory.getClient().enqueue(j11);
        JqmClientFactory.getClient().enqueue(j21);
        JqmClientFactory.getClient().enqueue(j21);
        JqmClientFactory.getClient().enqueue(j21);

        JqmEngine engine1 = new JqmEngine();
        JqmEngine engine2 = new JqmEngine();
        engine1.start("localhost");
        engine2.start("localhost2");

        int i = 0;
        while (i < 3)
        {
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j21);
            JqmClientFactory.getClient().enqueue(j21);
            JqmClientFactory.getClient().enqueue(j21);
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j21);
            JqmClientFactory.getClient().enqueue(j21);
            JqmClientFactory.getClient().enqueue(j21);

            Thread.sleep(1000);
            i++;
        }

        TestHelpers.waitFor(42, 10000, em);
        engine1.stop();
        engine2.stop();

        TypedQuery<History> query = em.createQuery("SELECT j FROM History j ORDER BY j.executionDate ASC", History.class);
        ArrayList<History> res = (ArrayList<History>) query.getResultList();
        Assert.assertEquals(42, res.size());
        Assert.assertEquals(168, em.createQuery("SELECT j FROM Message j", Message.class).getResultList().size());
        for (History jobInstance : res)
        {
            Assert.assertEquals(State.ENDED, jobInstance.getState());
        }
        em.close();
    }

    @Test
    public void testThreeNodesThreeQueues() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testThreeNodesThreeQueues");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        @SuppressWarnings("unused")
        JobDef jd11 = CreationTools.createJobDef(null, true, "App", jdargs, null, "jqm-tests/jqm-test-datetimesendmsg/target/test.jar",
                TestHelpers.qVip, 42, "AppliNode1-1", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        @SuppressWarnings("unused")
        JobDef jd12 = CreationTools.createJobDef(null, true, "App", jdargs, null, "jqm-tests/jqm-test-datetimesendmsg/target/test.jar",
                TestHelpers.qNormal, 42, "AppliNode1-2", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        @SuppressWarnings("unused")
        JobDef jd13 = CreationTools.createJobDef(null, true, "App", jdargs, null, "jqm-tests/jqm-test-datetimesendmsg/target/test.jar",
                TestHelpers.qSlow, 42, "AppliNode1-3", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        @SuppressWarnings("unused")
        JobDef jd21 = CreationTools.createJobDef(null, true, "App", jdargs, null, "jqm-tests/jqm-test-datetimesendmsg/target/test.jar",
                TestHelpers.qVip2, 42, "AppliNode2-1", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        @SuppressWarnings("unused")
        JobDef jd22 = CreationTools.createJobDef(null, true, "App", jdargs, null, "jqm-tests/jqm-test-datetimesendmsg/target/test.jar",
                TestHelpers.qNormal2, 42, "AppliNode2-2", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        @SuppressWarnings("unused")
        JobDef jd23 = CreationTools.createJobDef(null, true, "App", jdargs, null, "jqm-tests/jqm-test-datetimesendmsg/target/test.jar",
                TestHelpers.qSlow2, 42, "AppliNode2-3", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        @SuppressWarnings("unused")
        JobDef jd31 = CreationTools.createJobDef(null, true, "App", jdargs, null, "jqm-tests/jqm-test-datetimesendmsg/target/test.jar",
                TestHelpers.qVip3, 42, "AppliNode3-1", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        @SuppressWarnings("unused")
        JobDef jd32 = CreationTools.createJobDef(null, true, "App", jdargs, null, "jqm-tests/jqm-test-datetimesendmsg/target/test.jar",
                TestHelpers.qNormal3, 42, "AppliNode3-2", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        @SuppressWarnings("unused")
        JobDef jd33 = CreationTools.createJobDef(null, true, "App", jdargs, null, "jqm-tests/jqm-test-datetimesendmsg/target/test.jar",
                TestHelpers.qSlow3, 42, "AppliNode3-3", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        JobRequest j11 = new JobRequest("AppliNode1-1", "MAG");
        JobRequest j12 = new JobRequest("AppliNode1-2", "MAG");
        JobRequest j13 = new JobRequest("AppliNode1-3", "MAG");

        JobRequest j21 = new JobRequest("AppliNode2-1", "MAG");
        JobRequest j22 = new JobRequest("AppliNode2-2", "MAG");
        JobRequest j23 = new JobRequest("AppliNode2-3", "MAG");

        JobRequest j31 = new JobRequest("AppliNode3-1", "MAG");
        JobRequest j32 = new JobRequest("AppliNode3-2", "MAG");
        JobRequest j33 = new JobRequest("AppliNode3-3", "MAG");

        JqmClientFactory.getClient().enqueue(j11);
        JqmClientFactory.getClient().enqueue(j11);
        JqmClientFactory.getClient().enqueue(j11);
        JqmClientFactory.getClient().enqueue(j12);
        JqmClientFactory.getClient().enqueue(j12);
        JqmClientFactory.getClient().enqueue(j12);
        JqmClientFactory.getClient().enqueue(j13);
        JqmClientFactory.getClient().enqueue(j13);
        JqmClientFactory.getClient().enqueue(j13);

        JqmClientFactory.getClient().enqueue(j21);
        JqmClientFactory.getClient().enqueue(j21);
        JqmClientFactory.getClient().enqueue(j21);
        JqmClientFactory.getClient().enqueue(j22);
        JqmClientFactory.getClient().enqueue(j22);
        JqmClientFactory.getClient().enqueue(j22);
        JqmClientFactory.getClient().enqueue(j23);
        JqmClientFactory.getClient().enqueue(j23);
        JqmClientFactory.getClient().enqueue(j23);

        JqmClientFactory.getClient().enqueue(j31);
        JqmClientFactory.getClient().enqueue(j31);
        JqmClientFactory.getClient().enqueue(j31);
        JqmClientFactory.getClient().enqueue(j32);
        JqmClientFactory.getClient().enqueue(j32);
        JqmClientFactory.getClient().enqueue(j32);
        JqmClientFactory.getClient().enqueue(j33);
        JqmClientFactory.getClient().enqueue(j33);
        JqmClientFactory.getClient().enqueue(j33);

        JqmEngine engine1 = new JqmEngine();
        JqmEngine engine2 = new JqmEngine();
        JqmEngine engine3 = new JqmEngine();
        engine1.start("localhost");
        engine2.start("localhost2");
        engine3.start("localhost3");

        int i = 0;
        while (i < 3)
        {
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j12);
            JqmClientFactory.getClient().enqueue(j12);
            JqmClientFactory.getClient().enqueue(j12);
            JqmClientFactory.getClient().enqueue(j13);
            JqmClientFactory.getClient().enqueue(j13);
            JqmClientFactory.getClient().enqueue(j13);

            JqmClientFactory.getClient().enqueue(j21);
            JqmClientFactory.getClient().enqueue(j21);
            JqmClientFactory.getClient().enqueue(j21);
            JqmClientFactory.getClient().enqueue(j22);
            JqmClientFactory.getClient().enqueue(j22);
            JqmClientFactory.getClient().enqueue(j22);
            JqmClientFactory.getClient().enqueue(j23);
            JqmClientFactory.getClient().enqueue(j23);
            JqmClientFactory.getClient().enqueue(j23);

            JqmClientFactory.getClient().enqueue(j31);
            JqmClientFactory.getClient().enqueue(j31);
            JqmClientFactory.getClient().enqueue(j31);
            JqmClientFactory.getClient().enqueue(j32);
            JqmClientFactory.getClient().enqueue(j32);
            JqmClientFactory.getClient().enqueue(j32);
            JqmClientFactory.getClient().enqueue(j33);
            JqmClientFactory.getClient().enqueue(j33);
            JqmClientFactory.getClient().enqueue(j33);

            Thread.sleep(1000);
            i++;
        }

        TestHelpers.waitFor(108, 30000, em);
        engine1.stop();
        engine2.stop();
        engine3.stop();

        TypedQuery<History> query = em.createQuery("SELECT j FROM History j ORDER BY j.executionDate ASC", History.class);
        ArrayList<History> res = (ArrayList<History>) query.getResultList();

        for (History jobInstance : res)
        {
            Assert.assertEquals(State.ENDED, jobInstance.getState());
        }

        Assert.assertEquals((Long) 432L, em.createQuery("SELECT COUNT(m) from Message m", Long.class).getSingleResult());
    }

    @Test
    public void testThreeNodesThreeQueuesLock() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testThreeNodesThreeQueuesLock");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);
        ArrayList<History> job = new ArrayList<History>();

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        @SuppressWarnings("unused")
        JobDef jd11 = CreationTools.createJobDef(null, true, "App", jdargs, null, "jqm-tests/jqm-test-datetimesendmsg/target/test.jar",
                TestHelpers.qVip, 42, "AppliNode1-1", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        @SuppressWarnings("unused")
        JobDef jd12 = CreationTools.createJobDef(null, true, "App", jdargs, null, "jqm-tests/jqm-test-datetimesendmsg/target/test.jar",
                TestHelpers.qNormal, 42, "AppliNode1-2", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        @SuppressWarnings("unused")
        JobDef jd13 = CreationTools.createJobDef(null, true, "App", jdargs, null, "jqm-tests/jqm-test-datetimesendmsg/target/test.jar",
                TestHelpers.qSlow, 42, "AppliNode1-3", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        @SuppressWarnings("unused")
        JobDef jd21 = CreationTools.createJobDef(null, true, "App", jdargs, null, "jqm-tests/jqm-test-datetimesendmsg/target/test.jar",
                TestHelpers.qVip2, 42, "AppliNode2-1", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        @SuppressWarnings("unused")
        JobDef jd22 = CreationTools.createJobDef(null, true, "App", jdargs, null, "jqm-tests/jqm-test-datetimesendmsg/target/test.jar",
                TestHelpers.qNormal2, 42, "AppliNode2-2", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        @SuppressWarnings("unused")
        JobDef jd23 = CreationTools.createJobDef(null, true, "App", jdargs, null, "jqm-tests/jqm-test-datetimesendmsg/target/test.jar",
                TestHelpers.qSlow2, 42, "AppliNode2-3", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        @SuppressWarnings("unused")
        JobDef jd31 = CreationTools.createJobDef(null, true, "App", jdargs, null, "jqm-tests/jqm-test-datetimesendmsg/target/test.jar",
                TestHelpers.qVip3, 42, "AppliNode3-1", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        @SuppressWarnings("unused")
        JobDef jd32 = CreationTools.createJobDef(null, true, "App", jdargs, null, "jqm-tests/jqm-test-datetimesendmsg/target/test.jar",
                TestHelpers.qNormal3, 42, "AppliNode3-2", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        @SuppressWarnings("unused")
        JobDef jd33 = CreationTools.createJobDef(null, true, "App", jdargs, null, "jqm-tests/jqm-test-datetimesendmsg/target/test.jar",
                TestHelpers.qSlow3, 42, "AppliNode3-3", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        JobRequest j11 = new JobRequest("AppliNode1-1", "MAG");
        JobRequest j12 = new JobRequest("AppliNode1-2", "MAG");
        JobRequest j13 = new JobRequest("AppliNode1-3", "MAG");

        JobRequest j21 = new JobRequest("AppliNode2-1", "MAG");
        JobRequest j22 = new JobRequest("AppliNode2-2", "MAG");
        JobRequest j23 = new JobRequest("AppliNode2-3", "MAG");

        JobRequest j31 = new JobRequest("AppliNode3-1", "MAG");
        JobRequest j32 = new JobRequest("AppliNode3-2", "MAG");
        JobRequest j33 = new JobRequest("AppliNode3-3", "MAG");

        JqmClientFactory.getClient().enqueue(j11);
        JqmClientFactory.getClient().enqueue(j11);
        JqmClientFactory.getClient().enqueue(j11);
        JqmClientFactory.getClient().enqueue(j12);
        JqmClientFactory.getClient().enqueue(j12);
        JqmClientFactory.getClient().enqueue(j12);
        JqmClientFactory.getClient().enqueue(j13);
        JqmClientFactory.getClient().enqueue(j13);
        JqmClientFactory.getClient().enqueue(j13);

        JqmClientFactory.getClient().enqueue(j21);
        JqmClientFactory.getClient().enqueue(j21);
        JqmClientFactory.getClient().enqueue(j21);
        JqmClientFactory.getClient().enqueue(j22);
        JqmClientFactory.getClient().enqueue(j22);
        JqmClientFactory.getClient().enqueue(j22);
        JqmClientFactory.getClient().enqueue(j23);
        JqmClientFactory.getClient().enqueue(j23);
        JqmClientFactory.getClient().enqueue(j23);

        JqmClientFactory.getClient().enqueue(j31);
        JqmClientFactory.getClient().enqueue(j31);
        JqmClientFactory.getClient().enqueue(j31);
        JqmClientFactory.getClient().enqueue(j32);
        JqmClientFactory.getClient().enqueue(j32);
        JqmClientFactory.getClient().enqueue(j32);
        JqmClientFactory.getClient().enqueue(j33);
        JqmClientFactory.getClient().enqueue(j33);
        JqmClientFactory.getClient().enqueue(j33);

        JqmEngine engine1 = new JqmEngine();
        JqmEngine engine2 = new JqmEngine();
        JqmEngine engine3 = new JqmEngine();
        engine1.start("localhost");
        engine2.start("localhost2");
        engine3.start("localhost3");

        int i = 0;
        while (i < 3)
        {
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j12);
            JqmClientFactory.getClient().enqueue(j12);
            JqmClientFactory.getClient().enqueue(j12);
            JqmClientFactory.getClient().enqueue(j13);
            JqmClientFactory.getClient().enqueue(j13);
            JqmClientFactory.getClient().enqueue(j13);

            JqmClientFactory.getClient().enqueue(j21);
            JqmClientFactory.getClient().enqueue(j21);
            JqmClientFactory.getClient().enqueue(j21);
            JqmClientFactory.getClient().enqueue(j22);
            JqmClientFactory.getClient().enqueue(j22);
            JqmClientFactory.getClient().enqueue(j22);
            JqmClientFactory.getClient().enqueue(j23);
            JqmClientFactory.getClient().enqueue(j23);
            JqmClientFactory.getClient().enqueue(j23);

            JqmClientFactory.getClient().enqueue(j31);
            JqmClientFactory.getClient().enqueue(j31);
            JqmClientFactory.getClient().enqueue(j31);
            JqmClientFactory.getClient().enqueue(j32);
            JqmClientFactory.getClient().enqueue(j32);
            JqmClientFactory.getClient().enqueue(j32);
            JqmClientFactory.getClient().enqueue(j33);
            JqmClientFactory.getClient().enqueue(j33);
            JqmClientFactory.getClient().enqueue(j33);

            Thread.sleep(1000);
            i++;
        }

        TestHelpers.waitFor(135, 30000, em);
        engine1.stop();
        engine2.stop();
        engine3.stop();

        TypedQuery<History> query = em.createQuery("SELECT j FROM History j WHERE j.status = :ss", History.class);

        query.setParameter("ss", State.ENDED);
        job = (ArrayList<History>) query.getResultList();

        // 135
        ArrayList<Message> msgs = (ArrayList<Message>) em.createQuery("SELECT m FROM Message m WHERE m.textMessage = :m", Message.class)
                .setParameter("m", "DateTime will be printed").getResultList();

        Assert.assertEquals(job.size(), msgs.size());
    }

    @Test
    public void testStopNicely() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testStopNicely");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        em.getTransaction().begin();
        GlobalParameter gp = em.createQuery("SELECT n from GlobalParameter n WHERE n.key = 'internalPollingPeriodMs'",
                GlobalParameter.class).getSingleResult();
        gp.setValue("10");
        em.getTransaction().commit();

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        CreationTools.createJobDef(null, true, "App", jdargs, null, "jqm-tests/jqm-test-datetimesendmsg/target/test.jar", TestHelpers.qVip,
                42, "AppliNode1-1", null, "Franquin", "ModuleMachin", "other", "other", false, em);
        CreationTools.createJobDef(null, true, "App", jdargs, null, "jqm-tests/jqm-test-datetimesendmsg/target/test.jar", TestHelpers.qVip,
                42, "AppliNode1-2", null, "Franquin", "ModuleMachin", "other", "other", false, em);
        CreationTools.createJobDef(null, true, "App", jdargs, null, "jqm-tests/jqm-test-datetimesendmsg/target/test.jar",
                TestHelpers.qNormal, 42, "AppliNode2-1", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        JobRequest j11 = new JobRequest("AppliNode1-1", "MAG");
        JobRequest j12 = new JobRequest("AppliNode1-2", "MAG");
        JobRequest j21 = new JobRequest("AppliNode2-1", "MAG");

        JqmClientFactory.getClient().enqueue(j11);
        JqmClientFactory.getClient().enqueue(j11);
        JqmClientFactory.getClient().enqueue(j11);
        JqmClientFactory.getClient().enqueue(j12);
        JqmClientFactory.getClient().enqueue(j12);
        JqmClientFactory.getClient().enqueue(j12);
        JqmClientFactory.getClient().enqueue(j21);
        JqmClientFactory.getClient().enqueue(j21);
        JqmClientFactory.getClient().enqueue(j21);

        JqmEngine engine1 = new JqmEngine();
        JqmEngine engine2 = new JqmEngine();
        engine1.start("localhost");
        engine2.start("localhost4");

        // Wait for runs
        TestHelpers.waitFor(9, 5000, em);

        // Stop one node
        em.getTransaction().begin();
        em.createQuery("UPDATE Node n SET n.stop = 'true' WHERE n.name = 'localhost'").executeUpdate();
        em.getTransaction().commit();
        Thread.sleep(100);

        // Enqueue other requests.. they should run on node 2
        JqmClientFactory.getClient().enqueue(j11);
        JqmClientFactory.getClient().enqueue(j11);
        JqmClientFactory.getClient().enqueue(j11);
        JqmClientFactory.getClient().enqueue(j12);
        JqmClientFactory.getClient().enqueue(j12);
        JqmClientFactory.getClient().enqueue(j12);
        JqmClientFactory.getClient().enqueue(j11);
        JqmClientFactory.getClient().enqueue(j11);
        JqmClientFactory.getClient().enqueue(j11);
        JqmClientFactory.getClient().enqueue(j12);
        JqmClientFactory.getClient().enqueue(j12);
        JqmClientFactory.getClient().enqueue(j12);
        JqmClientFactory.getClient().enqueue(j11);
        JqmClientFactory.getClient().enqueue(j11);
        JqmClientFactory.getClient().enqueue(j11);
        JqmClientFactory.getClient().enqueue(j12);
        JqmClientFactory.getClient().enqueue(j12);
        JqmClientFactory.getClient().enqueue(j12);
        JqmClientFactory.getClient().enqueue(j21);
        JqmClientFactory.getClient().enqueue(j21);
        JqmClientFactory.getClient().enqueue(j21);

        TestHelpers.waitFor(27, 5000, em);

        // Only stop node2... node1 should be already dead.
        engine2.stop();

        // Check 27 have ended (3 should be blocked in queue)
        int i = 0;
        for (History jobInstance : em.createQuery("SELECT j FROM History j ORDER BY j.id ASC", History.class).getResultList())
        {
            if (!jobInstance.getState().equals(State.ENDED))
            {
                Assert.fail();
            }
            i++;
        }
        Assert.assertEquals(27, i);
        i = 0;
        for (JobInstance jobInstance : em.createQuery("SELECT j FROM JobInstance j ORDER BY j.id ASC", JobInstance.class).getResultList())
        {
            if (!jobInstance.getState().equals(State.SUBMITTED))
            {
                Assert.fail();
            }
            i++;
        }
        Assert.assertEquals(3, i);
    }
}
