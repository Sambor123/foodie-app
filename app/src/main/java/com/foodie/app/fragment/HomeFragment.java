package com.foodie.app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.etsy.android.grid.StaggeredGridView;
import com.foodie.app.Activity.DishInfoActivity;
import com.foodie.app.Entity.JsonListResult;
import com.foodie.app.Entity.Result;
import com.foodie.app.R;
import com.foodie.app.adapter.DishListAdapter;
import com.foodie.app.model.DishInfo;
import com.foodie.app.util.Constant;
import com.foodie.app.util.HttpUtils;
import com.foodie.app.view.Kanner;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.lang.reflect.Type;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by kumaha on 16/7/7.
 */
public class HomeFragment extends Fragment{
    protected static final String TAG="HomeFragment";

    private StaggeredGridView dishListView;
    private Kanner kanner;

    private DishListAdapter dishListAdapter;

    private List<DishInfo> dishes=null;

    private SwipeRefreshLayout sr;

    private ProgressBar progressBar;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(
                R.layout.fragment_home, container, false);
        dishListView= (StaggeredGridView) view.findViewById(R.id.channel_dish_list);
        sr = (SwipeRefreshLayout) view.findViewById(R.id.sr);
        View header = inflater.inflate(R.layout.kanner, dishListView, false);
        kanner = (Kanner) header.findViewById(R.id.kanner);
        dishListView.addHeaderView(header);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        return view;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        int[] imagesRes = {R.drawable.a, R.drawable.b, R.drawable.c};
        kanner.setImagesRes(imagesRes);
        updateDishes();
        sr.setColorSchemeResources(
                R.color.colorPrimary,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        sr.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateDishes();
                sr.setRefreshing(false);
            }
        });
    }

    private void updateDishes() {
        if (HttpUtils.isNetworkConnected(getActivity())) {
            HttpUtils.get(Constant.DISHES, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    Gson gson=new Gson();
                    //Type type = new TypeToken<JsonResult<User>>(){}.getType();
                    Type type = new TypeToken<JsonListResult<DishInfo>>(){}.getType();
                    JsonListResult<DishInfo> jsonListResult=gson.fromJson(new String(responseBody),type);
                    String status=jsonListResult.getStatus();
                    dishes=jsonListResult.getData();
                    //Log.v(TAG,data);
                    if(TextUtils.equals(status, Result.SUCCESS)&&dishes!=null){
                        //Toast.makeText(getActivity(),"刷新成功"+dishes.size(),Toast.LENGTH_LONG).show();
                        Log.i(TAG,new String(responseBody));
                        //sr.setRefreshing(false);
                        progressBar.setVisibility(View.GONE);
                        dishListAdapter = new DishListAdapter(getActivity(),dishes);
                        dishListAdapter.setOnItemClickListener(new DishListAdapter.MyItemClickListener() {
                            @Override
                            public void onItemClick(View view, int postion) {
                                Intent intent = new Intent(getActivity(), DishInfoActivity.class);
                                intent.putExtra("dishInfo", dishListAdapter.getDish(postion));
                                getActivity().startActivity(intent);
                            }
                        });
                        dishListView.setAdapter(dishListAdapter);
                        return;
                    }else{
                        Toast.makeText(getActivity(),"获取菜肴信息失败"+new String(responseBody),Toast.LENGTH_LONG).show();
                    }
                }
                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Toast.makeText(getActivity(), "网络繁忙", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Toast.makeText(getActivity(), "没有网络连接!", Toast.LENGTH_LONG).show();
        }
    }
}
