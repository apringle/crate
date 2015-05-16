package uk.co.alexpringle.crate;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public abstract class Crate<T extends HasId>
{
    private static final int STORE_VERSION = 1;
    private static final String DATABASE_NAME = "CRATE_DATABASE";
    private static final String ID = "ID";
    private static final String ITEM = "ITEM";
    private static final String TAG = "TAG";

    private static HashMap<String,CrateSQLiteOpenHelper> tableSQLiteHelperMap = new HashMap<String, CrateSQLiteOpenHelper>();

    private CrateSQLiteOpenHelper crateSQLiteOpenHelper;
    private Gson gson;
    private String tableName;

    protected Crate(Context context)
    {
        tableName = this.getClass().getSimpleName();
        crateSQLiteOpenHelper = tableSQLiteHelperMap.get(tableName);
        if(crateSQLiteOpenHelper == null)
        {
            crateSQLiteOpenHelper = new CrateSQLiteOpenHelper(context, tableName);
            tableSQLiteHelperMap.put(tableName,crateSQLiteOpenHelper);
        }
        gson = new Gson();
    }


    protected void beforeSave(T item)
    {
    }

    public final T withId(String itemId)
    {
        SQLiteDatabase database = crateSQLiteOpenHelper.getReadableDatabase();
        T item = null;

        Cursor cursor = database.rawQuery("SELECT * FROM " + tableName + " WHERE " + ID + " =?", new String[]{itemId});
        if(cursor != null && cursor.moveToFirst())
        {
            try
            {
                item = gson.fromJson(cursor.getString(cursor.getColumnIndex(ITEM)), getStoreType());
            }
            catch(JsonSyntaxException e)
            {
                Log.e("GroupLab", "Failed to read item with id : " + itemId);
                e.printStackTrace();
            }
            finally
            {
                cursor.close();
            }
        }
        else
        {
            Log.d("GroupLab", "No item in store with id :" + itemId);
        }

        return item;
    }

    public final boolean exists(String itemId)
    {
        SQLiteDatabase database = crateSQLiteOpenHelper.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + tableName + " WHERE " + ID + " =?", new String[]{itemId});

        boolean isInDatabase = cursor != null && cursor.moveToFirst();
        if(cursor != null)
        {
            cursor.close();
        }
        return isInDatabase;
    }

    public final List<T> withTag(String itemTag)
    {
        SQLiteDatabase database = crateSQLiteOpenHelper.getReadableDatabase();

        List<T> items = new ArrayList<T>();

        Cursor cursor = database.rawQuery("SELECT * FROM " + tableName + " WHERE " + TAG + " =?", new String[]{itemTag});
        if(cursor != null && cursor.moveToFirst())
        {
            do
            {
                T newItem = gson.fromJson(cursor.getString(cursor.getColumnIndex(ITEM)), getStoreType());
                items.add(newItem);
            }
            while(cursor.moveToNext());
            cursor.close();
        }
        else
        {
            Log.d("GroupLab", "No items in store with tag :" + itemTag);
        }

        return items;
    }

    public final void replace(String itemTag, Collection<T> items)
    {
        SQLiteDatabase database = crateSQLiteOpenHelper.getWritableDatabase();
        database.delete(tableName, TAG + "=?", new String[]{itemTag});
        for(T item : items)
        {
            put(item, itemTag);
        }
        database.close();
    }

    public final void replace(String itemTag, T item)
    {
        SQLiteDatabase database = crateSQLiteOpenHelper.getWritableDatabase();
        database.delete(tableName, TAG + "=?", new String[]{itemTag});
        put(item, itemTag);
        database.close();
    }

    public final void removeWithId(String itemId)
    {
        SQLiteDatabase database = crateSQLiteOpenHelper.getWritableDatabase();
        database.delete(tableName, ID + "=?", new String[]{itemId});
        database.close();
    }

    public final void removeWithTag(String itemTag)
    {
        SQLiteDatabase database = crateSQLiteOpenHelper.getWritableDatabase();
        database.delete(tableName, TAG + "=?", new String[]{itemTag});
        database.close();
    }

    public final void removeAll()
    {
        SQLiteDatabase database = crateSQLiteOpenHelper.getWritableDatabase();
        database.delete(tableName, null, null);
        database.close();
    }

    public final List<T> all()
    {
        SQLiteDatabase database = crateSQLiteOpenHelper.getReadableDatabase();

        List<T> items = new ArrayList<T>();

        Cursor cursor = database.rawQuery("SELECT * FROM " + tableName, new String[]{});
        if(cursor != null && cursor.moveToFirst())
        {
            do
            {
                T newItem = gson.fromJson(cursor.getString(cursor.getColumnIndex(ITEM)), getStoreType());
                items.add(newItem);
            }
            while(cursor.moveToNext());
            cursor.close();
        }
        else
        {
            Log.d("GroupLab", "No items in store");
        }

        return items;
    }

    public final void put(T item)
    {
        put(item, null);
    }

    public final void put(T item, String tag)
    {
        beforeSave(item);
        ContentValues values = new ContentValues();
        values.put(ID,item.getId());
        values.put(ITEM,gson.toJson(item));
        if(tag != null)
        {
            values.put(TAG,tag);
        }
        SQLiteDatabase database = crateSQLiteOpenHelper.getWritableDatabase();
        if(exists(item.getId()))
        {
            database.update(tableName, values, "ID=?", new String[]{item.getId()});
        }
        else
        {
            database.insert(tableName, null, values);
        }
        database.close();
    }

    public final void put(List<T> items)
    {
        for(T currentItem : items)
        {
            put(currentItem, null);
        }
    }

    public final void put(List<T> items, String tag)
    {
        for(T currentItem : items)
        {
            put(currentItem, tag);
        }
    }

    private Type getStoreType()
    {
        ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
        return parameterizedType.getActualTypeArguments()[0];
    }

    private static class CrateSQLiteOpenHelper extends SQLiteOpenHelper
    {
        private String tableName;

        public CrateSQLiteOpenHelper(Context context,String tableName)
        {
            super(context,DATABASE_NAME,null,STORE_VERSION);
            this.tableName = tableName;
        }

        @Override
        public final void onCreate(SQLiteDatabase db) {

        }

        @Override
        public final void onOpen(SQLiteDatabase db) {
            super.onOpen(db);
            String createStatement = "CREATE TABLE IF NOT EXISTS " + tableName + "(" +
                    ID + " TEXT PRIMARY KEY," + ITEM + " TEXT," + TAG + " TEXT)";
            db.execSQL(createStatement);
        }

        @Override
        public final void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

            if(cursor.moveToFirst())
            {
                do
                {
                    String currentTableName = cursor.getString(0);
                    db.execSQL("DROP TABLE IF EXISTS " + currentTableName);
                }
                while (cursor.moveToNext());
            }
            cursor.close();
        }
    }

}
