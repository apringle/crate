package uk.co.alexpringle.crate.test;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import org.robolectric.RuntimeEnvironment;
import uk.co.alexpringle.crate.Crate;
import uk.co.alexpringle.crate.test.crates.SimpleItem;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TestHelper
{
    private Random random;
    private SQLiteOpenHelper helperSQLiteOpenHelper;

    public TestHelper() throws NoSuchFieldException, IllegalAccessException
    {
        random = new Random(100);

        Field dbNameField = Crate.class.getDeclaredField("DATABASE_NAME");
        dbNameField.setAccessible(true);
        String databaseName = (String) dbNameField.get(null);
        dbNameField.setAccessible(false);

        Field dbVersionField = Crate.class.getDeclaredField("STORE_VERSION");
        dbVersionField.setAccessible(true);
        int databaseVersion = (Integer) dbVersionField.get(null);
        dbVersionField.setAccessible(false);

        helperSQLiteOpenHelper = new SQLiteOpenHelper(RuntimeEnvironment.application, databaseName, null, databaseVersion)
        {
            @Override
            public void onCreate(SQLiteDatabase sqLiteDatabase)
            {

            }

            @Override
            public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1)
            {

            }
        };
    }

    public void clearCrateDatabase() throws NoSuchFieldException, IllegalAccessException
    {
        SQLiteDatabase database = helperSQLiteOpenHelper.getWritableDatabase();
        if(database != null)
        {
            Cursor cursor = database.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

            if(cursor.moveToFirst())
            {
                do
                {
                    String currentTableName = cursor.getString(0);
                    database.execSQL("DROP TABLE IF EXISTS " + currentTableName);
                }
                while (cursor.moveToNext());
            }
            cursor.close();
            database.close();
        }
    }

    public SimpleItem createRandomSimpleItem()
    {
        SimpleItem simpleItem = new SimpleItem();

        simpleItem.setId(Integer.toHexString(random.nextInt()));
        simpleItem.setName("Forename Surname" + random.nextInt());
        simpleItem.setEmail("email@email.com");
        simpleItem.setBalance(random.nextDouble());
        simpleItem.setSomeInt(random.nextInt());
        simpleItem.setSomeLong(random.nextLong());
        simpleItem.setSomeFloat(random.nextFloat());
        simpleItem.setEnabled(random.nextBoolean());

        return simpleItem;
    }

    public List<SimpleItem> createRandomSimpleItems(int count)
    {
        List<SimpleItem> items = new ArrayList<SimpleItem>();
        for(int i = 0; i < count;i++)
        {
            items.add(createRandomSimpleItem());
        }
        return items;
    }
}
