package com.foodie.app.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.foodie.app.R;
import com.foodie.app.model.DishInfo;


/**
 * Created by HaPBoy on 5/18/16.
 */
public class DishInfoItemFragment extends Fragment {

    
    private DishInfo dishInfo;
    private Context context;

    private TextView dishNameView;
    private TextView dishIntroductionView;
    private TextView dishRestaurantNameView;
    private TextView dishTasteView;
    private TextView dishPriceView;
    private TextView dishAddressView;

    public static DishInfoItemFragment newInstance(Context context, DishInfo dishInfo) {
        DishInfoItemFragment fragment = new DishInfoItemFragment();
        fragment.context=context;
        fragment.dishInfo=dishInfo;
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = (View)inflater.inflate(R.layout.fragment_dish_info_item_list, container, false);
        dishNameView= (TextView) view.findViewById(R.id.dishName);
        dishIntroductionView= (TextView) view.findViewById(R.id.dish_introduction);
        dishRestaurantNameView= (TextView) view.findViewById(R.id.dish_restaurant_name);
        dishTasteView= (TextView) view.findViewById(R.id.dishTaste);
        dishAddressView= (TextView) view.findViewById(R.id.dishAddress);
        dishPriceView= (TextView) view.findViewById(R.id.dishPrice);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        updateDishInfo();
    }

    private void updateDishInfo() {
        dishNameView.setText(dishInfo.getDishName());
        dishPriceView.setText(dishInfo.getPrice());
        dishAddressView.setText(dishInfo.getAddress());
        dishTasteView.setText(dishInfo.getTaste());
        dishRestaurantNameView.setText(dishInfo.getRestaurantName());
        dishIntroductionView.setText(dishInfo.getIntroduction());
    }

}
