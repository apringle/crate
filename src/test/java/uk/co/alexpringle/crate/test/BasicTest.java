package uk.co.alexpringle.crate.test;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import uk.co.alexpringle.crate.test.crates.SimpleCrate;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class BasicTest
{
    @Test
    public void instantiateCrate()
    {
        SimpleCrate simpleCrate = new SimpleCrate(RuntimeEnvironment.application);
        Assert.assertNotNull(simpleCrate);
    }
}
