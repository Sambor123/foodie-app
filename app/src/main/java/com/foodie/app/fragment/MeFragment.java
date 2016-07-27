/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.foodie.app.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.foodie.app.Activity.LoginActivity;
import com.foodie.app.Activity.ModifyUserActivity;
import com.foodie.app.Activity.SettingActivity;
import com.foodie.app.Activity.TabActivity;
import com.foodie.app.Entity.JsonResult;
import com.foodie.app.R;
import com.foodie.app.model.User;
import com.foodie.app.util.HttpUtils;
import com.foodie.app.util.PrefUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.makeramen.roundedimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.lang.reflect.Type;

import cz.msebera.android.httpclient.Header;


public class MeFragment extends Fragment {

    private User user=null;
    private Button mLoginButton;
    private TextView mNicknameView;
    private TextView mPhoneView;
    private Boolean isUserLogin=false;
    private RoundedImageView mUserAvatorView;
    private RoundedImageView mUserDefaultView;
    private RelativeLayout mSettingView;

    private TextView mFansView;
    private TextView mFollowView;

    private RelativeLayout mUserLoginView;
    private RelativeLayout mUserNotLoginView;

    private RelativeLayout mWorksView;
    private RelativeLayout mCollecttionView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_me, container, false);
        mLoginButton = (Button) view.findViewById(R.id.btn_login_register);
        mNicknameView=(TextView)view.findViewById(R.id.me_nickname);
        mUserLoginView= (RelativeLayout) view.findViewById(R.id.user_login);
        mUserNotLoginView= (RelativeLayout) view.findViewById(R.id.user_not_login);
        mPhoneView= (TextView) view.findViewById(R.id.me_phone);
        mUserAvatorView= (RoundedImageView) view.findViewById(R.id.user_image);
        mUserDefaultView= (RoundedImageView) view.findViewById(R.id.default_user_image);
        mSettingView = (RelativeLayout) view.findViewById(R.id.view_setting);
        mFansView = (TextView) view.findViewById(R.id.me_fans_count);
        mFollowView = (TextView) view.findViewById(R.id.me_follow_count);
        mWorksView = (RelativeLayout) view.findViewById(R.id.view_my_work);
        mCollecttionView = (RelativeLayout) view.findViewById(R.id.view_favorite);
        return view;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent loginIntent=new Intent(getActivity(), LoginActivity.class);
                startActivityForResult(loginIntent,1);
            }
        });
        mUserDefaultView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent=new Intent(getActivity(), LoginActivity.class);
                startActivityForResult(loginIntent,1);
            }
        });
        mUserAvatorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modifyUserInfo();
            }
        });
        mNicknameView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modifyUserInfo();
            }
        });
        mSettingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(), SettingActivity.class);
                startActivity(intent);
            }
        });
        mFansView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), TabActivity.class);
                intent.putExtra("currentTab", 3);
                startActivity(intent);
            }
        });
        mFollowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), TabActivity.class);
                intent.putExtra("currentTab", 2);
                startActivity(intent);
            }
        });
        mWorksView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), TabActivity.class);
                intent.putExtra("currentTab", 1);
                startActivity(intent);
            }
        });
        mCollecttionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), TabActivity.class);
                intent.putExtra("currentTab", 0);
                startActivity(intent);
            }
        });
        String userId=PrefUtils.get("user","userId",getActivity());
        if(userId!=null){
            isUserLogin=true;
            updateUserInfo(userId);
        }
    }

    private void modifyUserInfo() {
        //Toast.makeText(getActivity(),"修改资料功能",Toast.LENGTH_LONG).show();
        Intent intent = new Intent(getActivity(), ModifyUserActivity.class);
        intent.putExtra("myInfo", user);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch(requestCode){
            case 1:
                boolean result = intent.getExtras().getBoolean("result");
                if (result==true){
                    String userId=PrefUtils.get("user","userId",getActivity());
                    if(userId!=null){
                        isUserLogin=true;
                        updateUserInfo(userId);
                    }
                }
                break;
            case 2:
                //来自按钮2的请求，作相应业务处理
        }

        //Log.i("1", result);
    }

    private void updateUserInfo(String userId){
        if (HttpUtils.isNetworkConnected(getActivity())) {
            //Toast.makeText(getActivity(),"sharedPref里面的用户ID"+userId,Toast.LENGTH_LONG).show();
            if(userId!=null&&!TextUtils.equals(userId,"")){
                HttpUtils.get("user/"+userId, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Gson gson=new Gson();
                        Type type = new TypeToken<JsonResult<User>>(){}.getType();
                        JsonResult<User> jsonResult=gson.fromJson(new String(responseBody),type);
                        //String status=jsonResult.getStatus();
                        user=jsonResult.getData();
                        isUserLogin=true;
                        mUserNotLoginView.setVisibility(View.GONE);
                        mUserLoginView.setVisibility(View.VISIBLE);
                        mNicknameView.setText(user.getNickname());
                        mPhoneView.setText(user.getPhone());
                        //显示头像图片的配置
                        DisplayImageOptions userImageOptions = new DisplayImageOptions.Builder()
                                .showImageOnLoading(R.drawable.loading_small)
                                .showImageOnFail(R.drawable.user)
                                .cacheInMemory(true)
                                .cacheOnDisk(true)
                                .bitmapConfig(Bitmap.Config.RGB_565)
                                .build();

                        ImageLoader.getInstance().displayImage(user.getAvator(), mUserAvatorView, userImageOptions);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Toast.makeText(getActivity(),"服务器挂了"+new String(responseBody),Toast.LENGTH_LONG).show();
                    }
                });
                //获取粉丝数
                HttpUtils.getWithAuth(getActivity().getApplicationContext(), "user/fanscount/", new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Gson gson = new Gson();
                        Type type = new TypeToken<JsonResult<Integer>>() {
                        }.getType();
                        JsonResult<Integer> jsonResult = gson.fromJson(new String(responseBody), type);
                        String status = jsonResult.getStatus();
                        int fansCount = 0;
                        if (jsonResult.getData() != null) {
                            fansCount = jsonResult.getData();
                        }
                        mFansView.setText(fansCount + "");
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Toast.makeText(getActivity(), "请检查网络" + new String(responseBody), Toast.LENGTH_LONG).show();
                    }
                });
                //获取关注数
                HttpUtils.getWithAuth(getActivity().getApplicationContext(), "user/followcount/", new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Gson gson = new Gson();
                        Type type = new TypeToken<JsonResult<Integer>>() {
                        }.getType();
                        JsonResult<Integer> jsonResult = gson.fromJson(new String(responseBody), type);
                        String status = jsonResult.getStatus();
                        int followCount = 0;
                        if (jsonResult.getData() != null) {
                            followCount = jsonResult.getData();
                        }
                        mFollowView.setText(followCount + "");
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Toast.makeText(getActivity(), "服务器挂了" + new String(responseBody), Toast.LENGTH_LONG).show();
                    }
                });
            }else{
                Toast.makeText(getActivity(),"请登录或注册",Toast.LENGTH_LONG).show();
                startActivity(new Intent(getActivity(), LoginActivity.class));
            }

        }else{
            Toast.makeText(getActivity(), "没有网络连接!", Toast.LENGTH_LONG).show();
        }
    }
}
