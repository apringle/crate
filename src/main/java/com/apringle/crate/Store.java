package com.apringle.crate;

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
import java.util.List;

abstract class Store<T extends HasId> extends SQLiteOpenHelper
{
    private static final int STORE_VERSION = 1;
    private static final String DATABASE_NAME = "CRATE_DATABASE";
    protected static final String ID = "ID";
    protected static final String ITEM = "ITEM";
    protected static final String TAG = "TAG";

    private Gson gson;
    private String tableName;

    protected Store(Context context) {
        super(context, DATABASE_NAME, null, STORE_VERSION);
        tableName = this.getClass().getSimpleName();
        gson = new Gson();
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

    protected void beforeSave(T item)
    {
    }

    public final T getById(String itemId)
    {
        SQLiteDatabase database = getReadableDatabase();
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
            Log.d("GroupLab","No item in store with id :" + itemId);
        }

        return item;
    }

    public final boolean exists(String itemId)
    {
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + tableName + " WHERE " + ID + " =?", new String[]{itemId});

        boolean isInDatabase = cursor != null && cursor.moveToFirst();
        if(cursor != null)
        {
            cursor.close();
        }
        return isInDatabase;
    }

    public final List<T> getByTag(String itemTag)
    {
        SQLiteDatabase database = getReadableDatabase();

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

    public final T getFirstByTag(String itemTag)
    {
        SQLiteDatabase database = getReadableDatabase();

        T item = null;
        Cursor cursor = database.rawQuery("SELECT * FROM " + tableName + " WHERE " + TAG + " =?", new String[]{itemTag});
        if(cursor != null && cursor.moveToFirst())
        {
            item = gson.fromJson(cursor.getString(cursor.getColumnIndex(ITEM)), getStoreType());
            cursor.close();
        }
        else
        {
            Log.d("GroupLab", "No items in store with tag :" + itemTag);
        }

        return item;
    }

    public final void replaceTag(Collection<T> items,String itemTag)
    {
        SQLiteDatabase database = getWritableDatabase();
        database.delete(tableName, TAG + "=?", new String[]{itemTag});
        for(T item : items)
        {
            putItem(item,itemTag);
        }
        database.close();
    }

    public final void replaceTag(T item,String itemTag)
    {
        SQLiteDatabase database = getWritableDatabase();
        database.delete(tableName, TAG + "=?", new String[]{itemTag});
        putItem(item,itemTag);
        database.close();
    }

    public final List<T> getAll()
    {
        SQLiteDatabase database = getReadableDatabase();

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

    public final void putItem(T item)
    {
        putItem(item,null);
    }

    public final void putItem(T item,String tag)
    {
        beforeSave(item);
        ContentValues values = new ContentValues();
        values.put(ID,item.getId());
        values.put(ITEM,gson.toJson(item));
        if(tag != null)
        {
            values.put(TAG,tag);
        }
        SQLiteDatabase database = getWritableDatabase();
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

    public final void putItems(List<T> items)
    {
        for(T currentItem : items)
        {
            putItem(currentItem,null);
        }
    }

    public final void putItems(List<T> items,String tag)
    {
        for(T currentItem : items)
        {
            putItem(currentItem,tag);
        }
    }

    protected final Type getStoreType()
    {
        ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
        return parameterizedType.getActualTypeArguments()[0];
    }

}
