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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.foodie.app.Entity.JsonResult;
import com.foodie.app.Entity.Result;
import com.foodie.app.R;
import com.foodie.app.model.User;
import com.foodie.app.util.HttpUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.makeramen.roundedimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Type;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import me.nereo.multi_image_selector.MultiImageSelector;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;

/**
 * Created by kumaha on 16/7/17.
 */

public class ModifyUserActivity extends AppCompatActivity {
    private User user = null;
    private RoundedImageView avatorView;
    private Button selectAvatorView;
    private EditText nicknameView;
    private ProgressBar progressBar;

    private List<String> paths;

    private static int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_user);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        setTitle("编辑个人信息");

        avatorView = (RoundedImageView) findViewById(R.id.user_avator);
        selectAvatorView = (Button) findViewById(R.id.btn_select_avator);
        nicknameView = (EditText) findViewById(R.id.nickname_et);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        user = (User) getIntent().getSerializableExtra("myInfo");
        showUserInfo();
        selectAvatorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paths = null;
                MultiImageSelector.create(ModifyUserActivity.this)
                        .showCamera(true) // show camera or not. true by default
                        .single() // single mode
                        .start(ModifyUserActivity.this, REQUEST_CODE);
            }
        });
        avatorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paths = null;
                MultiImageSelector.create(ModifyUserActivity.this)
                        .showCamera(true) // show camera or not. true by default
                        .single() // single mode
                        .start(ModifyUserActivity.this, REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Get the result list of select image paths
                paths = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
                Bitmap bmImg = BitmapFactory.decodeFile(paths.get(0));
                avatorView.setImageBitmap(bmImg);
            }
        }
    }

    private void modifyUserInfo() {
        if (paths == null) {
            Toast.makeText(ModifyUserActivity.this, "头像不能为空", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }
        File file = new File(paths.get(0));
        if (HttpUtils.isNetworkConnected(this)) {
            //封装请求参数
            RequestParams requestParams = new RequestParams();
            requestParams.put("nickname", nicknameView.getText());
            try {
                requestParams.put("file", file, "image/*");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "图片上传失败" + e, Toast.LENGTH_LONG).show();
            }

            HttpUtils.postWithAuth(getBaseContext(), "user/me/modify/", requestParams, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    Gson gson = new Gson();
                    Type type = new TypeToken<JsonResult<User>>() {
                    }.getType();
                    JsonResult<User> jsonResult = gson.fromJson(new String(responseBody), type);
                    String status = jsonResult.getStatus();
                    User user = jsonResult.getData();
                    //Log.v(TAG,data);
                    if (TextUtils.equals(status, Result.SUCCESS) && user != null) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ModifyUserActivity.this, "修改成功", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(ModifyUserActivity.this, MainActivity.class);
                        intent.putExtra("currentTab", 3);
                        startActivity(intent);
                        finish();
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Log.e("PostActivity", new String(responseBody));
                        Toast.makeText(ModifyUserActivity.this, "请重新登录", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ModifyUserActivity.this, "服务器繁忙", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Toast.makeText(this, "没有网络连接!", Toast.LENGTH_LONG).show();
        }
    }

    private void showUserInfo() {
        //显示头像图片的配置
        DisplayImageOptions userImageOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.loading_small)
                .showImageOnFail(R.drawable.user)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        ImageLoader.getInstance().displayImage(user.getAvator(), avatorView, userImageOptions);
        nicknameView.setText(user.getNickname());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_done:
                progressBar.setVisibility(View.VISIBLE);
                modifyUserInfo();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.modify_menu, menu);
        return true;
    }
}
