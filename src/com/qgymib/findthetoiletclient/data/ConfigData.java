package com.qgymib.findthetoiletclient.data;

import android.content.SharedPreferences;

/**
 * ConfigureInfo用于储存所有配置信息。
 * 
 * @author qgymib
 *
 */
public final class ConfigData {

    /**
     * 从配置文件中取得配置信息
     */
    public static void initPreferences(SharedPreferences preferences) {

        ConfigData.Account.isLogin = preferences.getBoolean("isLogin", false);
        ConfigData.Account.username = preferences.getString("username", null);
        ConfigData.Account.passwd_md5 = preferences.getString("passwd_md5",
                null);
        // TODO 功能完成时，需将权限改为normal
        ConfigData.Account.permission = preferences.getInt("permission",
                ConfigData.Account.Permission.developer);
        ConfigData.Cache.city = preferences.getString("cache_city", null);
    }

    /**
     * 更新配置信息
     */
    public static void updatePreferences(SharedPreferences preferences) {
        preferences.edit().putBoolean("isLogin", ConfigData.Account.isLogin)
                .commit();
        preferences.edit().putString("username", ConfigData.Account.username)
                .commit();
        preferences.edit()
                .putString("passwd_md5", ConfigData.Account.passwd_md5)
                .commit();
        preferences.edit().putInt("permission", ConfigData.Account.permission)
                .commit();
        preferences.edit().putString("cache_city", ConfigData.Cache.city);
    }

    /**
     * 可修改的用户自定义设置。在软件启动时加载，视实际需要可动态刷新。
     * 
     * @author qgymib
     *
     */
    public static final class Custom {
        /**
         * 地图中洗手间最多显示个数
         */
        public static int max_show_toilet_num = 5;
    }

    /**
     * 客户端的一般设定。
     * 
     * @author qgymib
     *
     */
    public static final class Common {
        /**
         * 用于Logcat输出的TAG。
         */
        public static final String tag = "com.qgymib.tag";
        /**
         * 后台通信线程池的大小。
         */
        public static final int thread_pool_size = 1;
        /**
         * 单一任务执行时间上限，单位 毫秒
         */
        public static final long maximum_task_execution_time = 10 * 1000;
        /**
         * 配置文件名称
         */
        public static final String preferences = "FindTheToilet";
    }

    /**
     * 储存用户信息。
     * 
     * @author qgymib
     *
     */
    public static final class Account {
        /**
         * 用户是否登录。<br>
         * false - 未登录<br>
         * true - 已登陆<br>
         */
        public static boolean isLogin = false;
        /**
         * 用户名
         */
        public static String username = null;
        /**
         * 经md5加密的密码
         */
        public static String passwd_md5 = null;
        /**
         * 用户权限。默认为普通用户。
         */
        public static int permission = ConfigData.Account.Permission.developer;

        /**
         * 用户权限列表。所有数值均大于零以表示权限。
         * 
         * @author qgymib
         *
         */
        public static final class Permission {
            /**
             * 正常权限（普通用户）
             */
            public static final int normal = 0;
            /**
             * 高级权限（管理员用户）
             */
            public static final int admin = 1;
            /**
             * 完全控制（开发者）
             */
            public static final int developer = 2;
        }

        /**
         * 登录/注册错误代码。所有数值均小于零。
         * 
         * @author qgymib
         *
         */
        public static final class Errno {
            /**
             * 用户名不存在
             */
            public static final int username_invalid = -1;
            /**
             * 密码错误
             */
            public static final int passwd_invalid = -2;
            /**
             * 网络连接异常
             */
            public static final int connection_error = -3;
            /**
             * 由于未知原因导致的验证失败
             */
            public static final int unknown = -4;
            /**
             * 用户名已存在
             */
            public static final int username_taken = -5;
        }
    }

    /**
     * 定位配置
     * 
     * @author qgymib
     *
     */
    public static final class Location {

        /**
         * 定位时间间隔
         */
        public static final int interval = 1000;
        /**
         * 定位结果集
         */
        public static final String type = "bd0911";

        /**
         * 定位信息类别关键字
         * 
         * @author qgymib
         *
         */
        public static final class Key {
            /**
             * 定位结果有效性
             */
            public static final String isValid = "isValid";
            /**
             * 纬度
             */
            public static final String latitude = "latitude";
            /**
             * 经度
             */
            public static final String longitude = "longitude";
            /**
             * 半径
             */
            public static final String radius = "radius";
            /**
             * 定位类别：GPS/基站
             */
            public static final String type = "type";
            /**
             * 时间
             */
            public static final String time = "time";
            /**
             * 运行状态代码
             */
            public static final String loc_type = "loc_type";
            /**
             * 运动速度
             */
            public static final String speed = "speed";
            /**
             * 卫星数量
             */
            public static final String satellite = "satellite";
            /**
             * 运动方向
             */
            public static final String direction = "direction";
            /**
             * 地址
             */
            public static final String address = "address";
            /**
             * 运营商信息
             */
            public static final String operationer = "operationer";
        }
    }

    /**
     * SQLite相关参数
     * 
     * @author qgymib
     *
     */
    public static final class Database {
        /**
         * 数据库名称
         */
        public static final String name = "ftt.db";
        /**
         * 表名称
         */
        public static final String table_name = "location";
        /**
         * 数据库版本
         */
        public static final int version = 1;
    }

    /**
     * 缓存信息
     * 
     * @author qgymib
     *
     */
    public static final class Cache {
        /**
         * 缓存城市
         */
        public static String city = null;
    }

    /**
     * 百度地图相关参数
     * 
     * @author qgymib
     *
     */
    public static final class Map {
        public static final int zoom_level = 17;
    }

    /**
     * 网络连接约定。
     * 
     * @author qgymib
     *
     */
    public static final class Net {
        /**
         * 服务器地址
         */
        // public static final String server_address = "10.0.2.2";
        public static final String server_address = "192.168.2.237";
        /**
         * 服务器端口
         */
        public static final int server_port = 9876;
        /**
         * 连接超时时间，单位 毫秒
         */
        public static final int connect_timeout = 10 * 1000;
        /**
         * 对一个socket上信息的读取次数。当读取完标记次数之后接收的信息仍为null时，将抛出异常。
         */
        public static final int socket_read_conunt = 10;
        /**
         * 当信息无效时，客户端总共请求的次数。 超出次数限制时，将断开与服务器的连接并提示相关信息。
         */
        public static final int connect_reset_count = 10;
        /**
         * 网络连接编码格式
         */
        public static final String encoding = "UTF-8";
    }

    /**
     * 自定义协议类型
     * 
     * @author qgymib
     *
     */
    public static final class MessageType {
        /**
         * 交互已完成，可以关闭连接
         */
        public static final int FIN = 0x00;
        /**
         * 搜索周边洗手间信息
         */
        public static final int SEARCH = 0x01;
        /**
         * 修正洗手间地点信息
         */
        public static final int FIX = 0x02;
        /**
         * 请求增加洗手间地点
         */
        public static final int INSERT = 0x03;
        /**
         * 请求删除洗手间信息
         */
        public static final int DELETE = 0x04;
        /**
         * 请求注册用户
         */
        public static final int SIGNUP = 0x05;
        /**
         * 请求验证用户
         */
        public static final int LOGIN = 0x06;
        /**
         * 数据包损坏或丢失，请求重新发送数据包
         */
        public static final int LOST = 0x07;
        /**
         * 搜索结果-数据版本
         */
        public static final int SEARCH_VERSION = 0x08;
        /**
         * 搜索结果-数据信息
         */
        public static final int SEARCH_VALUE = 0x09;
    }

    /**
     * 正则表达式校验字符串库。
     * 
     * @author qgymib
     *
     */
    public static final class Regex {
        /**
         * 校验报文。
         */
        public static final String parcel = "^0x0[0-9][\\d\\w_\\-:]*_CRC32:[a-fA-F\\d]{1,}$";
        /**
         * 校验用户名。用户名由字母、数字、下划线组成，长度至少为6个字符。
         */
        public static final String username = "^[\\w\\d_]{6,}$";
        /**
         * 校验邮箱。
         */
        public static final String email = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
        /**
         * 校验原始密码。密码可由字母、数字、下划线组成，长度至少6个字符，至多16个字符。
         */
        public static final String passwd = "^[\\w\\d_]{6,16}$";
    }

}
