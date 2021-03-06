package com.qgymib.findthetoiletclient.service;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.util.Log;

import com.qgymib.findthetoiletclient.data.ConfigData;

/**
 * 网络通信线程。线程生命周期需要与MainActivity同步。
 * 
 * @author qgymib
 *
 */
public class NetworkService {

    private ExecutorService taskThreadPool;

    /**
     * 初始化线程池。
     */
    public void init() {
        taskThreadPool = Executors
                .newFixedThreadPool(ConfigData.Common.thread_pool_size);
    }

    /**
     * 按照指定尺寸初始化线程池。
     * 
     * @param size
     */
    public void init(int size) {
        taskThreadPool = Executors.newFixedThreadPool(size);
    }

    /**
     * 关闭线程池。已经提交的任务将有序结束，但是不接受新的任务。若线程池已经关闭，此请求对其无任何影响。
     */
    public void shutdown() {
        taskThreadPool.shutdown();
    }

    /**
     * 尝试停止所有激活的任务，抛弃正在等待的任务，并返回等待运行任务的列表。
     */
    public List<Runnable> shutdownNow() {
        return taskThreadPool.shutdownNow();
    }

    /**
     * 请求验证用户
     * 
     * @param username
     *            用户名
     * @param passwd_md5
     *            经md5加密的密码
     * @return 用户权限或错误编码
     */
    public int requestLogin(String username, String passwd_md5) {
        int identity = ConfigData.Account.Errno.connection_error;

        Future<Integer> future = taskThreadPool
                .submit(new NetworkTaskManager().new LoginTask(username,
                        passwd_md5));

        try {
            // 取得运行结果
            identity = future.get(
                    ConfigData.Common.maximum_task_execution_time,
                    TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            // 任务中断
            identity = ConfigData.Account.Errno.unknown;
            Log.e(ConfigData.Common.tag,
                    "login task was interrupted while waiting ");
        } catch (ExecutionException e) {
            // 任务抛出异常
            identity = ConfigData.Account.Errno.unknown;
            Log.w(ConfigData.Common.tag, "login task executed failed");
            e.getCause().printStackTrace();
        } catch (TimeoutException e) {
            // 超出指定运行时间。最可能的是网络连接异常
            identity = ConfigData.Account.Errno.connection_error;
            // 立即中断任务
            if (!future.cancel(true)) {
                // 若任务无法中断
            }
        }

        return identity;
    }

    /**
     * 请求注册用户
     * 
     * @param username
     *            用户名
     * @param passwd_md5
     *            经md5加密的密码
     * @return 任何大于等于0的数值代表用户权限，小于0的数值为具体错误代码
     */
    public int requestSignup(String username, String passwd_md5) {
        int result = ConfigData.Account.Errno.connection_error;

        Future<Integer> future = taskThreadPool
                .submit(new NetworkTaskManager().new SignupTask(username,
                        passwd_md5));

        try {
            result = future.get(ConfigData.Common.maximum_task_execution_time,
                    TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            // 任务中断
            result = ConfigData.Account.Errno.unknown;
            Log.e(ConfigData.Common.tag,
                    "singup task was interrupted while waiting ");
        } catch (ExecutionException e) {
            // 任务抛出异常
            result = ConfigData.Account.Errno.unknown;
            Log.w(ConfigData.Common.tag, "signup task executed failed");
            e.getCause().printStackTrace();
        } catch (TimeoutException e) {
            // 超出指定运行时间。最可能的是网络连接异常
            result = ConfigData.Account.Errno.connection_error;
            // 立即中断任务
            if (!future.cancel(true)) {
                // 若任务无法中断
            }
        }

        return result;
    }

    /**
     * 请求搜索地点
     * 
     * @param locationKey
     *            城市编码
     * @param isNet
     *            是否尝试与服务器交互
     * @return 地理集合<br/>
     *         null - 若搜索失败
     */
    public String requrestSearch(String locationKey, boolean isNet) {
        String result = null;

        Future<String> future = taskThreadPool
                .submit(new NetworkTaskManager().new SearchTask(locationKey, isNet));

        try {
            result = future.get(ConfigData.Common.maximum_task_execution_time,
                    TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            Log.w(ConfigData.Common.tag, "search task failed");
        }

        return result;
    }
}
