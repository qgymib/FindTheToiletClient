package com.qgymib.findthetoiletclient.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseHelper extends SQLiteOpenHelper {

    public DataBaseHelper(Context context) {
        super(context, ConfigData.Database.name, null,
                ConfigData.Database.version);
    }

    public DataBaseHelper(Context context, String name, CursorFactory factory,
            int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 若表不存在则创建
        db.execSQL("CREATE TABLE IF NOT EXISTS "
                + ConfigData.Database.table_name
                + " (location_key varchar(20) NOT NULL, version int NOT NULL, value text NOT NULL, PRIMARY KEY (location_key))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }

}
