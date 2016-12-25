package ru.wtg.whereaminow.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created 12/20/2016.
 */

@SuppressWarnings("TryWithIdenticalCatches")
public class DBHelper<T extends AbstractSavedItem> {

    private static final int DB_VERSION = 1;
    private static final String COLUMN_ID = "_id";

    public final Fields fields;
    private final Context context;

    private DBOpenHelper mDBHelper;
    private SQLiteDatabase mDB;
    private String selection = null;
    private String[] selectionArgs = null;

    DBHelper(Context context, String itemType, Class<?> item){
        this.context = context;
        fields = new Fields(itemType, item);
        open();
    }

    public void open() {
        mDBHelper = DBOpenHelper.getInstance(context, null, DB_VERSION);
        mDB = mDBHelper.getWritableDatabase();
        if(!isTableExists(fields.itemType)) {
            mDB.execSQL(fields.getCreateString());
        }
    }

    public void close() {
        if (mDBHelper!=null) mDBHelper.close();
    }

    public Cursor getAll() {
        return mDB.query(fields.itemType, null, selection, selectionArgs, null, null, null);
    }

    @SuppressWarnings("WeakerAccess")
    public int getCount() {
        return getAll().getCount();
    }

    @SuppressWarnings("WeakerAccess")
    public Cursor getByPosition(int position) {
        Cursor cursor = getAll();
        cursor.moveToPosition(position);
        return cursor;
    }

    @SuppressWarnings("WeakerAccess")
    public Cursor getById(long id){
        return mDB.query(fields.itemType, null, COLUMN_ID + " = ?", new String[]{String.valueOf(id)},null,null,null);
    }

    public void clear() {
        mDB.delete(fields.itemType, null, null);
    }

    public void setRestrictions(String selection, String[] selectionArgs) {
        this.selection = selection;
        this.selectionArgs = selectionArgs;

    }

    @SuppressWarnings("WeakerAccess")
    public void save(T item) {
        ContentValues cv = new ContentValues();

        for(Map.Entry<String,Fields.FieldOptions> x: fields.fields.entrySet()){
            try {
                Field field = item.getClass().getDeclaredField(x.getValue().name);
                field.setAccessible(true);
                Object value = field.get(item);
                if(value != null) {
                    if(x.getValue().serialize) {
                        cv.put(x.getValue().name+"_", Utils.serializeToString((Serializable) Class.forName(x.getValue().sourceType).cast(value)));
                    } else if (x.getValue().sourceType.equals("boolean")) {
                        cv.put(x.getValue().name+"_", Boolean.class.cast(value));
                    } else if (x.getValue().sourceType.equals("int")) {
                        cv.put(x.getValue().name+"_", Integer.class.cast(value));
                    } else if (x.getValue().sourceType.equals("long")) {
                        cv.put(x.getValue().name+"_", Long.class.cast(value));
                    } else if (x.getValue().sourceType.equals("float")) {
                        cv.put(x.getValue().name+"_", Float.class.cast(value));
                    } else if (x.getValue().sourceType.equals("double")) {
                        cv.put(x.getValue().name+"_", Double.class.cast(value));
                    } else if (x.getValue().sourceType.equals("java.lang.String")) {
                        cv.put(x.getValue().name+"_", String.class.cast(value));
                    }
                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        if(item.getNumber() > 0){
            mDB.update(fields.itemType, cv, COLUMN_ID + " = ?", new String[]{String.valueOf(item.getNumber())});
        } else {
            long a = mDB.insert(fields.itemType, null, cv);
            item.setNumber(a);
        }
    }

    @SuppressWarnings("WeakerAccess")
    public T load(Cursor cursor) {
        T item = null;
        try {
            item = (T) fields.classType.getConstructor(Context.class).newInstance(context);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        for(Map.Entry<String,Fields.FieldOptions> x: fields.fields.entrySet()){
            try {
                Field field = fields.classType.getDeclaredField(x.getValue().name);
                field.setAccessible(true);
                if (x.getValue().serialize) {
                    String value = cursor.getString(cursor.getColumnIndex(x.getValue().name + "_"));
                    field.set(item, Utils.deserializeFromString(value));
                } else if (x.getValue().sourceType.equals("boolean")) {
                    int value = cursor.getInt(cursor.getColumnIndex(x.getValue().name + "_"));
                    field.set(item, value == 1);
                } else if (x.getValue().sourceType.equals("int")) {
                    int value = cursor.getInt(cursor.getColumnIndex(x.getValue().name + "_"));
                    field.set(item, value);
                } else if (x.getValue().sourceType.equals("long")) {
                    long value = cursor.getLong(cursor.getColumnIndex(x.getValue().name + "_"));
                    field.set(item, value);
                } else if (x.getValue().sourceType.equals("float")) {
                    float value = cursor.getFloat(cursor.getColumnIndex(x.getValue().name + "_"));
                    field.set(item, value);
                } else if (x.getValue().sourceType.equals("double")) {
                    double value = cursor.getDouble(cursor.getColumnIndex(x.getValue().name + "_"));
                    field.set(item, value);
                } else if (x.getValue().sourceType.equals("java.lang.String")) {
                    String value = cursor.getString(cursor.getColumnIndex(x.getValue().name + "_"));
                    field.set(item, value);
                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        long value = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
        try {
            Field field = fields.classType.getSuperclass().getDeclaredField("number");
            field.setAccessible(true);
            field.set(item, value);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return item;
    }

    @SuppressWarnings("WeakerAccess")
    public void deleteById(long id) {
        mDB.delete(fields.itemType, COLUMN_ID + " = " + id, null);
    }

    public void deleteByPosition(int position) {
        Cursor cursor = getAll();
        cursor.move(position+1);
        deleteById(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
    }

    @SuppressWarnings("WeakerAccess")
    public void deleteByItem(T item) {
        deleteById(item.getNumber());
    }


    private boolean isTableExists(String tableName) {
        boolean isExist = false;
        Cursor cursor = mDB.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '" + tableName + "'", null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                isExist = true;
            }
            cursor.close();
        }
        return isExist;
    }

    @SuppressWarnings("WeakerAccess")
    protected static class Fields {
        final String itemType;
        final Class classType;
        TreeMap<String,FieldOptions> fields = new TreeMap<>();

        class FieldOptions {
            String name;
            String type;
            String sourceType;
            boolean serialize;
            public String toString(){
                return "{"+name+", "+type+", "+sourceType+", "+ serialize +"}";
            }
        }

        @SuppressWarnings("WeakerAccess")
        public Fields(String itemType, Class item) {
            this.itemType = itemType;
            this.classType = item;
            for (Field field : item.getDeclaredFields()) {
                processField(field);
            }
        }

        private void processField(Field field) {
            if(!Modifier.isStatic(field.getModifiers())
                    && !Modifier.isFinal(field.getModifiers())
                    && !Modifier.isTransient(field.getModifiers()))
            {
                FieldOptions o = new FieldOptions();
                o.name = field.getName();

                if(field.getType().isAssignableFrom(String.class)){
                    o.type = "text";
                    o.serialize = false;
                } else if(field.getType().isAssignableFrom(Boolean.class) || field.getType().isAssignableFrom(Boolean.TYPE)){
                    o.type = "integer";
                    o.serialize = false;
                } else if(field.getType().isAssignableFrom(Integer.class) || field.getType().isAssignableFrom(Integer.TYPE)){
                    o.type = "integer";
                    o.serialize = false;
                } else if(field.getType().isAssignableFrom(Long.class) || field.getType().isAssignableFrom(Long.TYPE)){
                    o.type = "integer";
                    o.serialize = false;
                } else if(field.getType().isAssignableFrom(Float.class) || field.getType().isAssignableFrom(Float.TYPE)){
                    o.type = "real";
                    o.serialize = false;
                } else if(field.getType().isAssignableFrom(Double.class) || field.getType().isAssignableFrom(Double.TYPE)){
                    o.type = "real";
                    o.serialize = false;
                } else {
                    o.type = "blob";
                    o.serialize = Serializable.class.isAssignableFrom(field.getType());
                    if(!Serializable.class.isAssignableFrom(field.getType())){
                        try {
                            throw new Exception("Not serialize value: "+field.getName()+", type: "+field.getType().getCanonicalName());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                o.sourceType = field.getType().getCanonicalName();
                fields.put(field.getName(),o);
            }
        }

        @SuppressWarnings("WeakerAccess")
        public String getCreateString() {
            String res = "create table " + itemType + "(_id integer primary key autoincrement, ";
            for(Map.Entry<String,FieldOptions> x: fields.entrySet()){
                res += x.getValue().name + "_ " + x.getValue().type;
                if(!x.getKey().equals(fields.lastKey())) res += ", ";
            }
            res += ")";
            return res;
        }

    }

}