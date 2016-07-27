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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.foodie.app.R;
import com.foodie.app.fragment.DishInfoCommentFragment;
import com.foodie.app.fragment.RecommentFragment;
import com.foodie.app.model.RestaurantInfo;
import com.foodie.app.view.ViewPagerIndicator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by HaPBoy on 5/18/16.
 */
public class RestaurantInfoActivity extends AppCompatActivity {

    // Context
    private Context context;
    // ViewPager
    private ViewPager viewPager;
    private FragmentPagerAdapter pagerAdapter;

    // ViewPagerIndicator
    private ViewPagerIndicator viewPagerIndicator;
    private List<String> titles = Arrays.asList("商家详情", "美食", "猜你喜欢");

    // Fragment
    private List<Fragment> fragments = new ArrayList<>();

    private RestaurantInfo restaurantInfo;

    //收藏按钮图片
    private int iconFavorite[] = {R.drawable.ic_favorite_border_white, R.drawable.ic_favorite_white};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_info);
        // Context
        context = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        //ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);

        restaurantInfo = (RestaurantInfo) getIntent().getSerializableExtra("restaurantInfo");

        setTitle(restaurantInfo.getRestaurantName());
        // ViewPager
        viewPager = (ViewPager) findViewById(R.id.view_pager);

        // ViewPagerIndicator
        viewPagerIndicator = (ViewPagerIndicator) findViewById(R.id.indicator);
        viewPagerIndicator.setTabItemTitles(titles);
        viewPagerIndicator.setVisibleTabCount(3);

        // 基本信息 Fragment
        fragments.add(new RecommentFragment());

        fragments.add(new DishInfoCommentFragment());

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

        // 图书封面
        //Fragment dishFragment = DishCoverFragment.newInstance(context,restaurantInfo);
        //getSupportFragmentManager().beginTransaction().add(R.id.fragment_dish_cover, dishFragment).commit();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        //client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
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
        menuItem.setIcon(iconFavorite[0]);
        return super.onPrepareOptionsMenu(menu);
    }

    //
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
