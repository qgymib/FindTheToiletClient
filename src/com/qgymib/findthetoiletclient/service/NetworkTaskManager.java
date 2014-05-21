package com.qgymib.findthetoiletclient.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

import com.qgymib.findthetoiletclient.R.string;
import com.qgymib.findthetoiletclient.app.ConfigureInfo;
import com.qgymib.findthetoiletclient.app.Tools;

public class NetworkTaskManager {

    /**
     * 储存连接信息的socket，在任务开始时进行初始化， 在任务结束时销毁，参见 {@link #call()}。
     */
    private Socket clientSocket = null;
    /**
     * 用于向socket写数据，在任务开始时进行初始化， 在任务结束时销毁，参见{@link #call()}。
     */
    private PrintWriter writer = null;
    /**
     * 用于从socket读数据，在任务开始时进行初始化， 在任务结束时销毁，参见{@link #call()}。
     */
    private BufferedReader reader = null;
    /**
     * 最后一次接收到的信息。
     */
    private String lastReceivedMessage = null;
    /**
     * 最后一次发送的信息。
     */
    private String lastSendedMessage = null;

    /**
     * 初始化Task所需资源。此处抛出的异常必须被捕获，以便通知用户并作相应处理。
     * 
     * @throws IOException
     * @see LoginTask#call()
     */
    private void initTask() throws IOException {
        // 初始化客户端socket
        Log.d(ConfigureInfo.Common.tag, "init client socket");
        clientSocket = new Socket(ConfigureInfo.Net.server_address,
                ConfigureInfo.Net.server_port);
        Log.d(ConfigureInfo.Common.tag, "init client socket finished");
        // 设置超时时间
        Log.d(ConfigureInfo.Common.tag, "set connect time out");
        clientSocket.setSoTimeout(ConfigureInfo.Net.connect_timeout);
        Log.d(ConfigureInfo.Common.tag, "set connect time out finished");
        // 初始化writer
        Log.d(ConfigureInfo.Common.tag, "init writer");
        writer = new PrintWriter(new OutputStreamWriter(
                clientSocket.getOutputStream(), ConfigureInfo.Net.encoding),
                false);
        Log.d(ConfigureInfo.Common.tag, "init writer finished");
        // 初始化reader
        Log.d(ConfigureInfo.Common.tag, "init reader");
        reader = new BufferedReader(new InputStreamReader(
                clientSocket.getInputStream(), ConfigureInfo.Net.encoding));
        Log.d(ConfigureInfo.Common.tag, "init reader finished");
    }

    /**
     * 释放Task占用的资源
     * 
     * @throws IOException
     */
    private void purgeTask() {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                Log.e(ConfigureInfo.Common.tag, "socket reader close failed");
            } finally {
                reader = null;
            }
        }

        if (writer != null) {
            writer.close();
            writer = null;
        }

        if (clientSocket != null) {
            try {
                clientSocket.close();
            } catch (IOException e) {
                Log.e(ConfigureInfo.Common.tag, "socket close failed");
            } finally {
                clientSocket = null;
            }
        }
    }

    /**
     * 发送指定信息，并在指定信息后添加CRC32校验码。信息会被缓存，以便当服务器要求时立即进行重发，参见
     * {@link #sendCachedMessage()}
     * 
     * @param messageList
     *            MessageType + message1 + message2 ...
     * @see ConfigureInfo.message_type
     */
    private void sendMessage(int type, String... messageList) {
        // 封装任务类型
        String message = "0x0" + type + "_";
        // 计算数据长度
        int messageListSize = messageList.length;

        // 拼接字符串
        for (int i = 0; i < messageListSize; i++) {
            message += messageList[i];
            message += "_";
        }

        // 完成字符串的校验
        lastSendedMessage = message + "CRC32:"
                + Tools.getCRC32(message.substring(0, message.length() - 1));

        // 发送字符串
        writer.println(lastSendedMessage);
        writer.flush();
    }

    /**
     * 立即发送缓存的信息
     */
    private void sendCachedMessage() {
        writer.println(lastSendedMessage);
        writer.flush();
    }

    /**
     * 接收套接字上的报文。接收到的信息将会被缓存到{@link #lastReceivedMessage}中。
     * 
     * @throws IOException
     */
    private void rcvPackage() throws IOException {
        // 确保得到的不是空信息
        for (int i = ConfigureInfo.Net.socket_read_conunt; i > 0; i--) {
            lastReceivedMessage = reader.readLine();

            if (lastReceivedMessage != null) {
                return;
            }
        }

        // 若执行到此处，则为读取不到信息，抛出IO异常
        throw new IOException("can not read message on local socket "
                + clientSocket.getLocalPort());
    }

    /**
     * 接收有效信息。
     * 
     * @throws IOException
     */
    private void getVaildMessage() throws IOException {
        boolean isGetVaildMessage = false;
        // 数据包最多请求重发次数
        for (int i = ConfigureInfo.Net.connect_reset_count; i > 0; i--) {
            // 接收数据包
            rcvPackage();

            if (checkMessage()) {
                // 信息校验成功，进行下一步处理
                Log.d(ConfigureInfo.Common.tag, "信息校验成功");
                isGetVaildMessage = true;
                break;
            } else {
                // 信息校验失败，请求重新发送数据包
                Log.d(ConfigureInfo.Common.tag, "信息校验失败");
                sendMessage(ConfigureInfo.MessageType.LOST);
            }
        }

        if (!isGetVaildMessage) {
            // 若执行到此处，则为在重发周期内读取不到有效信息
            throw new IOException(
                    "connect shuold be interrupt because of net environment");
        }
    }

    /**
     * 检查接收的报文是否有效。
     * 
     * @param src
     * @return 若报文有效则返回true，否则返回false
     */
    private boolean checkMessage() {
        boolean isValid = false;
        Pattern pattern = Pattern.compile(ConfigureInfo.Regex.parcel);
        Matcher matcher = pattern.matcher(lastReceivedMessage);

        Log.d(ConfigureInfo.Common.tag, "验证消息: " + lastReceivedMessage);
        
        // 校验信息基本格式
        if (!matcher.find()) {
            Log.d(ConfigureInfo.Common.tag, "正则表达式验证不通过");
            isValid = false;
        } else {

            // 由于约定包格式前4个字符必须为形如0x01格式的字串
            // 因此CRC校验符位置小于4的一定为包损坏
            // 由于当无法找到CRC校验符时返回的是-1，因此此处一并将这种情况处理了
            int crcPosition = lastReceivedMessage.indexOf("_CRC32:");
            if (crcPosition >= 4) {
                Log.d(ConfigureInfo.Common.tag, "CRC位置验证通过");
                String crcValue = lastReceivedMessage
                        .substring(crcPosition + 7);

                String rawData = lastReceivedMessage.substring(0, crcPosition);
                if (crcValue.equals(Tools.getCRC32(rawData))) {
                    Log.d(ConfigureInfo.Common.tag, "CRC验证通过");
                    isValid = true;
                } else {
                    Log.d(ConfigureInfo.Common.tag, "CRC验证未通过");
                    isValid = false;
                }
            }
        }
        return isValid;
    }

    /**
     * 取得任务类型。由于Java 1.7版本以下不支持String类型对比，此函数会将任务类型转换为int类型。
     * 
     * @return 转化为int的任务类型
     */
    private final int getTaskType() {
        return Integer.parseInt(lastReceivedMessage.substring(2, 4));
    }

    /**
     * 取得报文中包含的信息列表
     * 
     * @return
     */
    private String[] getMessageList() {
        int crcPosition = lastReceivedMessage.indexOf("_CRC32:");
        String messageContainer = lastReceivedMessage.substring(5, crcPosition);

        return messageContainer.split("_");
    }

    /**
     * 登录任务。返回用户权限或者错误代码
     * 
     * @author qgymib
     * @see ConfigureInfo.account.account_permission
     * @see ConfigureInfo.account.errno
     */
    public class LoginTask implements Callable<Integer> {

        private String username = null;
        private String passwd_md5 = null;

        /**
         * 构建一个任务，向服务器请求验证用户名和密码。
         * 
         * @param username
         *            用户名
         * @param passwd_md5
         *            经md5加密的密码
         */
        public LoginTask(String username, String passwd_md5) {
            this.username = username;
            this.passwd_md5 = passwd_md5;
        }

        @Override
        public Integer call() {
            int result = ConfigureInfo.Account.Errno.unknow;
            // 此处需要捕获所有异常。当异常发生时，需要终止任务并通知用户
            try {
                // 初始化所需资源
                initTask();

                // 标记任务是否完成
                boolean isFinished = false;

                // 向服务器请求验证用户
                sendMessage(ConfigureInfo.MessageType.LOGIN, username,
                        passwd_md5);

                // 在任务未完成的情况下，线程需保持运行
                while (!isFinished) {

                    // 接收有效信息
                    getVaildMessage();

                    // 当请求验证用户时，可能发生的事件为
                    switch (getTaskType()) {

                    // 用户身份验证结果
                    case ConfigureInfo.MessageType.LOGIN:
                        result = actionForLogin();
                        break;

                    // 数据包损坏或丢失，请求重新发送
                    case ConfigureInfo.MessageType.LOST:
                        sendCachedMessage();
                        break;

                    // 服务器确认连接可以关闭
                    case ConfigureInfo.MessageType.FIN:
                        // 标记任务已经结束
                        isFinished = true;
                        break;
                    }
                }

            } catch (IOException e) {
                // 标记处理结果为网络异常
                result = ConfigureInfo.Account.Errno.connection_error;
                Log.d(ConfigureInfo.Common.tag,
                        "if connection filed, this line should be shown in logcat");
            } finally {
                // 关闭端口
                purgeTask();
            }

            return result;
        }

        /**
         * Login处理事件
         * 
         * @return
         */
        private int actionForLogin() {
            // 事件处理完成，客户端请求关闭连接
            sendMessage(ConfigureInfo.MessageType.FIN);

            // 返回处理结果
            return Integer.parseInt(getMessageList()[0]);
        }
    }

    /**
     * 注册任务。返回注册结果或错误编码
     * 
     * @author qgymib
     *
     */
    public class SignupTask implements Callable<Integer> {

        private String username = null;
        private String passwd_md5 = null;

        /**
         * 构建一个任务，向服务器请求注册用户
         * 
         * @param username
         *            用户名
         * @param passwd_md5
         *            经md5加密的密码
         */
        public SignupTask(String username, String passwd_md5) {
            this.username = username;
            this.passwd_md5 = passwd_md5;
        }

        @Override
        public Integer call() throws Exception {
            int result = ConfigureInfo.Account.Errno.unknow;

            try {
                // 初始化任务所需资源
                initTask();

                // 标记任务是否完成
                boolean isFinished = false;

                // 向服务器请求注册用户
                sendMessage(ConfigureInfo.MessageType.SIGNUP, username,
                        passwd_md5);

                while (!isFinished) {
                    // 接收有效信息
                    getVaildMessage();

                    // 当请求注册用户时，可能发生的事件为
                    switch (getTaskType()) {

                    // 注册结果
                    case ConfigureInfo.MessageType.SIGNUP:
                        actionForSignup();
                        break;

                    // 数据包损坏或丢失，请求重新发送
                    case ConfigureInfo.MessageType.LOST:
                        sendCachedMessage();
                        break;

                    // 服务器确认连接可以关闭
                    case ConfigureInfo.MessageType.FIN:
                        // 标记任务已经结束
                        isFinished = true;
                        break;
                    }
                }
            } catch (IOException e) {
                // 标记处理结果为网络异常
                result = ConfigureInfo.Account.Errno.connection_error;
                Log.d(ConfigureInfo.Common.tag,
                        "if connection filed, this line should be shown in logcat");
            } finally {
                // 关闭端口
                purgeTask();
            }

            return result;
        }

        /**
         * 对注册结果的事件处理
         */
        private int actionForSignup() {
            // 事件处理完成，客户端请求关闭连接
            sendMessage(ConfigureInfo.MessageType.FIN);

            // 返回处理结果
            return Integer.parseInt(getMessageList()[0]);
        }

    }

}
