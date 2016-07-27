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
import android.widget.TextView;
import android.widget.Toast;

import com.foodie.app.Entity.JsonListResult;
import com.foodie.app.Entity.Result;
import com.foodie.app.R;
import com.foodie.app.model.WorksInfo;
import com.foodie.app.util.HttpUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.lang.reflect.Type;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class MyMessageFragment extends Fragment {
    protected static final String TAG = "MessageFragment";
    private RecyclerView worksListView;
    private List<WorksInfo> worksInfos;
    //private WorksListAdapter worksListAdapter;
    private SwipeRefreshLayout sr;
    private TextView noDataView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = (View) inflater.inflate(
                R.layout.fragment_message_me, container, false);
        //设置Item增加、移除动画
        worksListView = (RecyclerView) view.findViewById(R.id.recyclerview);
        worksListView.setItemAnimator(new DefaultItemAnimator());
        sr = (SwipeRefreshLayout) view.findViewById(R.id.sr);
        noDataView = (TextView) view.findViewById(R.id.no_data);
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
    }

    private void updateWorksInfo() {
        if (HttpUtils.isNetworkConnected(getActivity())) {
            HttpUtils.getWithAuth(getContext(), "works/me/", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    Gson gson = new Gson();
                    //Type type = new TypeToken<JsonResult<User>>(){}.getType();
                    Type type = new TypeToken<JsonListResult<WorksInfo>>() {
                    }.getType();
                    JsonListResult<WorksInfo> jsonListResult = gson.fromJson(new String(responseBody), type);
                    String status = jsonListResult.getStatus();
                    worksInfos = jsonListResult.getData();
                    //Log.v(TAG,data);
                    if (TextUtils.equals(status, Result.SUCCESS) && worksInfos.size() > 0) {
                        //Toast.makeText(getActivity(),"刷新成功"+worksInfos.size(),Toast.LENGTH_LONG).show();
                        Log.i(TAG, new String(responseBody));
                        worksListView.setAdapter(new MessageListAdapter(getActivity().getApplicationContext(), worksInfos));
                        return;
                    } else if (TextUtils.equals(status, Result.SUCCESS) && worksInfos.size() == 0) {
                        noDataView.setVisibility(View.VISIBLE);
                        //Toast.makeText(getActivity(),"你还没有发表过动态",Toast.LENGTH_LONG).show();
                    } else {
                        //Toast.makeText(getActivity(),"获取动态信息失败",Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Toast.makeText(getActivity(), "请检查网络!", Toast.LENGTH_LONG).show();
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
        //private boolean isUserLogin=getUserState();
        private MyItemClickListener myItemClickListener = new MyItemClickListener() {
            @Override
            public void onItemClick(View view, int position, int type) {
                switch (type) {
                    case 1:
                        TextView v = (TextView) view;
                        //v.setText();
                        break;
                }
            }
        };


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
            public int mViewType;
            public View mView;
            public TextView mPostTimeView;
            public ImageView mWorksImageView;
            public TextView mIntroductionView;
            public MyItemClickListener mOnClickListener;

            public ViewHolder(View view, int viewType, MyItemClickListener listener) {
                super(view);
                mView = view;
                mViewType = viewType;
                if (viewType == IS_HEADER) {
                    return;
                }
                mPostTimeView = (TextView) view.findViewById(R.id.tv_post_time);
                mWorksImageView = (ImageView) view.findViewById(R.id.iv_works_image);
                mIntroductionView = (TextView) view.findViewById(R.id.tv_introduction);
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
            this.context = context;
            this.worksList = works;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


            //RecyclerViewHolder holder;
            //对不同的flag创建不同的Holder
            if (viewType == IS_HEADER) {
                View view = LayoutInflater.from(context).inflate(R.layout.message_header, parent, false);
                return new ViewHolder(view, IS_HEADER, myItemClickListener);
            } else if (viewType == IS_NORMAL) {
                View view = LayoutInflater.from(context)
                        .inflate(R.layout.list_item_works_me, parent, false);
                return new ViewHolder(view, IS_NORMAL, myItemClickListener);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
//            //显示作品图片的配置
//            if(holder.mViewType==IS_HEADER){
//                return;
//            }else if (holder.mViewType==IS_NORMAL){
//                position=position-1;
//            }
            DisplayImageOptions worksImageOptions = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.loading_large)
                    .showImageOnFail(R.drawable.recipe)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();
            ImageLoader.getInstance().displayImage(worksList.get(position).getThumbnail(), holder.mWorksImageView, worksImageOptions);

            holder.mIntroductionView.setText(worksList.get(position).getIntroduction());
            holder.mPostTimeView.setText(worksList.get(position).getCreateTime());
        }

        @Override
        public int getItemCount() {
            return worksList.size();
        }

        @Override
        public int getItemViewType(int position) {
            return IS_NORMAL;
        }
    }

}
