package com.foodie.app.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.foodie.app.R;
import com.foodie.app.model.RestaurantInfo;


/**
 * Created by HaPBoy on 5/18/16.
 */
public class RecommentFragment extends Fragment {
    
    private RestaurantInfo restaurantInfo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View lv = (View)inflater.inflate(R.layout.fragment_recommend, container, false);
        return lv;
    }
}
