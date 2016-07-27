package com.foodie.app.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.foodie.app.R;
import com.foodie.app.model.WorksInfo;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class WorksListAdapter extends BaseAdapter {

    private List<WorksInfo> worksInfo=new ArrayList<>();
    private Context context;

    public WorksListAdapter(Context context, List<WorksInfo> worksInfo) {
        super();
        this.worksInfo=worksInfo;
        this.context=context;
    }

    public void setDishes(List<WorksInfo> worksInfo) {
        this.worksInfo = worksInfo;
    }

    public static class ViewHolder {
        CircleImageView avatorView;
        TextView nicknameView;
        TextView postTimeView;
        TextView introductionView;
        ImageView worksImageView;

        ViewHolder(View view) {
            avatorView = (CircleImageView) view.findViewById(R.id.avatar);
            nicknameView= (TextView) view.findViewById(R.id.tv_nickname);
            postTimeView= (TextView) view.findViewById(R.id.tv_post_time);
            introductionView= (TextView) view.findViewById(R.id.tv_introduction);
            worksImageView= (ImageView) view.findViewById(R.id.iv_works_image);
        }
    }

    @Override
    public int getCount() {
        return worksInfo.size();
    }

    @Override
    public Object getItem(int position) {
        return worksInfo.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(null==convertView){
            convertView=View.inflate(context, R.layout.list_item_works, null);
            holder=new ViewHolder(convertView);
            convertView.setTag(holder);
        }else{
            holder=(ViewHolder)convertView.getTag();
        }
        WorksInfo worksInfo=(WorksInfo) getItem(position);
        String imageUrl = "http://bjtu-foodie.oss-cn-shanghai.aliyuncs.com/dish/dish1.png";

        //显示图片的配置
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.recipe)
                .showImageOnFail(R.drawable.recipe)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        ImageLoader.getInstance().displayImage(imageUrl, holder.worksImageView, options);
        ImageLoader.getInstance().displayImage("http://bjtu-foodie.oss-cn-shanghai.aliyuncs.com/head/head5.png", holder.avatorView, options);
        //holder.avatorView.setImageResource(R.drawable.user);
        holder.nicknameView.setText(worksInfo.getNickname());
        holder.postTimeView.setText(worksInfo.getCreateTime());
        holder.introductionView.setText(worksInfo.getIntroduction());
        //holder.worksImageView.setImageResource(R.drawable.recipe);
        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
