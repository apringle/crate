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
    private static String LOG_TAG = "Crate";

    /**
     * Used to enable or disable debug logging
     */
    public static boolean LOGGING_ENABLED = true;

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
        tableName = this.getClass().getName();
        tableName = tableName.replace(".","");
        crateSQLiteOpenHelper = tableSQLiteHelperMap.get(tableName);
        if(crateSQLiteOpenHelper == null)
        {
            crateSQLiteOpenHelper = new CrateSQLiteOpenHelper(context, tableName);
            tableSQLiteHelperMap.put(tableName,crateSQLiteOpenHelper);
        }
        gson = new Gson();
    }


    /**
     * Called before any item is stored in the crate. Useful for tasks such as nulling password fields.
     * @param item The item to be stored in the crate
     */
    protected void beforeSave(T item)
    {
    }

    /**
     * @param itemId Id of item to retrieve
     * @return Item if exists, otherwise null
     */
    public final T withId(String itemId)
    {
        throwIfNull("itemId",itemId);

        SQLiteDatabase database = crateSQLiteOpenHelper.getReadableDatabase();
        T item = null;

        Cursor cursor = database.rawQuery("SELECT * FROM " + tableName + " WHERE " + ID + " =?", new String[]{itemId});
        if(cursor != null && cursor.moveToFirst())
        {
            try
            {
                item = gson.fromJson(cursor.getString(cursor.getColumnIndex(ITEM)), getStoreType());
                log("Retrieved item ",item);
            }
            catch(JsonSyntaxException e)
            {
                error("Failed to read item with id " + itemId, null);
                e.printStackTrace();
            }
            finally
            {
                cursor.close();
            }
        }
        else
        {
            log("No item in crate with id " + itemId,null);
        }

        return item;
    }

    /**
     * @param itemId Id of item
     * @return True if item exists in the crate
     */
    public final boolean exists(String itemId)
    {
        throwIfNull("itemId",itemId);
        return exists(itemId,true);
    }

    private boolean exists(String itemId,boolean log)
    {
        SQLiteDatabase database = crateSQLiteOpenHelper.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + tableName + " WHERE " + ID + " =?", new String[]{itemId});

        boolean isInDatabase = cursor != null && cursor.moveToFirst();
        if(cursor != null)
        {
            cursor.close();
        }

        if(log)
        {
            if(isInDatabase)
            {
                log("No item in crate with id " + itemId,null);
            }
            else
            {
                log("Item in crate with id " + itemId,null);
            }
        }
        return isInDatabase;
    }

    /**
     * @param itemTag Tag of items
     * @return All tagged items or empty list
     */
    public final List<T> withTag(String itemTag)
    {
        throwIfNull("itemTag",itemTag);

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
            log("Retrieved " + items.size() + " items with tag " + itemTag,null);
        }
        else
        {
            log("No items in crate, with tag " + itemTag, null);
        }

        return items;
    }

    /**
     * Replaces all items with the given tag with new items with the same tag
     * @param itemTag Tag to replace
     * @param items Replacement items to store in the crate
     */
    public final void replace(String itemTag, Collection<T> items)
    {
        throwIfNull("itemTag",itemTag);
        throwIfNull("items",items);

        SQLiteDatabase database = crateSQLiteOpenHelper.getWritableDatabase();
        int itemsRemoved = database.delete(tableName, TAG + "=?", new String[]{itemTag});
        log("Removed " + itemsRemoved + " items with tag " + itemTag,null);
        database.close();
        put(items, itemTag);
    }

    /**
     * Replaces all items with the given tag with a new item with the same tag
     * @param itemTag Tag to replace
     * @param item Replacement item to store in the crate
     */
    public final void replace(String itemTag, T item)
    {
        throwIfNull("itemTag", itemTag);
        throwIfNull("item",item);

        SQLiteDatabase database = crateSQLiteOpenHelper.getWritableDatabase();
        int itemsRemoved = database.delete(tableName, TAG + "=?", new String[]{itemTag});
        log("Removed " + itemsRemoved + " items with tag " + itemTag,null);
        put(item, itemTag,true,database);
        database.close();
    }

    /**
     * @param itemId Id of item to remove
     */
    public final void removeWithId(String itemId)
    {
        throwIfNull("itemId",itemId);

        SQLiteDatabase database = crateSQLiteOpenHelper.getWritableDatabase();
        int itemsRemoved = database.delete(tableName, ID + "=?", new String[]{itemId});
        database.close();
        if(itemsRemoved == 1)
        {
            log("Removed item with id " + itemId,null);
        }
        else
        {
            log("No item to remove with id " + itemId,null);
        }
    }

    /**
     * @param itemTag Tag of items to remove
     */
    public final void removeWithTag(String itemTag)
    {
        throwIfNull("itemTag", itemTag);

        SQLiteDatabase database = crateSQLiteOpenHelper.getWritableDatabase();
        int itemsRemoved = database.delete(tableName, TAG + "=?", new String[]{itemTag});
        database.close();
        log("Removed " + itemsRemoved + "items with tag " + itemTag,null);
    }

    /**
     * Removes all items from crate
     */
    public final void removeAll()
    {
        SQLiteDatabase database = crateSQLiteOpenHelper.getWritableDatabase();
        int itemsRemoved = database.delete(tableName, null, null);
        database.close();
        log("Removed " + itemsRemoved + " items",null);
    }

    /**
     * @return All items in crate
     */
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
            log("Retrieved " + items.size() + " items", null);
        }
        else
        {
            log("No items in crate",null);
        }

        return items;
    }

    /**
     * @param item Item to store in crate
     */
    public final void put(T item)
    {
        put(item,null,true,null);
    }

    /**
     *
     * @param item Item to store in crate
     * @param tag Tag for item
     */
    public final void put(T item, String tag)
    {
        put(item, tag, true,null);
    }

    private void put(T item, String tag, boolean log,SQLiteDatabase pDatabase)
    {
        throwIfNull("item",item);
        beforeSave(item);
        ContentValues values = new ContentValues();
        values.put(ID,item.getId());
        values.put(ITEM,gson.toJson(item));
        if(tag != null)
        {
            values.put(TAG,tag);
        }
        SQLiteDatabase database = pDatabase;
        if(pDatabase == null)
        {
            database = crateSQLiteOpenHelper.getWritableDatabase();
        }

        if(exists(item.getId(),false))
        {
            database.update(tableName, values, "ID=?", new String[]{item.getId()});
        }
        else
        {
            database.insert(tableName, null, values);
        }

        if(pDatabase == null)
        {
            database.close();
        }

        if(log)
        {
            if(tag == null)
            {
                log("Stored item",item);
            }
            else
            {
                log("Stored item with tag " + tag,item);
            }
        }
    }

    /**
     *
     * @param items Items to store in crate
     */
    public final void put(Collection<T> items)
    {
        throwIfNull("items", items);
        SQLiteDatabase database = crateSQLiteOpenHelper.getWritableDatabase();
        database.beginTransaction();
        for(T currentItem : items)
        {
            put(currentItem, null, false,database);
        }
        database.setTransactionSuccessful();
        database.endTransaction();
        database.close();
        log("Stored " + items.size() + " items", null);
    }

    /**
     *
     * @param items Items to store in crate
     * @param tag Tag for items
     */
    public final void put(Collection<T> items, String tag)
    {
        throwIfNull("items", items);
        throwIfNull("tag", tag);

        SQLiteDatabase database = crateSQLiteOpenHelper.getWritableDatabase();
        database.beginTransaction();
        for(T currentItem : items)
        {
            put(currentItem, tag, false,database);
        }
        database.setTransactionSuccessful();
        database.endTransaction();
        database.close();
        log("Stored " + items.size() + " items with tag " + tag, null);
    }

    private Type getStoreType()
    {
        ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
        return parameterizedType.getActualTypeArguments()[0];
    }

    public final void close()
    {
        tableSQLiteHelperMap.remove(this.tableName);
        this.crateSQLiteOpenHelper.close();
        log("Closed crate",null);
    }

    private void throwIfNull(String paramName,Object object)
    {
        if(object == null)
        {
            throw new IllegalArgumentException(paramName + " can not be null");
        }
    }


    private String buildLogMessage(String message, T item)
    {
        StringBuilder logStringBuilder = new StringBuilder(this.getClass().getSimpleName());

        logStringBuilder.append("[");
        logStringBuilder.append("@");
        logStringBuilder.append(Integer.toHexString(hashCode()));
        logStringBuilder.append("] ");
        logStringBuilder.append(message);

        if(item != null)
        {
            logStringBuilder.append("\n");
            logStringBuilder.append(item);
        }

        return logStringBuilder.toString();
    }

    private void log(String message,T item)
    {
        if(LOGGING_ENABLED)
        {
            Log.d(LOG_TAG,buildLogMessage(message,item));
        }
    }

    private void error(String message,T item)
    {
        Log.e(LOG_TAG,buildLogMessage(message,item));
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
