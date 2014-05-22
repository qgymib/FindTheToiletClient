package com.qgymib.findthetoiletclient.gui;

import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.qgymib.findthetoiletclient.R;
import com.qgymib.findthetoiletclient.app.ConfigureInfo;
import com.qgymib.findthetoiletclient.app.FTTApplication;
import com.qgymib.findthetoiletclient.app.Tools;
import com.qgymib.findthetoiletclient.service.NetworkService;

/**
 * LoginFragment，用于显示登录界面以及内部登录事务， {@link AccountFragment}的子视图之一。
 * 
 * @author qgymib
 *
 */
public class LoginFragment extends Fragment {
    public static final String fragmentTag = "login";

    private View containView;

    private EditText usernameEditText;
    private EditText passwdEditText;
    private Button loginButton;
    private TextView changeViewToSignupTextView;

    private String username;
    private String passwd_md5;

    public LoginFragment(){
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        containView = inflater.inflate(R.layout.fragment_account_login,
                container, false);
        init();
        return containView;
    }

    /**
     * 初始化组件并设置事件
     */
    private void init() {
        usernameEditText = (EditText) containView
                .findViewById(R.id.editText_login_username);
        passwdEditText = (EditText) containView
                .findViewById(R.id.editText_login_passwd);
        loginButton = (Button) containView.findViewById(R.id.loginButton);
        changeViewToSignupTextView = (TextView) containView
                .findViewById(R.id.textView_changeViewToSignup);

        // 监听登录事件
        loginButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                // 取得用户名
                username = usernameEditText.getText().toString();
                // 取得密码
                String passwd = passwdEditText.getText().toString();

                // 进行本地初步验证。若本地验证不通过则提示信息
                if (!checkInfo(username, passwd)) {
                    Toast.makeText(
                            getParentFragment().getActivity()
                                    .getApplicationContext(),
                            getString(R.string.local_vaild_for_login_failed),
                            Toast.LENGTH_LONG).show();

                    // 跳过服务器验证阶段
                    return;
                }

                // 取得经md5加密的密码
                passwd_md5 = Tools.getMD5(passwd);

                // 关闭用户可编辑界面以防止任务二次执行
                setComponentEnabled(false);

                // 执行验证任务
                LoginTask loginTask = new LoginTask();
                loginTask.execute(username, passwd_md5);

                // 用于储存验证结果
                int result = 0;
                try {
                    // 取得验证结果
                    result = loginTask.get();

                    if (result >= 0) {
                        // 若返回值大于等于0，则登录成功。其返回值为用户权限
                        ConfigureInfo.Account.permission = result;
                        // 提示登陆成功
                        Toast.makeText(
                                getParentFragment().getActivity()
                                        .getApplicationContext(),
                                getString(R.string.success_login),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // 返回值小于0则登录错误。提示相应信息
                        String warnningInfo = null;
                        switch (result) {
                        // 网络连接异常
                        case ConfigureInfo.Account.Errno.connection_error:
                            warnningInfo = getString(R.string.error_connection);
                            break;

                        // 用户名不存在
                        case ConfigureInfo.Account.Errno.username_invalid:
                            warnningInfo = getString(R.string.error_username_invalid);
                            break;

                        // 密码错误
                        case ConfigureInfo.Account.Errno.passwd_invalid:
                            warnningInfo = getString(R.string.error_passwd_invalid);
                            break;

                        // 未知错误
                        case ConfigureInfo.Account.Errno.unknow:
                        // 任何不在列表中的错误均为未知错误
                        default:
                            warnningInfo = getString(R.string.error_unknow);
                            break;
                        }
                        Toast.makeText(
                                getParentFragment().getActivity()
                                        .getApplicationContext(), warnningInfo,
                                Toast.LENGTH_LONG).show();
                    }

                } catch (InterruptedException e) {
                    Log.e(ConfigureInfo.Common.tag,
                            "login asynctask was interrupted");
                    result = -1;
                    Toast.makeText(
                            getParentFragment().getActivity()
                                    .getApplicationContext(),
                            "login asynctask was interrupted",
                            Toast.LENGTH_LONG).show();
                } catch (ExecutionException e) {
                    Log.e(ConfigureInfo.Common.tag,
                            "login asynctask executed failed");
                    result = -1;
                    Toast.makeText(
                            getParentFragment().getActivity()
                                    .getApplicationContext(),
                            "login asynctask executed failed",
                            Toast.LENGTH_LONG).show();
                }

                // 重新开启用户可编辑界面
                setComponentEnabled(true);

                // 若登录成功，则跳转到InfoFragment
                if (result >= 0) {
                    AccountFragment accountFragment = (AccountFragment) getParentFragment();
                    DataTransfer.ViewTransferForAccount dt = (DataTransfer.ViewTransferForAccount) accountFragment;
                    dt.transAction(R.layout.fragment_account_info);
                }
            }
        });

        // 设置字体下划线
        changeViewToSignupTextView.getPaint().setFlags(
                Paint.UNDERLINE_TEXT_FLAG);
        // 监听切换到注册界面事件
        changeViewToSignupTextView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                AccountFragment accountFragment = (AccountFragment) getParentFragment();
                DataTransfer.ViewTransferForAccount dt = (DataTransfer.ViewTransferForAccount) accountFragment;
                dt.transAction(R.layout.fragment_account_signup);
            }
        });
    }

    /**
     * 设置用户界面组件是否可以进行操作。
     * 
     * @param isEnabled
     */
    private void setComponentEnabled(boolean isEnabled) {
        usernameEditText.setEnabled(isEnabled);
        passwdEditText.setEnabled(isEnabled);
        loginButton.setEnabled(isEnabled);
        loginButton.setClickable(isEnabled);
    }

    /**
     * 本地用户名、密码验证。仅当信息通过本地验证之后才向服务器发送信息。
     * 
     * @param username
     *            用户名
     * @param passwd
     *            原始密码
     * @return 若返回真则本地验证通过，否则本地验证失败
     */
    private boolean checkInfo(String username, String passwd) {
        boolean result = true;

        // 验证用户名
        Matcher usernameMatcher = Pattern.compile(ConfigureInfo.Regex.username)
                .matcher(username);
        if (usernameMatcher.find()) {
            // 若用户名有效
            result = true;
        } else {
            // 若用户名无效
            result = false;
        }

        // 用户名验证通过则可以进行密码验证
        if (result) {
            Matcher passwdMatcher = Pattern.compile(ConfigureInfo.Regex.passwd)
                    .matcher(passwd);
            if (passwdMatcher.find()) {
                // 若密码合法
                result = true;
            } else {
                // 若密码不合法
                result = false;
            }
        }

        return result;
    }

    /**
     * 专属于LoginFragment的登录任务。
     * 
     * @author qgymib
     *
     */
    private class LoginTask extends AsyncTask<String, Integer, Integer> {

        @Override
        protected Integer doInBackground(String... params) {

            // 取得网络服务
            FTTApplication app = (FTTApplication) getParentFragment()
                    .getActivity().getApplication();
            NetworkService networkService = app.getNetworkService();

            // 取得验证结果
            return networkService.requestLogin(params[0], params[1]);
        }

    }
}
