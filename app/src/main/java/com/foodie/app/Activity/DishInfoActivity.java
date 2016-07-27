package com.foodie.app.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.foodie.app.Entity.JsonResult;
import com.foodie.app.Entity.Result;
import com.foodie.app.R;
import com.foodie.app.fragment.DishCoverFragment;
import com.foodie.app.fragment.DishInfoCommentFragment;
import com.foodie.app.fragment.DishInfoItemFragment;
import com.foodie.app.fragment.RecommentFragment;
import com.foodie.app.model.Collection;
import com.foodie.app.model.DishInfo;
import com.foodie.app.util.HttpUtils;
import com.foodie.app.util.PrefUtils;
import com.foodie.app.view.ViewPagerIndicator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import cz.msebera.android.httpclient.Header;



/**
 * Created by HaPBoy on 5/18/16.
 */
public class DishInfoActivity extends AppCompatActivity {
    private static String TAG = "DishInfoActivity";
    // Context
    private Context context;
    // ViewPager
    private ViewPager viewPager;
    private FragmentPagerAdapter pagerAdapter;

    //是否已经收藏
    private boolean isDishFavorite = false;
    //用户是否登录
    private boolean isUserLogin;

    // ViewPagerIndicator
    private ViewPagerIndicator viewPagerIndicator;
    private List<String> titles = Arrays.asList("美食详情", "相关评论", "猜你喜欢");

    // Fragment
    private List<Fragment> fragments = new ArrayList<>();

    private DishInfo dishInfo;

    //收藏按钮图片
    private int iconFavorite[] = {R.drawable.ic_favorite_border_white, R.drawable.ic_favorite_white};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dish_info);
        // Context
        context = this;
        //获取美食信息
        dishInfo = (DishInfo) getIntent().getSerializableExtra("dishInfo");
        //初始化用户状态
        isUserLogin = getUserState();
        if (isUserLogin) {
            Log.i(TAG, "用户已经登录" + isUserLogin);
            updateDishFavorite();//更新收藏状态
        }
        //初始化toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        //ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);

        //初始化界面信息
        setTitle(dishInfo.getDishName());
        // ViewPager
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        // ViewPagerIndicator
        viewPagerIndicator = (ViewPagerIndicator) findViewById(R.id.indicator);
        viewPagerIndicator.setTabItemTitles(titles);
        viewPagerIndicator.setVisibleTabCount(3);

        // 基本信息 Fragment
        fragments.add(DishInfoItemFragment.newInstance(context,dishInfo));

        // 评论 Fragment
        fragments.add(new DishInfoCommentFragment());

        // 推荐 Fragment
        fragments.add(new RecommentFragment());

        // PagerAdapter
        pagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public int getCount() {
                return fragments.size();
            }

            @Override
            public Fragment getItem(int position) {
                return fragments.get(position);
            }
        };

        // 设置数据适配器
        viewPager.setAdapter(pagerAdapter);
        viewPagerIndicator.setViewPager(viewPager, 0);

        // 封面图
        Fragment dishFragment = DishCoverFragment.newInstance(context,dishInfo);
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_dish_cover, dishFragment).commit();
    }

    private boolean getUserState() {
        if (PrefUtils.get("user", "userId", getBaseContext()) == null) {
            //Toast.makeText(DishInfoActivity.this, "请先登录", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private boolean updateDishFavorite() {
        if (HttpUtils.isNetworkConnected(this)) {
            HttpUtils.getWithAuth(getBaseContext(), "dish/" + dishInfo.getId() + "/iscollect/", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    Gson gson=new Gson();
                    //Type type = new TypeToken<JsonResult<User>>(){}.getType();
                    Type type = new TypeToken<JsonResult<Collection>>(){}.getType();
                    JsonResult<Collection> jsonResult=gson.fromJson(new String(responseBody),type);
                    String status=jsonResult.getStatus();
                    String tipCode = jsonResult.getTipCode();
                    //Log.v(TAG,data);
                    Log.i(TAG, "获取收藏状态成功" + new String(responseBody));
                    if (TextUtils.equals(status, Result.SUCCESS) && TextUtils.equals(tipCode, "collect")) {
                        Log.i(TAG, "用户收藏了该美食");
                        isDishFavorite=true;
                        invalidateOptionsMenu();
                        return;
                    } else if (TextUtils.equals(status, Result.SUCCESS) && TextUtils.equals(tipCode, "notCollect")) {
                        Log.i(TAG, "用户未收藏该美食");
                        isDishFavorite=false;
                        invalidateOptionsMenu();
                        return;
                    } else if (TextUtils.equals(status, Result.FAILED) && TextUtils.equals(tipCode, "notLogin")) {
                        Log.i(TAG, "客户端登录,服务端session失效");
                        Toast.makeText(DishInfoActivity.this, "登录状态失效,请重新登录", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(DishInfoActivity.this, LoginActivity.class));
                        DishInfoActivity.this.finish();
                    } else if (TextUtils.equals(status, Result.FAILED) && TextUtils.equals(tipCode, "notExist")) {
                        Log.i(TAG, "菜品不存在");
                        Toast.makeText(DishInfoActivity.this, "该菜品已下架", Toast.LENGTH_LONG).show();
                    }
                    invalidateOptionsMenu();
                }
                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Toast.makeText(DishInfoActivity.this,"获取收藏数据失败",Toast.LENGTH_LONG).show();
                    Log.i(TAG, "获取收藏状态失败" + new String(responseBody));
                }
            });
        } else {
            Toast.makeText(this, "请检查网络连接!", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    private void toggleFavorite() {
        if (HttpUtils.isNetworkConnected(this)) {
            HttpUtils.postWithAuth(getBaseContext(), "dish/" + dishInfo.getId() + "/toggleCollect/", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    Gson gson=new Gson();
                    //Type type = new TypeToken<JsonResult<User>>(){}.getType();
                    Type type = new TypeToken<JsonResult<Collection>>(){}.getType();
                    JsonResult<Collection> jsonResult=gson.fromJson(new String(responseBody),type);
                    String status=jsonResult.getStatus();
                    String tipCaode=jsonResult.getTipCode();
                    //Log.v(TAG,data);
                    if (TextUtils.equals(status,Result.FAILED)&&TextUtils.equals(tipCaode,"notLogin")){
                        Toast.makeText(DishInfoActivity.this,"请先登录",Toast.LENGTH_LONG).show();
                        Log.i(TAG, "客户端已登录,服务端session失效");
                        return;
                    }else if(TextUtils.equals(status,Result.FAILED)&&TextUtils.equals(tipCaode,"notExist")){
                        Toast.makeText(DishInfoActivity.this,"菜品不存在",Toast.LENGTH_LONG).show();
                        Log.i(TAG, "传入的菜品id不存在");
                        return;
                    }else if(TextUtils.equals(status,Result.FAILED)&&TextUtils.equals(tipCaode,"CollectFail")){
                        Toast.makeText(DishInfoActivity.this,"收藏失败",Toast.LENGTH_LONG).show();
                        Log.i(TAG, "收藏失败");
                        return;
                    }else if(TextUtils.equals(status,Result.FAILED)&&TextUtils.equals(tipCaode,"unCollectFail")){
                        Toast.makeText(DishInfoActivity.this,"取消收藏失败",Toast.LENGTH_LONG).show();
                        Log.i(TAG, "取消失败");
                        return;
                    }else if(TextUtils.equals(status,Result.SUCCESS)&&TextUtils.equals(tipCaode,"Collect")){
                        //Toast.makeText(DishInfoActivity.this,"收藏成功",Toast.LENGTH_LONG).show();
                        Log.i(TAG, "收藏成功");
                        isDishFavorite=true;
                        invalidateOptionsMenu();
                        return;
                    }else if(TextUtils.equals(status,Result.SUCCESS)&&TextUtils.equals(tipCaode,"unCollect")){
                        //Toast.makeText(DishInfoActivity.this,"取消成功",Toast.LENGTH_LONG).show();
                        Log.i(TAG, "取消成功");
                        isDishFavorite=false;
                        invalidateOptionsMenu();
                        return;
                    }
                }
                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Toast.makeText(DishInfoActivity.this,"获取收藏数据失败",Toast.LENGTH_LONG).show();
                    Log.i("DishInfoActivity",new String(responseBody));
                }
            });
        } else {
            Toast.makeText(this, "请检查网络连接!", Toast.LENGTH_LONG).show();
        }

        return;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_favorite:
                toggleFavorite();
                if (isUserLogin) {
                    new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText(isDishFavorite ? "取消收藏" : "收藏成功")
                            .setContentText(isDishFavorite ? "美食已取消收藏" : "美食已收藏")
                            .setConfirmText("确定")
                            .show();
                }
                return true;
            case R.id.action_browser:
                Intent intent = new Intent(Intent.ACTION_VIEW);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.dish_info, menu);
        return true;
    }

    //
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.action_favorite);
        Log.i(TAG, "Pre" + isDishFavorite);
        menuItem.setIcon(isDishFavorite?iconFavorite[1]:iconFavorite[0]);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
}
