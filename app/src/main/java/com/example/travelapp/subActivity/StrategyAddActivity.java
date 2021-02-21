package com.example.travelapp.subActivity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.Toast;

import com.example.travelapp.R;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class StrategyAddActivity extends AppCompatActivity {
    private String username;
    private String city;
    private float score;
    //控件信息
    private ImageButton commit_button;
    private EditText strategy_text;
    private RatingBar strategy_score;
    private EditText strategy_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_strategy_add);
        //隐藏任务栏
        getSupportActionBar().hide();
        //获取数据
        username = getIntent().getStringExtra("username");
        city = getIntent().getStringExtra("city");
        //初始化控件
        commit_button = (ImageButton)findViewById(R.id.add_strategy_commit);
        strategy_score = (RatingBar)findViewById(R.id.add_strategy_score);
        strategy_text = (EditText)findViewById(R.id.add_strategy_text);
        strategy_title = (EditText)findViewById(R.id.add_strategy_title);
        //配置监听事件
        commit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateStrategy();
            }
        });
        strategy_score.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
               score = rating;
            }
        });
    }

    //上传攻略
    public void updateStrategy(){
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                String url = "http://121.196.149.163:8080/dzwblog/AddStrategy_server";

                //创建HTTP对象
                HttpPost httpRequset = new HttpPost(url);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("username", username));
                params.add(new BasicNameValuePair("city", city));
                params.add(new BasicNameValuePair("title",strategy_title.getText().toString()));
                params.add(new BasicNameValuePair("text", strategy_text.getText().toString()));
                params.add(new BasicNameValuePair("score", Float.toString(score)));

                try{
                    //装入数据和设置数据格式
                    httpRequset.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));
                    //发出请求
                    CloseableHttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpRequset);
                    //获取服务器响应的资源

                    String line=null;
                    StringBuilder entityStringBuilder=new StringBuilder();
                    try {
                        BufferedReader b = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), "UTF-8"),8*1024);
                        while ((line=b.readLine())!=null) {
                            entityStringBuilder.append(line+"/n");
                        }

                        JSONObject myjson = new JSONObject(entityStringBuilder.toString());
                        final boolean result =  myjson.getBoolean("result");
                        Runnable t = new Runnable() {
                            @Override
                            public void run() {
                                if(result){
                                    Toast.makeText(StrategyAddActivity.this,R.string.add_strategy_true,Toast.LENGTH_LONG).show();
                                    finish();
                                }
                                else
                                {
                                    Toast.makeText(StrategyAddActivity.this,R.string.add_strategy_false,Toast.LENGTH_LONG).show();
                                    finish();
                                }
                            }
                        };
                        runOnUiThread(t);


                    }catch(Exception e){
                        e.printStackTrace();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                finish();


            }
        }).start();
    }
}