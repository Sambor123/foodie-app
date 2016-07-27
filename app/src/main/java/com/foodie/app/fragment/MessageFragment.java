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

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.foodie.app.Activity.LoginActivity;
import com.foodie.app.Activity.PostActivity;
import com.foodie.app.Entity.JsonListResult;
import com.foodie.app.Entity.JsonResult;
import com.foodie.app.Entity.Result;
import com.foodie.app.R;
import com.foodie.app.model.Fans;
import com.foodie.app.model.User;
import com.foodie.app.model.WorksInfo;
import com.foodie.app.util.Constant;
import com.foodie.app.util.HttpUtils;
import com.foodie.app.util.PrefUtils;
import com.github.clans.fab.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.makeramen.roundedimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.lang.reflect.Type;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;

public class MessageFragment extends Fragment {
    protected static final String TAG="MessageFragment";
    private RecyclerView worksListView;
    private List<WorksInfo> worksInfos;
    //private WorksListAdapter worksListAdapter;
    private SwipeRefreshLayout sr;
    private FloatingActionButton fab;

    private User user;
    private String userId;

    private ProgressBar progressBar;

    //private boolean isUserValid=true;//用户登录状态是否有效,若无效则退出
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = (View) inflater.inflate(
                R.layout.fragment_message, container, false);
        //设置Item增加、移除动画
        worksListView= (RecyclerView) view.findViewById(R.id.recyclerview);
        worksListView.setItemAnimator(new DefaultItemAnimator());
        sr = (SwipeRefreshLayout) view.findViewById(R.id.sr);
        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        return view;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        worksListView.setLayoutManager(new LinearLayoutManager(worksListView.getContext()));
        updateWorksInfo();
        sr.setColorSchemeResources(
                R.color.colorPrimary,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        sr.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateWorksInfo();
                sr.setRefreshing(false);
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), PostActivity.class));
            }
        });
    }

    private void updateWorksInfo() {
        if (HttpUtils.isNetworkConnected(getActivity())) {
            HttpUtils.get(Constant.WORKS_LIST, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    Gson gson=new Gson();
                    //Type type = new TypeToken<JsonResult<User>>(){}.getType();
                    Type type = new TypeToken<JsonListResult<WorksInfo>>(){}.getType();
                    JsonListResult<WorksInfo> jsonListResult=gson.fromJson(new String(responseBody),type);
                    String status=jsonListResult.getStatus();
                    worksInfos=jsonListResult.getData();
                    //Log.v(TAG,data);
                    if(TextUtils.equals(status, Result.SUCCESS)&&worksInfos!=null){
                        //Toast.makeText(getActivity(),"刷新成功"+worksInfos.size(),Toast.LENGTH_LONG).show();
                        Log.i(TAG,new String(responseBody));
                        progressBar.setVisibility(View.GONE);
                        worksListView.setAdapter(new MessageListAdapter(getActivity(),worksInfos));
                        return;
                    }else{
                        Toast.makeText(getActivity(), "获取动态信息失败" + new String(responseBody), Toast.LENGTH_LONG).show();
                    }
                }
                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Toast.makeText(getActivity(), "网络异常", Toast.LENGTH_LONG).show();
                }
            });

        } else {
            Toast.makeText(getActivity(), "没有网络连接!", Toast.LENGTH_LONG).show();
        }
    }

    public static class MessageListAdapter
            extends RecyclerView.Adapter<MessageListAdapter.ViewHolder> {
        private List<WorksInfo> worksList;
        private Context context;
        private static final int IS_HEADER = 2;
        private static final int IS_NORMAL = 1;

        private User user;
        //private boolean isUserLogin=getUserState();
        private MyItemClickListener myItemClickListener = new MyItemClickListener() {
            @Override
            public void onItemClick(View view, int position, int type) {
                switch (type) {
                    case 1:
                        TextView v = (TextView) view;
                        toggleFollow(v, position);
                        break;
                }
            }
        };

        private void toggleFollow(final TextView tv, int position) {
            if (HttpUtils.isNetworkConnected(context)) {
                HttpUtils.postWithAuth(context, "user/" + worksList.get(position).getUserId() + "/togglefollow/", new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Gson gson = new Gson();
                        //Type type = new TypeToken<JsonResult<User>>(){}.getType();
                        Type type = new TypeToken<JsonResult<Fans>>() {
                        }.getType();
                        JsonResult<Fans> jsonResult = gson.fromJson(new String(responseBody), type);
                        String status = jsonResult.getStatus();
                        String tipCode = jsonResult.getTipCode();
                        //Log.v(TAG,data);
                        Log.i(TAG, "获取状态成功" + new String(responseBody));
                        if (TextUtils.equals(status, Result.SUCCESS) && TextUtils.equals(tipCode, "follow")) {
                            Log.i(TAG, "关注该用户成功");
                            tv.setText("已关注");
                            return;
                        } else if (TextUtils.equals(status, Result.SUCCESS) && TextUtils.equals(tipCode, "unFollow")) {
                            Log.i(TAG, "取消关注该用户成功");
                            tv.setText("关注");
                            return;
                        } else if (TextUtils.equals(status, Result.FAILED) && TextUtils.equals(tipCode, "followFail")) {
                            Log.i(TAG, "关注失败");
                        } else if (TextUtils.equals(status, Result.FAILED) && TextUtils.equals(tipCode, "unFollowFail")) {
                            Log.i(TAG, "取消失败");
                        } else if (TextUtils.equals(status, Result.FAILED) && TextUtils.equals(tipCode, "notLogin")) {
                            Log.i(TAG, "客户端登录,服务端session失效");
                            Toast.makeText(context, "登录状态失效,请重新登录", Toast.LENGTH_LONG).show();
                            //context.startActivity(new Intent(MessageFragment.g, LoginActivity.class));
                            context.startActivity(new Intent(context, LoginActivity.class));
                        } else if (TextUtils.equals(status, Result.FAILED) && TextUtils.equals(tipCode, "userNotExist")) {
                            Log.i(TAG, "用户不存在");
                            Toast.makeText(context, "用户不存在", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Toast.makeText(context, "获取收藏数据失败", Toast.LENGTH_LONG).show();
                        Log.i(TAG, "获取关注状态失败" + new String(responseBody));
                    }
                });
            } else {
                Toast.makeText(context, "请检查网络连接!", Toast.LENGTH_LONG).show();
            }
        }

        public interface MyItemClickListener {
            void onItemClick(View view, int postion, int type);
        }

        public void setMyItemClickListener(MyItemClickListener listener) {
            myItemClickListener = listener;
        }
        public void setWorks(List<WorksInfo> works) {
            this.worksList = works;
        }

        public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public  int mViewType;
            public  View mView;
            public  CircleImageView mAvator;
            public  TextView mNicknameView;
            public  TextView mPostTimeView;
            public  ImageView mWorksImageView;
            public  TextView mIntroductionView;
            public TextView mFollowView;
            public MyItemClickListener mOnClickListener;

            public RoundedImageView userAvatorView;
            public TextView userNameView;

            public ViewHolder(View view, int viewType, MyItemClickListener listener) {
                super(view);
                mView = view;
                mViewType=viewType;
                if(viewType==IS_HEADER){
                    userAvatorView = (RoundedImageView) view.findViewById(R.id.user_image);
                    userNameView = (TextView) view.findViewById(R.id.user_name_text);
                    return;
                }
                mAvator= (CircleImageView) view.findViewById(R.id.avatar);
                mNicknameView= (TextView) view.findViewById(R.id.tv_nickname);
                mPostTimeView= (TextView) view.findViewById(R.id.tv_post_time);
                mWorksImageView= (ImageView) view.findViewById(R.id.iv_works_image);
                mIntroductionView= (TextView) view.findViewById(R.id.tv_introduction);
                mFollowView = (TextView) view.findViewById(R.id.follow_tv);
                mFollowView.setOnClickListener(this);
                mOnClickListener = listener;
                //init();
            }
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.follow_tv:
                        mOnClickListener.onItemClick(v, getPosition(), 1);
                }
            }
        }

        public MessageListAdapter(Context context, List<WorksInfo> works) {
            super();
            this.context=context;
            this.worksList=works;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


            //RecyclerViewHolder holder;
            //对不同的flag创建不同的Holder
            if (viewType == IS_HEADER) {
                View view = LayoutInflater.from(context).inflate(R.layout.message_header, parent, false);
                return new ViewHolder(view, IS_HEADER, myItemClickListener);
            } else if(viewType==IS_NORMAL){
                View view = LayoutInflater.from(context)
                        .inflate(R.layout.list_item_works, parent, false);
                return new ViewHolder(view, IS_NORMAL, myItemClickListener);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            //显示作品图片的配置
            if(holder.mViewType==IS_HEADER){
                String userId = PrefUtils.get("user", "userId", context.getApplicationContext());
                HttpUtils.get("user/" + userId, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Gson gson = new Gson();
                        Type type = new TypeToken<JsonResult<User>>() {
                        }.getType();
                        JsonResult<User> jsonResult = gson.fromJson(new String(responseBody), type);
                        //String status=jsonResult.getStatus();
                        user = jsonResult.getData();
                        //显示头像图片的配置
                        DisplayImageOptions userImageOptions = new DisplayImageOptions.Builder()
                                .showImageOnLoading(R.drawable.loading_small)
                                .showImageOnFail(R.drawable.user)
                                .cacheInMemory(true)
                                .cacheOnDisk(true)
                                .bitmapConfig(Bitmap.Config.RGB_565)
                                .build();

                        ImageLoader.getInstance().displayImage(user.getAvator(), holder.userAvatorView, userImageOptions);
                        holder.userNameView.setText(user.getNickname());
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Toast.makeText(context, "服务器挂了" + new String(responseBody), Toast.LENGTH_LONG).show();
                    }
                });
                return;
            }else if (holder.mViewType==IS_NORMAL){
                position=position-1;
            }
            DisplayImageOptions worksImageOptions = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.loading_large)
                    .showImageOnFail(R.drawable.recipe)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();
            ImageLoader.getInstance().displayImage(worksList.get(position).getThumbnail(), holder.mWorksImageView, worksImageOptions);
            //显示头像图片的配置
            DisplayImageOptions avatorOptions = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.loading_small)
                    .showImageOnFail(R.drawable.user)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();

            ImageLoader.getInstance().displayImage(worksList.get(position).getAvator(), holder.mAvator, avatorOptions);

            holder.mNicknameView.setText(worksList.get(position).getNickname());
            holder.mIntroductionView.setText(worksList.get(position).getIntroduction());
            holder.mPostTimeView.setText(worksList.get(position).getCreateTime());
            boolean isFollow = true;

            if (HttpUtils.isNetworkConnected(context)) {
                HttpUtils.getWithAuth(context, "user/" + worksList.get(position).getUserId() + "/isfollow/", new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Gson gson = new Gson();
                        //Type type = new TypeToken<JsonResult<User>>(){}.getType();
                        Type type = new TypeToken<JsonResult<Fans>>() {
                        }.getType();
                        JsonResult<Fans> jsonResult = gson.fromJson(new String(responseBody), type);
                        String status = jsonResult.getStatus();
                        String tipCode = jsonResult.getTipCode();
                        //Log.v(TAG,data);
                        Log.i(TAG, "获取关注状态成功" + new String(responseBody));
                        if (TextUtils.equals(status, Result.SUCCESS) && TextUtils.equals(tipCode, "follow")) {
                            Log.i(TAG, "已关注了该用户");
                            holder.mFollowView.setText("已关注");
                            return;
                        } else if (TextUtils.equals(status, Result.SUCCESS) && TextUtils.equals(tipCode, "notFollow")) {
                            Log.i(TAG, "未关注该用户");
                            holder.mFollowView.setText("关注");
                            return;
                        } else if (TextUtils.equals(status, Result.FAILED) && TextUtils.equals(tipCode, "notLogin")) {
                            Log.i(TAG, "客户端登录,服务端session失效");
                            Toast.makeText(context, "登录状态失效,请重新登录", Toast.LENGTH_LONG).show();
                            context.startActivity(new Intent(context, LoginActivity.class));
                        } else if (TextUtils.equals(status, Result.FAILED) && TextUtils.equals(tipCode, "notExist")) {
                            Log.i(TAG, "用户不存在");
                            Toast.makeText(context, "用户不存在", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Toast.makeText(context, "获取收藏数据失败", Toast.LENGTH_LONG).show();
                        Log.i(TAG, "获取关注状态失败" + new String(responseBody));
                    }
                });
            } else {
                Toast.makeText(context, "请检查网络连接!", Toast.LENGTH_LONG).show();
            }
        }
        @Override
        public int getItemCount(){
            return worksList.size()+1;
        }
        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return IS_HEADER;
            }else{
                return IS_NORMAL;
            }
        }
    }
}
