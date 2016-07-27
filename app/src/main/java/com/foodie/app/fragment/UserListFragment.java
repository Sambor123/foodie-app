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
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.foodie.app.Entity.JsonListResult;
import com.foodie.app.Entity.Result;
import com.foodie.app.R;
import com.foodie.app.model.User;
import com.foodie.app.util.HttpUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.lang.reflect.Type;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class UserListFragment extends Fragment {

    private List<User> users;
    private String flag;

    private RecyclerView rv;
    private static final String FLAG = "allfans";
    private static final String ARG_BOOK = "book";

    private TextView noDataView;

    public static UserListFragment newInstance(String flag) {
        UserListFragment fragment = new UserListFragment();
        Bundle args = new Bundle();
        args.putString(FLAG, flag);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if (getArguments().containsKey(FLAG)) {
                flag = getArguments().getString(FLAG);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_list, container, false);

        rv = (RecyclerView) view.findViewById(R.id.recyclerview);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        noDataView = (TextView) view.findViewById(R.id.no_data);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        updateUsers();
    }


    private void updateUsers() {
        if (HttpUtils.isNetworkConnected(getActivity())) {
            String url;
            if (flag != null) {
                url = flag.equals("allfans") ? "user/allfans/" : "user/allfollow/";
            } else {
                return;
            }
            HttpUtils.getWithAuth(getContext(), url, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    Gson gson = new Gson();
                    //Type type = new TypeToken<JsonResult<User>>(){}.getType();
                    Type type = new TypeToken<JsonListResult<User>>() {
                    }.getType();
                    JsonListResult<User> jsonListResult = gson.fromJson(new String(responseBody), type);
                    String status = jsonListResult.getStatus();
                    String tipCode = jsonListResult.getTipCode();
                    users = jsonListResult.getData();
                    //Log.v(TAG,data);
                    if (TextUtils.equals(status, Result.SUCCESS) && users.size() > 0) {
                        //Toast.makeText(getActivity(),"刷新成功"+worksInfos.size(),Toast.LENGTH_LONG).show();
                        Log.i("UserListFragment", new String(responseBody));
                        rv.setAdapter(new UserListAdapter(getContext(), users));
                        return;
                    } else if (TextUtils.equals(status, Result.SUCCESS) && users.size() == 0) {
                        noDataView.setVisibility(View.VISIBLE);
                        return;
                    } else if (TextUtils.equals(status, Result.FAILED) && TextUtils.equals(tipCode, "null")) {
                        Log.i("UserListFragment", new String(responseBody));
                    } else {
                        //Toast.makeText(getActivity(),"获取信息失败",Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Toast.makeText(getActivity(), "请检查网络链接", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Toast.makeText(getActivity(), "没有网络连接!", Toast.LENGTH_LONG).show();
        }
    }


    public static class UserListAdapter
            extends RecyclerView.Adapter<UserListAdapter.ViewHolder> {

        private final TypedValue mTypedValue = new TypedValue();
        private int mBackground;

        private Context context;
        private List<User> users;

        public static class ViewHolder extends RecyclerView.ViewHolder {

            public final View mView;
            public final ImageView mImageView;
            public final TextView mTextView;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mImageView = (ImageView) view.findViewById(R.id.avatar);
                mTextView = (TextView) view.findViewById(R.id.nickname);
            }

        }

        public UserListAdapter(Context context, List<User> users) {
            context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
            mBackground = mTypedValue.resourceId;
            this.context = context;
            this.users = users;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item, parent, false);
            view.setBackgroundResource(mBackground);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final User user = users.get(position);
            holder.mTextView.setText(user.getNickname());
            //显示头像图片的配置
            DisplayImageOptions avatorOptions = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.loading_small)
                    .showImageOnFail(R.drawable.user)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();

            ImageLoader.getInstance().displayImage(user.getAvator(), holder.mImageView, avatorOptions);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(context,user.toString(),Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return users.size();
        }
    }
}
