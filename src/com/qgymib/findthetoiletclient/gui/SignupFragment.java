package com.qgymib.findthetoiletclient.gui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.qgymib.findthetoiletclient.R;
import com.qgymib.findthetoiletclient.app.FTTApplication;
import com.qgymib.findthetoiletclient.app.Tools;
import com.qgymib.findthetoiletclient.data.ConfigData;
import com.qgymib.findthetoiletclient.data.DataTransfer;
import com.qgymib.findthetoiletclient.service.NetworkService;

/**
 * SignupFragment，用于显示注册界面以及内部登录事务， {@link AccountFragment}的子视图之一。
 * 
 * @author qgymib
 *
 */
public class SignupFragment extends Fragment {
    public static final String fragmentTag = "signup";

    private View containView;

    private TextView infoTextView;
    private EditText usernameTextView;
    private EditText passwdTextView;
    private EditText repeatPasswdTextView;
    private Button signupButton;
    private TextView changeViewToLoginTextView;

    private String username = null;
    private String passwd_md5 = null;

    private boolean isUsernameVaild = false;
    private boolean isPasswdVaild = false;
    private boolean isRepeatPasswdVaild = false;

    private boolean isFirstCheckUsername = true;
    private boolean isFirstCheckPasswd = true;
    private boolean isFirstCheckRepeatPasswd = true;

    public SignupFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        containView = inflater.inflate(R.layout.fragment_account_signup,
                container, false);
        init();
        return containView;
    }

    /**
     * 初始化组件并设置事件
     */
    private void init() {

        infoTextView = (TextView) containView
                .findViewById(R.id.textView_signup_info);
        usernameTextView = (EditText) containView
                .findViewById(R.id.editText_signup_username);
        passwdTextView = (EditText) containView
                .findViewById(R.id.editText_signup_passwd);
        repeatPasswdTextView = (EditText) containView
                .findViewById(R.id.editText_signup_repeat_passwd);
        signupButton = (Button) containView.findViewById(R.id.button_signup);
        changeViewToLoginTextView = (TextView) containView
                .findViewById(R.id.textView_changeViewToLogin);

        // 在用户未填写完成的情况下，按钮不可点击
        checkButtonClickable();

        // 用户名有效性校验
        usernameTextView.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                    int count) {
                // do nothing
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
                // do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 仅当此文本框失去过一次焦点之后才进行实时校验
                if (isFirstCheckUsername) {
                    return;
                }

                // 校验用户名
                checkUsername();

                // 重新设置注册按钮的可用性
                checkButtonClickable();
            }
        });
        usernameTextView.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // 当第一次失去焦点时执行用户名检查
                if (!hasFocus && isFirstCheckUsername) {
                    // 校验用户名
                    checkUsername();

                    // 标记已经执行过首次校验
                    isFirstCheckUsername = false;

                    // 重新设置注册按钮的可用性
                    checkButtonClickable();
                }
            }
        });

        // 密码有效性检查
        passwdTextView.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                    int count) {
                // do nothing
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
                // do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 仅当此文本框失去过一次焦点之后才进行实时校验
                if (isFirstCheckPasswd) {
                    return;
                }

                // 校验密码
                checkPasswd();

                // 重新设置注册按钮的可用性
                checkButtonClickable();
            }
        });
        passwdTextView.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // 当第一次失去焦点时执行密码检查
                if (!hasFocus && isFirstCheckPasswd) {
                    // 校验密码
                    checkPasswd();

                    // 标记已经执行过首次校验
                    isFirstCheckPasswd = false;

                    // 重新设置注册按钮的可用性
                    checkButtonClickable();
                }
            }
        });

        // 重复输入密码有效性检查
        repeatPasswdTextView.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                    int count) {
                // do nothing
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
                // do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isFirstCheckRepeatPasswd) {
                    return;
                }

                // 校验重复输入的密码
                checkRepeatPasswd();

                // 重新设置注册按钮的可用性
                checkButtonClickable();
            }
        });
        repeatPasswdTextView
                .setOnFocusChangeListener(new OnFocusChangeListener() {

                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (!hasFocus && isFirstCheckRepeatPasswd) {
                            // 校验重复输入的密码
                            checkRepeatPasswd();

                            // 标记已经执行过首次校验
                            isFirstCheckRepeatPasswd = false;

                            // 重新设置注册按钮的可用性
                            checkButtonClickable();
                        }
                    }
                });

        // 点击注册按钮的事件
        signupButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 若产生了点击事件，则说明上述四项校验已经通过
                // 产生经md5加密的密码
                passwd_md5 = Tools.getMD5(passwdTextView.getText().toString());

                SignupTask signupTask = new SignupTask();
                signupTask.execute(username, passwd_md5);

            }
        });

        // 设置下划线
        changeViewToLoginTextView.getPaint()
                .setFlags(Paint.UNDERLINE_TEXT_FLAG);
        // 监听切换到登录界面事件
        changeViewToLoginTextView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                AccountFragment accountFragment = (AccountFragment) getParentFragment();
                DataTransfer.ViewTransfer dt = (DataTransfer.ViewTransfer) accountFragment;
                dt.viewTransAction(R.layout.fragment_account_login);
            }
        });
    }

    /**
     * 重新设置注册按钮的可用性，提示无效内容
     */
    private void checkButtonClickable() {
        signupButton.setClickable(isUsernameVaild && isPasswdVaild
                && isRepeatPasswdVaild);
        signupButton.setEnabled(isUsernameVaild && isPasswdVaild
                && isRepeatPasswdVaild);
    }

    /**
     * 校验用户名
     */
    private void checkUsername() {
        username = usernameTextView.getText().toString();
        Matcher matcher = Pattern.compile(ConfigData.Regex.username).matcher(
                username);

        if (matcher.find()) {
            // 若用户名有效
            isUsernameVaild = true;
            infoTextView.setText(null);
        } else {
            // 若用户名无效
            isUsernameVaild = false;
            infoTextView.setText(getResources().getString(
                    R.string.fragment_account_signup_username_invalid));
        }
    }

    /**
     * 校验密码
     */
    private void checkPasswd() {
        String passwd = passwdTextView.getText().toString();
        Matcher matcher = Pattern.compile(ConfigData.Regex.passwd).matcher(
                passwd);

        if (matcher.find()) {
            // 若密码合法
            infoTextView.setText(null);
            isPasswdVaild = true;
        } else {
            // 若密码不合法
            infoTextView.setText(getResources().getString(
                    R.string.fragment_account_signup_passwd_invalid));
            isPasswdVaild = false;
        }
    }

    /**
     * 校验重复输入的密码
     */
    private void checkRepeatPasswd() {
        String passwd = passwdTextView.getText().toString();
        String repeatPasswd = repeatPasswdTextView.getText().toString();

        if (passwd.equals(repeatPasswd)) {
            // 若两次输入的密码相符
            infoTextView.setText(null);
            isRepeatPasswdVaild = true;
        } else {
            // 若两次输入的密码不相符
            infoTextView.setText(getResources().getString(
                    R.string.fragment_account_signup_passwd_repeat_invalid));
            isRepeatPasswdVaild = false;
        }
    }

    @SuppressWarnings("unused")
    private void checkInfo() {
        // TODO 校验用户输入的用户名、密码、邮箱合法性
        // 实现一个富文本框，检查用户的输入信息。当上述textview第一次失去焦点时进行校验，在其后则进行实时校验。
        // 对于任意一个用户输入错误，则在文本框中添加一条信息指出何处有错。在实时校验中，若用户修正了错误，则删除对应信息。
    }

    private class SignupTask extends AsyncTask<String, Integer, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            // 取得网络服务
            FTTApplication app = (FTTApplication) getParentFragment()
                    .getActivity().getApplication();
            NetworkService networkService = app.getNetworkService();

            return networkService.requestSignup(params[0], params[1]);
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            if (result >= 0) {
                // 若返回值大于等于0，则注册成功
                // 提示登陆成功
                Toast.makeText(
                        getParentFragment().getActivity()
                                .getApplicationContext(),
                        getString(R.string.success_signup), Toast.LENGTH_SHORT)
                        .show();

            } else {
                String warnningInfo;

                // 注册不成功，提示相应信息
                switch (result) {

                // 用户名已存在
                case ConfigData.Account.Errno.username_taken:
                    warnningInfo = getString(R.string.error_username_taken);
                    break;

                // 网络连接异常
                case ConfigData.Account.Errno.connection_error:
                    warnningInfo = getString(R.string.error_connection);
                    break;

                case ConfigData.Account.Errno.unknown:
                default:
                    warnningInfo = getString(R.string.error_unknow);
                    break;
                }

                Toast.makeText(
                        getParentFragment().getActivity()
                                .getApplicationContext(), warnningInfo,
                        Toast.LENGTH_LONG).show();
            }

            // 若注册成功，则跳转到InfoFragment
            if (result >= 0) {
                // 注册用户名
                ConfigData.Account.username = username;
                // 注册用户权限
                ConfigData.Account.permission = result;
                // 跳转至InfoFragment
                AccountFragment accountFragment = (AccountFragment) getParentFragment();
                DataTransfer.ViewTransfer dt = (DataTransfer.ViewTransfer) accountFragment;
                dt.viewTransAction(R.layout.fragment_account_info);
            }
        }

    }

}
