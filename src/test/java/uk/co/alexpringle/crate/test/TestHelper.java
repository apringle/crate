package uk.co.alexpringle.crate.test;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import org.robolectric.RuntimeEnvironment;
import uk.co.alexpringle.crate.Crate;

import java.lang.reflect.Field;

public class TestHelper
{
    private SQLiteOpenHelper helperSQLiteOpenHelper;

    public TestHelper() throws NoSuchFieldException, IllegalAccessException
    {
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
}
