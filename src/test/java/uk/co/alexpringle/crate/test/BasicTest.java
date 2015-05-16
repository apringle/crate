package uk.co.alexpringle.crate.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import uk.co.alexpringle.crate.test.crates.SimpleCrate;
import uk.co.alexpringle.crate.test.crates.SimpleItem;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class BasicTest
{
    private TestHelper testHelper;
    private SimpleCrate testCrate;

    @Before
    public void setUp() throws Exception
    {
        testHelper = new TestHelper();
        testHelper.clearCrateDatabase();
        testCrate = new SimpleCrate(RuntimeEnvironment.application);
    }

    @Test
    public void instantiateCrate()
    {
        SimpleCrate simpleCrate = new SimpleCrate(RuntimeEnvironment.application);
        Assert.assertNotNull(simpleCrate);
    }

    @Test
    public void putItem()
    {
        SimpleItem randomItem = testHelper.createRandomSimpleItem();
        testCrate.put(randomItem);

        SimpleItem retrievedItem = testCrate.withId(randomItem.getId());
        Assert.assertNotNull(retrievedItem);
        Assert.assertEquals(randomItem.getId(),retrievedItem.getId());
        Assert.assertEquals(randomItem,retrievedItem);
    }
}
