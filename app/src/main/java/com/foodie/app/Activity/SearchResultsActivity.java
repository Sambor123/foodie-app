package com.foodie.app.Activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.foodie.app.R;
import com.foodie.app.fragment.DishListFragment;

/**
 * Created by kumaha on 16/7/15.
 */

public class SearchResultsActivity extends AppCompatActivity {
    private String query;
    FloatingSearchView mFloatingSearchView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        query = getIntent().getStringExtra("query");
        mFloatingSearchView = (FloatingSearchView) findViewById(R.id.floating_search_view);
        mFloatingSearchView.setOnHomeActionClickListener(new FloatingSearchView.OnHomeActionClickListener() {
            @Override
            public void onHomeClicked() {
                onBackPressed();
            }
        });
        mFloatingSearchView.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {
            @Override
            public void onActionMenuItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.id_action_search:
                        doSearch(mFloatingSearchView.getQuery().toString());
                        break;
                }
            }
        });
        //搜索结果
        Fragment SearchResultFragment = DishListFragment.newInstance(query);
        getSupportFragmentManager().beginTransaction().add(R.id.search_result, SearchResultFragment).commit();
    }

    private void doSearch(String query) {
        //搜索结果
        Fragment SearchResultFragment = DishListFragment.newInstance(query);
        getSupportFragmentManager().beginTransaction().add(R.id.search_result, SearchResultFragment).commit();
    }

}
