package uk.co.alexpringle.crate.test;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import uk.co.alexpringle.crate.test.crates.SimpleCrate;
import uk.co.alexpringle.crate.test.crates.SimpleItem;

import java.util.List;

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
        testCrate = new SimpleCrate(RuntimeEnvironment.application);
    }

    @After
    public void tearDown() throws Exception
    {
        testHelper.clearCrateDatabase();
        testCrate.close();
    }

    @Test
    public void instantiateCrate()
    {
        SimpleCrate simpleCrate = new SimpleCrate(RuntimeEnvironment.application);
        Assert.assertNotNull(simpleCrate);
        simpleCrate.close();
    }

    @Test
    public void putItem()
    {
        SimpleItem randomItem = testHelper.createRandomSimpleItem();
        testCrate.put(randomItem);

        SimpleItem retrievedItem = testCrate.withId(randomItem.getId());
        Assert.assertNotNull(retrievedItem);
        Assert.assertEquals(randomItem.getId(), retrievedItem.getId());
        Assert.assertEquals(randomItem, retrievedItem);
    }

    @Test
    public void putItems()
    {
        List<SimpleItem> randomSimpleItems = testHelper.createRandomSimpleItems(3);
        testCrate.put(randomSimpleItems);

        for(SimpleItem randomItem : randomSimpleItems)
        {
            SimpleItem retrievedItem = testCrate.withId(randomItem.getId());
            Assert.assertNotNull(retrievedItem);
            Assert.assertEquals(randomItem.getId(), retrievedItem.getId());
            Assert.assertEquals(randomItem, retrievedItem);
        }
    }

    @Test
    public void notExists()
    {
        Assert.assertFalse(testCrate.exists("should not exist 1234"));
    }

    @Test
    public void exists()
    {
        SimpleItem simpleItem = testHelper.createRandomSimpleItem();
        testCrate.put(simpleItem);

        Assert.assertTrue(testCrate.exists(simpleItem.getId()));
    }
}
