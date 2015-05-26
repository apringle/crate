package uk.co.alexpringle.crate.test;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;
import uk.co.alexpringle.crate.test.crates.SimpleCrate;
import uk.co.alexpringle.crate.test.crates.SimpleItem;

import java.util.ArrayList;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class CrateTest
{
    private TestHelper testHelper;
    private SimpleCrate testCrate;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception
    {
        ShadowLog.stream = System.out;
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

    @Test
    public void removeWithId()
    {
        SimpleItem simpleItem = testHelper.createRandomSimpleItem();
        testCrate.put(simpleItem);

        Assert.assertTrue(testCrate.exists(simpleItem.getId()));

        testCrate.removeWithId(simpleItem.getId());
        Assert.assertFalse(testCrate.exists(simpleItem.getId()));
        Assert.assertNull(testCrate.withId(simpleItem.getId()));
    }

    @Test
    public void all()
    {
        List<SimpleItem> randomSimpleItems = testHelper.createRandomSimpleItems(5);
        testCrate.put(randomSimpleItems);

        List<SimpleItem> retrievedItems = testCrate.all();

        Assert.assertEquals(5, retrievedItems.size());

        for(SimpleItem retrievedItem : retrievedItems)
        {
            Assert.assertTrue(randomSimpleItems.contains(retrievedItem));
        }
    }

    @Test
    public void removeAll()
    {
        List<SimpleItem> randomSimpleItems = testHelper.createRandomSimpleItems(5);
        testCrate.put(randomSimpleItems);

        List<SimpleItem> retrievedItems = testCrate.all();
        Assert.assertEquals(5,retrievedItems.size());

        testCrate.removeAll();

        retrievedItems = testCrate.all();
        Assert.assertEquals(0,retrievedItems.size());
    }

    @Test
    public void withTag()
    {
        List<SimpleItem> randomSimpleItems = testHelper.createRandomSimpleItems(6);
        testCrate.put(randomSimpleItems, "TEST_TAG");

        List<SimpleItem> unrelatedSimpleItems = testHelper.createRandomSimpleItems(3);
        testCrate.put(unrelatedSimpleItems,"UNRELATED_TAG");

        List<SimpleItem> retrievedItems = testCrate.withTag("TEST_TAG");
        Assert.assertEquals(6, retrievedItems.size());
        for(SimpleItem retrievedItem : retrievedItems)
        {
            Assert.assertTrue(randomSimpleItems.contains(retrievedItem));
        }

        SimpleItem extraRandomItem = testHelper.createRandomSimpleItem();
        testCrate.put(extraRandomItem, "TEST_TAG");

        retrievedItems = testCrate.withTag("TEST_TAG");
        Assert.assertEquals(7,retrievedItems.size());
        Assert.assertTrue(retrievedItems.contains(extraRandomItem));
    }

    @Test
    public void removeWithTag()
    {
        List<SimpleItem> randomSimpleItems = testHelper.createRandomSimpleItems(6);
        testCrate.put(randomSimpleItems, "TEST_TAG");

        List<SimpleItem> unrelatedSimpleItems = testHelper.createRandomSimpleItems(3);
        testCrate.put(unrelatedSimpleItems, "UNRELATED_TAG");

        List<SimpleItem> retrievedItems = testCrate.withTag("TEST_TAG");
        Assert.assertEquals(6, retrievedItems.size());

        testCrate.removeWithTag("TEST_TAG");

        retrievedItems = testCrate.withTag("TEST_TAG");
        Assert.assertEquals(0,retrievedItems.size());

        // ensure unrelated items have not been removed
        List<SimpleItem> unrelatedRetrievedItems = testCrate.withTag("UNRELATED_TAG");
        Assert.assertEquals(3, unrelatedRetrievedItems.size());
    }

    @Test
    public void replaceSingle()
    {
        List<SimpleItem> unrelatedItems = testHelper.createRandomSimpleItems(3);
        testCrate.put(unrelatedItems, "UNRELATED_TAG");

        List<SimpleItem> existingItems = testHelper.createRandomSimpleItems(8);
        testCrate.put(existingItems, "TEST_TAG");

        SimpleItem newItem = testHelper.createRandomSimpleItem();
        testCrate.replace("TEST_TAG", newItem);

        List<SimpleItem> retrievedItems = testCrate.withTag("TEST_TAG");
        Assert.assertEquals(1,retrievedItems.size());
        Assert.assertTrue(retrievedItems.contains(newItem));

        // ensure unrelated items have not been replaced
        List<SimpleItem> retrievedUnrelatedItems = testCrate.withTag("UNRELATED_TAG");
        Assert.assertEquals(3,retrievedUnrelatedItems.size());
    }

    @Test
    public void replaceMultiple()
    {
        List<SimpleItem> unrelatedItems = testHelper.createRandomSimpleItems(3);
        testCrate.put(unrelatedItems, "UNRELATED_TAG");

        List<SimpleItem> existingItems = testHelper.createRandomSimpleItems(4);
        testCrate.put(existingItems, "TEST_TAG");

        List<SimpleItem> newItems = testHelper.createRandomSimpleItems(2);
        testCrate.replace("TEST_TAG", newItems);

        List<SimpleItem> retrievedItems = testCrate.withTag("TEST_TAG");
        Assert.assertEquals(2, retrievedItems.size());
        for(SimpleItem retrievedItem : retrievedItems)
        {
            Assert.assertTrue(newItems.contains(retrievedItem));
        }

        // ensure unrelated items have not been replaced
        List<SimpleItem> retrievedUnrelatedItems = testCrate.withTag("UNRELATED_TAG");
        Assert.assertEquals(3, retrievedUnrelatedItems.size());
    }

    @Test
    public void withNullId()
    {
        expectedException.expect(IllegalArgumentException.class);
        testCrate.withId(null);
    }

    @Test
    public void existsNullId()
    {
        expectedException.expect(IllegalArgumentException.class);
        testCrate.exists(null);
    }

    @Test
    public void withNullTag()
    {
        expectedException.expect(IllegalArgumentException.class);
        testCrate.withTag(null);
    }

    @Test
    public void replaceNullTag()
    {
        expectedException.expect(IllegalArgumentException.class);
        testCrate.replace(null, new ArrayList<SimpleItem>());
    }

    @Test
    public void replaceNullItems()
    {
        expectedException.expect(IllegalArgumentException.class);
        List<SimpleItem> nullItemList = null;
        testCrate.replace("TAG",nullItemList);
    }

    @Test
    public void replaceNullItem()
    {
        expectedException.expect(IllegalArgumentException.class);
        SimpleItem nullSimpleItem = null;
        testCrate.replace("TAG",nullSimpleItem);
    }

    @Test
    public void removeNullId()
    {
        expectedException.expect(IllegalArgumentException.class);
        testCrate.removeWithId(null);
    }

    @Test
    public void removeNullTag()
    {
        expectedException.expect(IllegalArgumentException.class);
        testCrate.removeWithTag(null);
    }

    @Test
    public void putNullItem()
    {
        expectedException.expect(IllegalArgumentException.class);
        SimpleItem nullItem = null;
        testCrate.put(nullItem);
    }

    @Test
    public void putNullItems()
    {
        expectedException.expect(IllegalArgumentException.class);
        List<SimpleItem> nullItemList = null;
        testCrate.put(nullItemList);
    }

    @Test
    public void putItemsNullTag()
    {
        expectedException.expect(IllegalArgumentException.class);
        List<SimpleItem> items = new ArrayList<SimpleItem>();
        testCrate.put(items,null);
    }
}
