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
     * 智能插入/修改城市编码以及对应的洗手间地理信息。<br/>
     * 当locationKey不存在时，插入对应信息<br/>
     * 当locationKey存在时，拥有如下行为：<br/>
     * * remoteVersion > localVersion - 更新数据库<br/>
     * * remoteVersion <= localVersion - 无动作
     * 
     * @param locationKey
     *            城市编码
     * @param locationVersion
     *            信息版本
     * @param locationValueSet
     *            洗手间信息集
     */
    public void insertLocationSet(String locationKey, long locationVersion,
            String locationValueSet) {
        LocationInfo info = getLocationSet(locationKey);
        SQLiteDatabase db = dbh.getWritableDatabase();

        if (info == null) {
            // 本地信息不存在

            ContentValues cv = new ContentValues();
            cv.put("location_key", locationKey);
            cv.put("version", locationVersion);
            cv.put("value", locationValueSet);

            db.insert(ConfigData.Database.table_name, null, cv);
        } else if (locationVersion > info.version) {
            // 本地信息版本过低

            ContentValues cv = new ContentValues();
            cv.put("version", locationVersion);
            cv.put("value", locationValueSet);

            db.update(ConfigData.Database.table_name, cv, "location_key = ?",
                    new String[] { locationKey });
        }
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
        LocationInfo info = null;

        SQLiteDatabase db = dbh.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT version, value FROM "
                + ConfigData.Database.table_name + " WHERE location_key = ?",
                new String[] { locationKey });

        if (cursor.moveToNext()) {
            info = new LocationInfo();
            info.version = cursor.getLong(0);
            info.value = cursor.getString(1);
        }

        return info;
    }
}
