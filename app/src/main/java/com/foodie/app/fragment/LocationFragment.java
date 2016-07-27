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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.foodie.app.Activity.MapActivity;
import com.foodie.app.Entity.JsonListResult;
import com.foodie.app.Entity.Result;
import com.foodie.app.R;
import com.foodie.app.model.RestaurantInfo;
import com.foodie.app.util.Constant;
import com.foodie.app.util.HttpUtils;
import com.foodie.app.util.PrefUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.lang.reflect.Type;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class LocationFragment extends Fragment {
    protected static final String TAG="MessageFragment";
    private RecyclerView restaurantListView;
    private List<RestaurantInfo> restaurantList;

    private RestaurantListAdapter restaurantListAdapter;

    //定位组件
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();
    public String mLocation;

    public TextView mLocationView;

    private SwipeRefreshLayout sr;
    private ProgressBar progressBar;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout view = (LinearLayout) inflater.inflate(
                R.layout.fragment_location, container, false);
        restaurantListView= (RecyclerView) view.findViewById(R.id.recyclerview);
        restaurantListView.setItemAnimator(new DefaultItemAnimator());
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        sr = (SwipeRefreshLayout) view.findViewById(R.id.sr);
        return view;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        restaurantListView.setLayoutManager(new LinearLayoutManager(restaurantListView.getContext()));

        //定位
        //定位核心类
        mLocationClient = new LocationClient(getActivity().getApplicationContext());     //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);    //注册监听函数
        initLocation();//初始化
        mLocationClient.start();
        //更新列表
        updateRestaurantInfo();
        sr.setColorSchemeResources(
                R.color.colorPrimary,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        sr.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateRestaurantInfo();
                sr.setRefreshing(false);
            }
        });

    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = 2000;
        option.setScanSpan(0);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            mLocation = location.getAddrStr();
            PrefUtils.set("user", "location", mLocation, getActivity().getBaseContext());
            //Receive Location
            StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());
            if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());// 单位：公里每小时
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
                sb.append("\nheight : ");
                sb.append(location.getAltitude());// 单位：米
                sb.append("\ndirection : ");
                sb.append(location.getDirection());// 单位度
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                sb.append("\ndescribe : ");
                sb.append("gps定位成功");
            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                //运营商信息
                sb.append("\noperationers : ");
                sb.append(location.getOperators());
                sb.append("\ndescribe : ");
                sb.append("网络定位成功");
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                sb.append("\ndescribe : ");
                sb.append("离线定位成功，离线定位结果也是有效的");
            } else if (location.getLocType() == BDLocation.TypeServerError) {
                sb.append("\ndescribe : ");
                sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                sb.append("\ndescribe : ");
                sb.append("网络不同导致定位失败，请检查网络是否通畅");
            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                sb.append("\ndescribe : ");
                sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
            }
            sb.append("\nlocationdescribe : ");
            sb.append(location.getLocationDescribe());// 位置语义化信息
            List<Poi> list = location.getPoiList();// POI数据
            if (list != null) {
                sb.append("\npoilist size = : ");
                sb.append(list.size());
                for (Poi p : list) {
                    sb.append("\npoi= : ");
                    sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
                }
            }
            Log.i("BaiduLocationApiDem", sb.toString());
        }
    }

    private void updateRestaurantInfo() {
        if (HttpUtils.isNetworkConnected(getActivity())) {
            HttpUtils.get(Constant.RESTAURANT_LIST, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    Gson gson=new Gson();
                    //Type type = new TypeToken<JsonResult<User>>(){}.getType();
                    Type type = new TypeToken<JsonListResult<RestaurantInfo>>(){}.getType();
                    JsonListResult<RestaurantInfo> jsonListResult=gson.fromJson(new String(responseBody),type);
                    String status=jsonListResult.getStatus();
                    restaurantList=jsonListResult.getData();
                    //Log.v(TAG,data);
                    if(TextUtils.equals(status, Result.SUCCESS)&&restaurantList!=null){
                        //Toast.makeText(getActivity(),"刷新成功"+restaurantList.size(),Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                        Log.i(TAG,new String(responseBody));
                        restaurantListAdapter = new RestaurantListAdapter(getActivity(), restaurantList, mLocation);
                        restaurantListAdapter.setMyItemClickListener(new RestaurantListAdapter.MyItemClickListener() {
                            @Override
                            public void onItemClick(View view, int postion) {
                                if (postion == 0) {
                                    Log.v(TAG, "点击了第一个");
                                    getActivity().startActivity(new Intent(getActivity(), MapActivity.class));
                                    return;
                                }
                                Log.v(TAG, "点击了不是第一个");
                                Intent intent = new Intent(getActivity(), MapActivity.class);
                                intent.putExtra("restaurantInfo",restaurantListAdapter.getRestaurantInfo(postion));
                                getActivity().startActivity(intent);
                            }
                        });
                        restaurantListView.setAdapter(restaurantListAdapter);
                        return;
                    }else{
                        Toast.makeText(getActivity(),"获取商家信息失败"+new String(responseBody),Toast.LENGTH_LONG).show();
                    }

                }
                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Toast.makeText(getActivity(),"服务器挂了",Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Toast.makeText(getActivity(), "没有网络连接!", Toast.LENGTH_LONG).show();
        }
    }


    public static class RestaurantListAdapter
            extends RecyclerView.Adapter<RestaurantListAdapter.ViewHolder>{
        private List<RestaurantInfo> restaurantList;
        private String mLocation = "正在获取你的位置...";
        private Context context;
        private static final int IS_HEADER = 2;
        private static final int IS_NORMAL = 1;

        private MyItemClickListener myItemClickListener;

        public void setMyItemClickListener(MyItemClickListener listener){
            this.myItemClickListener=listener;
        }
        public RestaurantListAdapter(Context context,List<RestaurantInfo> restaurantList,MyItemClickListener myItemClickListener) {
            super();
            this.restaurantList=restaurantList;
            this.context=context;
            this.myItemClickListener=myItemClickListener;
        }

        public interface MyItemClickListener{
             void onItemClick(View view,int postion);
        }
        public void setWorks(List<RestaurantInfo> restaurantList) {
            this.restaurantList = restaurantList;
        }

        public RestaurantInfo getRestaurantInfo(int position){
            if (position == 0) {
                return null;
            }
            return restaurantList.get(position - 1);
        }
        public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
            public  int mViewType;
            public  View mView;
            public ImageView mRestaurantImageView;
            public TextView mRestaurantNameView;
            public RatingBar mScoreView;
            public TextView mCommentCountView;
            public  TextView mAveragePriceView;
            public TextView mKeywordView;
            public TextView mAddressVview;
            public TextView mDistanceView;
            public  MyItemClickListener mItemClickListener;

            public TextView headerView;

            public ViewHolder(View view,int viewType,MyItemClickListener myItemClickListener) {
                super(view);
                mView = view;
                mViewType=viewType;
                mItemClickListener = myItemClickListener;
                if (viewType == IS_HEADER) {
                    headerView = (TextView) view.findViewById(R.id.my_location);
                    headerView.setText("正在获取你的当前位置...");
                    headerView.setOnClickListener(this);
                    return;
                }
                mRestaurantImageView= (ImageView) view.findViewById(R.id.restaurant_image);
                mRestaurantNameView= (TextView) view.findViewById(R.id.restaurant_name_text);
                mScoreView= (RatingBar) view.findViewById(R.id.rating_bar);
                mCommentCountView= (TextView) view.findViewById(R.id.comment_count);
                mAveragePriceView= (TextView) view.findViewById(R.id.average_price);
                mKeywordView= (TextView) view.findViewById(R.id.keyword);
                mAddressVview= (TextView) view.findViewById(R.id.location);
                mDistanceView= (TextView) view.findViewById(R.id.distance);
                mItemClickListener=myItemClickListener;
                view.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                if(mItemClickListener != null){
                    mItemClickListener.onItemClick(v,getPosition());
                }
            }
        }

        public RestaurantListAdapter(Context context, List<RestaurantInfo> restaurantList, String mLocation) {
            super();
            this.context=context;
            this.restaurantList=restaurantList;
            this.mLocation = mLocation;
        }
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            //RecyclerViewHolder holder;
            //对不同的flag创建不同的Holder
            if (viewType == IS_HEADER) {
                View view = LayoutInflater.from(context).inflate(R.layout.location_header, parent, false);
                return new ViewHolder(view, IS_HEADER, myItemClickListener);
            } else if (viewType == IS_NORMAL) {
                View view = LayoutInflater.from(context)
                        .inflate(R.layout.list_item_location, parent, false);
                return new ViewHolder(view, IS_NORMAL, myItemClickListener);
            }
            return null;
        }
        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            if (position == 0) {
                holder.headerView.setText(mLocation);
                return;
            }
            position = position - 1;
            RestaurantInfo resaurantInfo=restaurantList.get(position);

            DisplayImageOptions RestaurantImageOptions = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.loading_large)
                    .showImageOnFail(R.drawable.recipe)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();
            ImageLoader.getInstance().displayImage(resaurantInfo.getPictureSmall(), holder.mRestaurantImageView, RestaurantImageOptions);
           
            holder.mDistanceView.setText("1.1km");
            holder.mAddressVview.setText(resaurantInfo.getAddress());
            holder.mKeywordView.setText(resaurantInfo.getKeyWord());
            holder.mAveragePriceView.setText(resaurantInfo.getAveragePrice());
            holder.mCommentCountView.setText("212");
            holder.mRestaurantNameView.setText(resaurantInfo.getRestaurantName());
            holder.mScoreView.setRating(4);
        }

        @Override
        public int getItemCount(){
            return restaurantList.size() + 1;
        }
        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return IS_HEADER;
            }
            return IS_NORMAL;
        }

    }
}
