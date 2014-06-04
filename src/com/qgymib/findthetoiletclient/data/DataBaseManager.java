package com.qgymib.findthetoiletclient.data;

import com.qgymib.findthetoiletclient.data.DataTransfer.LocationInfo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DataBaseManager {
    /**
     * 数据库辅助类
     */
    private DataBaseHelper dbh = null;

    /**
     * 传入环境以便打开或创建数据库
     * 
     * @param context
     */
    public DataBaseManager(Context context) {
        dbh = new DataBaseHelper(context);
    }

    /**
     * 插入城市编码以及对应的洗手间地理信息
     * 
     * @param locationKey
     *            城市编码
     * @param locationVersion
     *            信息版本
     * @param locationValueSet
     *            洗手间信息集
     */
    public void insertLocationSet(String locationKey, int locationVersion,
            String locationValueSet) {
        SQLiteDatabase db = dbh.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("location_key", locationKey);
        cv.put("version", locationVersion);
        cv.put("value", locationValueSet);

        db.insert(ConfigData.Database.table_name, null, cv);
    }

    /**
     * 返回查询城市编码所对应的洗手间信息
     * 
     * @param locationKey
     *            城市编码
     * @return {@link LocationInfo} - 若查询成功<br/>
     *         null - 若查询失败
     */
    public LocationInfo getLocationSet(String locationKey) {
        LocationInfo info = new LocationInfo();

        // TODO 完成取得对应城市的厕所列表功能
        SQLiteDatabase db = dbh.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT version, value FROM "
                + ConfigData.Database.table_name + " WHERE location_key = ?",
                new String[] { locationKey });

        if (cursor.moveToNext()) {
            info.version = cursor.getLong(0);
            info.value = cursor.getString(1);
        }

        return info;
    }
}
