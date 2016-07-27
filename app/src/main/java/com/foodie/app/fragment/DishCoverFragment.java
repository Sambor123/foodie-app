package com.foodie.app.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.foodie.app.R;
import com.foodie.app.model.DishInfo;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;


/**
 * Created by HaPBoy on 5/22/16.
 */
public class DishCoverFragment extends Fragment {

    private Context context;
    private DishInfo dishInfo;

    public static DishCoverFragment newInstance(Context context,DishInfo dishInfo) {
        DishCoverFragment fragment = new DishCoverFragment();
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
        View view = inflater.inflate(R.layout.fragment_dish_cover, container, false);

        ImageView ivDishCover = (ImageView) view.findViewById(R.id.dish_cover);

        TextView tvRate = (TextView) view.findViewById(R.id.tv_cover_rate);
        RatingBar rbRate = (RatingBar) view.findViewById(R.id.rb_cover_rate);

        View viewRate = view.findViewById(R.id.dish_rate);

        ivDishCover.setImageResource(R.drawable.recipe);

        DisplayImageOptions dishCoverOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.loading_large)
                .showImageOnFail(R.drawable.recipe)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        ImageLoader.getInstance().displayImage(dishInfo.getPicture(),ivDishCover, dishCoverOptions);
        // 评分
        tvRate.setText(dishInfo.getScore());
        rbRate.setRating(Float.parseFloat(dishInfo.getScore()));

        // 封面入场动画
        Animation cover_an = AnimationUtils.loadAnimation(getContext(), R.anim.book_cover_anim);
        ivDishCover.startAnimation(cover_an);
        // 评分入场动画
        Animation rate_an = AnimationUtils.loadAnimation(getContext(), R.anim.book_cover_rate_anim);
        viewRate.startAnimation(rate_an);
        return view;
    }
}
