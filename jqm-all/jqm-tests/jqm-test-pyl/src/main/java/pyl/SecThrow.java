package pyl;

import com.enioka.jqm.api.JobBase;

public class SecThrow extends JobBase
{
    @Override
    public void start()
    {
        throw new Error("error voluntarily thrown by test payload");
    }
}