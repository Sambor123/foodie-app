package com.foodie.app.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.foodie.app.R;
import com.foodie.app.model.DishInfo;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;


public class DishListAdapter extends BaseAdapter {

    private List<DishInfo> dishes;
    private Context context;
    private MyItemClickListener myItemClickListener;
    public DishListAdapter(Context context,List<DishInfo> dishes) {
        super();
        this.dishes=dishes;
        this.context=context;
    }
    public DishListAdapter(Context context,List<DishInfo> dishes,MyItemClickListener myItemClickListener) {
        super();
        this.dishes=dishes;
        this.context=context;
        this.myItemClickListener=myItemClickListener;
    }
    public void setDishes(List<DishInfo> dishes) {
        this.dishes = dishes;
    }
    public List<DishInfo> getDishes(){
        return dishes;
    }
    public DishInfo getDish(int position){
        return dishes.get(position);
    }
    public interface MyItemClickListener {
        public void onItemClick(View view,int postion);
    }
    /**
     * 设置Item点击监听
     * @param listener
     */
    public void setOnItemClickListener(MyItemClickListener listener){
        this.myItemClickListener = listener;
    }
    public static class ViewHolder implements View.OnClickListener{
        ImageView dishImageView;
        ImageView userImageView;
        TextView userNameTextView;
        TextView titleTextView;
        MyItemClickListener mItemClickListener;
        int mPosition;

        ViewHolder(View view, MyItemClickListener myItemClickListener, int position) {
            dishImageView= (ImageView) view.findViewById(R.id.user_back_image);
            //userImageView= (ImageView) view.findViewById(R.id.user_image);
            //userNameTextView= (TextView) view.findViewById(R.id.restaurant_name_text);
            titleTextView= (TextView) view.findViewById(R.id.title_text);
            mItemClickListener=myItemClickListener;
            view.setOnClickListener(this);
            mPosition = position;
        }
        /**
         * 点击监听
         */
        @Override
        public void onClick(View v) {
            if(mItemClickListener != null){
                mItemClickListener.onItemClick(v, mPosition);
            }
        }

    }

    @Override
    public int getCount() {
        return dishes.size();
    }

    @Override
    public Object getItem(int position) {
        return dishes.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(null==convertView){
            convertView=View.inflate(context, R.layout.list_item_dish, null);
            holder = new ViewHolder(convertView, myItemClickListener, position);
            convertView.setTag(holder);
        }else{
            holder=(ViewHolder)convertView.getTag();
        }
        DishInfo dishInfo=(DishInfo) getItem(position);
        //显示作品图片的配置
        DisplayImageOptions dishOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.loading_large)
                .showImageOnFail(R.drawable.recipe)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        
        ImageLoader.getInstance().displayImage(dishInfo.getPicture(), holder.dishImageView, dishOptions);
//        //显示头像图片的配置
//        DisplayImageOptions pictureSmallOptions = new DisplayImageOptions.Builder()
//                .showImageOnLoading(R.drawable.loading_small)
//                .showImageOnFail(R.drawable.user)
//                .cacheInMemory(true)
//                .cacheOnDisk(true)
//                .bitmapConfig(Bitmap.Config.RGB_565)
//                .build();
//        ImageLoader.getInstance().displayImage(dishInfo.getPicture(), holder.userImageView, pictureSmallOptions);
        holder.titleTextView.setText(dishInfo.getDishName());
        //holder.userNameTextView.setText(dishInfo.getRestaurantName());
        return convertView;
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
}
