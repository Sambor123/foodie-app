package com.foodie.app.Activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.foodie.app.Entity.JsonResult;
import com.foodie.app.Entity.Result;
import com.foodie.app.R;
import com.foodie.app.model.User;
import com.foodie.app.util.Constant;
import com.foodie.app.util.HttpUtils;
import com.foodie.app.util.PrefUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.cookie.Cookie;
import cz.msebera.android.httpclient.impl.cookie.BasicClientCookie;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {
    protected static final String TAG="LoginActivity";
    // UI references.
    private EditText mPhoneView;
    private EditText mPasswordView;
    private EditText mNicknameView;
    private EditText mPasswordAgainView;
    private View mProgressView;
    private View mLoginFormView;
    private Button mSignInButton;
    private Button mSwitchButton;

    private Boolean isRegiterAction=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mPhoneView = (EditText) findViewById(R.id.et_phone);
        mPasswordView = (EditText) findViewById(R.id.et_password);
        mPasswordAgainView= (EditText) findViewById(R.id.et_password_again);
        mNicknameView= (EditText) findViewById(R.id.et_nickname);

        mSignInButton = (Button) findViewById(R.id.btn_login_register);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isRegiterAction){
                    attemptRegister();
                }else{
                    attemptLogin();
                }
            }
        });
        mSwitchButton= (Button) findViewById(R.id.switch_button);
        mSwitchButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isRegiterAction==false){
                    isRegiterAction=true;
                    mSwitchButton.setText("已有帐号?");
                    mSignInButton.setText("注册账号");
                    clearText();
                    mNicknameView.setVisibility(View.VISIBLE);
                    mPasswordAgainView.setVisibility(View.VISIBLE);
                }else{
                    clearText();
                    mSignInButton.setText("登录");
                    mSwitchButton.setText("还没有帐号?");
                    isRegiterAction=false;
                    mNicknameView.setVisibility(View.GONE);
                    mPasswordAgainView.setVisibility(View.GONE);
                }
            }
        });
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void clearText() {
        mPasswordAgainView.setText("");
        mNicknameView.setText("");
        mPasswordView.setText("");
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        mPhoneView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String phone = mPhoneView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }
        // Check for a valid email address.
        if (TextUtils.isEmpty(phone)) {
            mPhoneView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (!isPhoneValid(phone)) {
            mPhoneView.setError(getString(R.string.error_invalid_email));
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel){
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            if (HttpUtils.isNetworkConnected(LoginActivity.this)) {
                //封装请求参数
                RequestParams requestParams=new RequestParams();
                requestParams.put("phone",phone);
                requestParams.put("password",password);
                HttpUtils.post(Constant.LOGIN, requestParams, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Gson gson=new Gson();
                        Type type = new TypeToken<JsonResult<User>>(){}.getType();
                        JsonResult<User> jsonResult=gson.fromJson(new String(responseBody),type);
                        String status=jsonResult.getStatus();
                        User user=jsonResult.getData();
                        //Log.v(TAG,data);
                        if(TextUtils.equals(status, Result.SUCCESS)&&user!=null){
                            PersistentCookieStore myCookieStore = new PersistentCookieStore(getApplicationContext());
                            HttpUtils.client.setCookieStore(myCookieStore);
                            List<Cookie> cookies = myCookieStore.getCookies();
                            String JSESSIONID = "";
                            if (cookies.isEmpty()) {
                                Log.i("session", "None");
                            } else {
                                for (int i = 0; i < cookies.size(); i++) {
                                    if("JSESSIONID".equals(cookies.get(i).getName())){
                                        JSESSIONID = cookies.get(i).getValue();  // 第二种方法 通过 JSESSIONID
                                        System.out.println(JSESSIONID);
                                        break;
                                    }
                                }
                                PrefUtils.set("user","session",JSESSIONID,getBaseContext());
                                Log.i("session", "保存"+JSESSIONID);
                            }
                            BasicClientCookie newCookie = new BasicClientCookie("userId",user.getId());
                            newCookie.setVersion(1);
                            newCookie.setDomain("114.215.135.153");
                            newCookie.setPath("/");
                            myCookieStore.addCookie(newCookie);
                            //保存用户id到本地
                            PrefUtils.set("user","userId",user.getId(),getBaseContext());
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("userInfo", user);
                            startActivity(intent);
                            LoginActivity.this.finish();
                            showProgress(false);
                        }else{
                            Toast.makeText(LoginActivity.this, "手机号或密码错误", Toast.LENGTH_SHORT).show();
                            showProgress(false);
                        }
                    }
                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Toast.makeText(LoginActivity.this, "网络出现问题了" + new String(responseBody), Toast.LENGTH_LONG).show();
                        showProgress(false);
                    }
                });
            } else {
                Toast.makeText(LoginActivity.this, "没有网络连接!", Toast.LENGTH_LONG).show();

            }
        }
    }
    private void attemptRegister() {
        // Reset errors.
        mPhoneView.setError(null);
        mPasswordView.setError(null);
        mNicknameView.setError(null);
        mPasswordAgainView.setError(null);

        // Store values at the time of the login attempt.
        String phone = mPhoneView.getText().toString();
        String password = mPasswordView.getText().toString();
        String nickname=mNicknameView.getText().toString();
        String passwordAgain=mPasswordAgainView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(phone)) {
            mPhoneView.setError(getString(R.string.error_field_required));
            focusView = mPhoneView;
            cancel = true;
        } else if (!isPhoneValid(phone)) {
            mPhoneView.setError(getString(R.string.error_invalid_email));
            focusView = mPasswordView;
            cancel = true;
        }
        // Check for a valid email address.
        if (TextUtils.isEmpty(nickname)) {
            mNicknameView.setError("昵称不能为空");
            focusView = mNicknameView;
            cancel = true;
        } else if (!isNicknameValid(nickname)) {
            mNicknameView.setError("昵称太短了");
            focusView = mNicknameView;
            cancel = true;
        }
        // Check for a valid email address.
        if (TextUtils.isEmpty(passwordAgain)) {
            mPasswordAgainView.setError("重复密码不能为空");
            focusView = mPasswordAgainView;
            cancel = true;
        } else if (!isPasswordAgainValid(passwordAgain,password)) {
            mPasswordAgainView.setError("两次密码不一致");
            focusView = mPasswordAgainView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            if (HttpUtils.isNetworkConnected(LoginActivity.this)) {
                //封装请求参数
                RequestParams requestParams=new RequestParams();
                requestParams.put("phone",phone);
                requestParams.put("password",password);
                requestParams.put("nickname",nickname);
                requestParams.put("passwordAgain",passwordAgain);
                HttpUtils.post(Constant.REGISTER, requestParams, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Gson gson=new Gson();
                        Type type = new TypeToken<JsonResult<User>>(){}.getType();
                        JsonResult<User> jsonResult=gson.fromJson(new String(responseBody),type);
                        String status=jsonResult.getStatus();
                        User user=jsonResult.getData();
                        //Log.v(TAG,data);
                        if(TextUtils.equals(status, Result.SUCCESS)&&user!=null){
                            Toast.makeText(LoginActivity.this,"注册成功",Toast.LENGTH_LONG).show();
                            showProgress(false);
                            mSignInButton.setText("登录");
                            mSwitchButton.setText("还没有帐号?");
                            isRegiterAction=false;
                            mNicknameView.setVisibility(View.GONE);
                            mPasswordAgainView.setVisibility(View.GONE);
                            //mPhoneView.setText("");
                            mPasswordView.setText("");

                            PersistentCookieStore myCookieStore = new PersistentCookieStore(getApplicationContext());
                            HttpUtils.client.setCookieStore(myCookieStore);
                            List<Cookie> cookies = myCookieStore.getCookies();
                            String JSESSIONID = "";
                            if (cookies.isEmpty()) {
                                Log.i("session", "None");
                            } else {
                                for (int i = 0; i < cookies.size(); i++) {
                                    if ("JSESSIONID".equals(cookies.get(i).getName())) {
                                        JSESSIONID = cookies.get(i).getValue();  // 第二种方法 通过 JSESSIONID
                                        System.out.println(JSESSIONID);
                                        break;
                                    }
                                }
                                PrefUtils.set("user", "session", JSESSIONID, getBaseContext());
                                Log.i("session", "保存" + JSESSIONID);
                            }
                            BasicClientCookie newCookie = new BasicClientCookie("userId", user.getId());
                            newCookie.setVersion(1);
                            newCookie.setDomain("114.215.135.153");
                            newCookie.setPath("/");
                            myCookieStore.addCookie(newCookie);

                        }else{
                            Toast.makeText(LoginActivity.this, "手机号已注册", Toast.LENGTH_LONG).show();
                            showProgress(false);
                        }

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Toast.makeText(LoginActivity.this, "网络出现问题了", Toast.LENGTH_LONG).show();
                        showProgress(false);
                    }
                });
            } else {
                Toast.makeText(LoginActivity.this, "没有网络连接!", Toast.LENGTH_LONG).show();

            }
        }
    }
    private boolean isPhoneValid(String phone) {
        //TODO: Replace this with your own logic
        return phone.length()==11;
    }
    private boolean isNicknameValid(String nickname) {
        //TODO: Replace this with your own logic
        return nickname.length()>2;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 5;
    }

    private boolean isPasswordAgainValid(String passwordAgain,String password) {
        //TODO: Replace this with your own logic
        return passwordAgain.equals(password);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        //addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };
        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }
}

