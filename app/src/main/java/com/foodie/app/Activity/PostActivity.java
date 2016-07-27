package com.foodie.app.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.foodie.app.Entity.JsonResult;
import com.foodie.app.Entity.Result;
import com.foodie.app.R;
import com.foodie.app.model.WorksInfo;
import com.foodie.app.util.Constant;
import com.foodie.app.util.HttpUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Type;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import me.nereo.multi_image_selector.MultiImageSelector;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;

/**
 * Created by kumaha on 16/7/9.
 */
public class PostActivity extends AppCompatActivity {
    private static int REQUEST_CODE = 1;
    private EditText mIntroductionView;
    private ImageButton mImageView;
    private EditText mDishNameView;
    private File file;
    //private String picPath;

    List<String> paths;

    MenuItem sendItem;

    ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        mIntroductionView = (EditText) findViewById(R.id.et_introduction);
        mImageView = (ImageButton) findViewById(R.id.ib_plus);
        mDishNameView = (EditText) findViewById(R.id.et_dish_name);
        mImageView = (ImageButton) findViewById(R.id.ib_plus);
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paths = null;
                MultiImageSelector.create(PostActivity.this)
                        .showCamera(true) // show camera or not. true by default
                        .single() // single mode
                        .start(PostActivity.this, REQUEST_CODE);

            }
        });
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        setTitle("分享美食");
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Get the result list of select image paths
                paths = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
                Bitmap bmImg = BitmapFactory.decodeFile(paths.get(0));
                mImageView.setImageBitmap(bmImg);
                sendItem.setEnabled(true);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_done:
                if (paths == null) {
                    Toast.makeText(PostActivity.this, "请选择要上传的图片", Toast.LENGTH_SHORT).show();
                    break;
                }
                progressBar.setVisibility(View.VISIBLE);
                sendWorks();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void sendWorks() {
        sendItem.setEnabled(false);
        if (paths == null) {
            Toast.makeText(PostActivity.this, "图片不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        File file = new File(paths.get(0));
        if (HttpUtils.isNetworkConnected(this)) {
            //封装请求参数
            RequestParams requestParams = new RequestParams();
            requestParams.put("dishName", mDishNameView.getText());
            requestParams.put("introduction", mIntroductionView.getText());
            try {
                requestParams.put("file", file,"image/*");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "图片上传失败"+e, Toast.LENGTH_LONG).show();
            }

            HttpUtils.postWithAuth(getBaseContext(),Constant.SEND_WORK, requestParams, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    Gson gson = new Gson();
                    Type type = new TypeToken<JsonResult<WorksInfo>>() {
                    }.getType();
                    JsonResult<WorksInfo> jsonResult = gson.fromJson(new String(responseBody), type);
                    String status = jsonResult.getStatus();
                    WorksInfo worksInfo = jsonResult.getData();
                    //Log.v(TAG,data);

                    if (TextUtils.equals(status, Result.SUCCESS) && worksInfo != null) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(PostActivity.this, "发布成功" + worksInfo.getDishName(), Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(PostActivity.this, MainActivity.class);
                        intent.putExtra("currentTab", 2);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.e("PostActivity",new String(responseBody));
                        Toast.makeText(PostActivity.this, "请重新登录", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Toast.makeText(PostActivity.this, "服务器繁忙", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Toast.makeText(this, "没有网络连接!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.post_menu, menu);
        sendItem = menu.findItem(R.id.action_done);
        sendItem.setEnabled(true);
        return true;
    }
}

