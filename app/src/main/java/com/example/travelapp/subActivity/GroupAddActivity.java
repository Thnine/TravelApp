package com.example.travelapp.subActivity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.travelapp.Fragment.GroupFragment.GroupMemberAddFragment;
import com.example.travelapp.Fragment.GroupFragment.GroupRoutePlanFragment;
import com.example.travelapp.R;
import com.example.travelapp.entity.FriendC;
import com.example.travelapp.entity.PlanRecord;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GroupAddActivity extends AppCompatActivity {

    private String username;
    private List<FriendC> members;
    private List<PlanRecord> PlanRecords;
    private String groupname;

    //控件信息
    TextView PhaseText;
    Button NextButton;
    //fragments
    GroupMemberAddFragment MemberAddFragment;
    GroupRoutePlanFragment RoutePlanFragment;
    Fragment NowFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_add);
        //隐藏消息栏
        getSupportActionBar().hide();
        //获取数据
        username = getIntent().getStringExtra("username");
        //初始化一些数据
        members = new ArrayList<>();
        //初始化控件
        PhaseText = (TextView)findViewById(R.id.group_add_phase_text);
        NextButton = (Button)findViewById(R.id.group_add_next_button);
        //初始化文本
        PhaseText.setText("添加用户");
        //生成fragment
        MemberAddFragment = new GroupMemberAddFragment();
        RoutePlanFragment = new GroupRoutePlanFragment();
        NowFragment = MemberAddFragment;
        //fragment布局
        getSupportFragmentManager().beginTransaction().replace(R.id.group_add_fragment,MemberAddFragment).commit();

        //配置监听事件
        NextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("GroupAddActivity",NowFragment.getClass().getName());
                if(NowFragment.getClass().getName().equals("com.example.travelapp.Fragment.GroupFragment.GroupMemberAddFragment")){
                    NowFragment = RoutePlanFragment;
                    getSupportFragmentManager().beginTransaction().replace(R.id.group_add_fragment,RoutePlanFragment).commit();
                    PhaseText.setText("路线规划");
                    //从GroupMemberAddFragment获取成员信息
                    members = MemberAddFragment.getFriends();
                }
                else if(NowFragment.getClass().getName().equals("com.example.travelapp.Fragment.GroupFragment.GroupRoutePlanFragment")){
                    PhaseText.setText("取个名字");
                    //从GroupRoutePlanFragment获取路线规划信息
                    PlanRecords = RoutePlanFragment.getPlanRecords();
                    //为你的团队取个名字
                    LastDet();
                }
            }
        });

    }

    //上传队伍创建信息进入数据库
    public void CommitGroupCreateInfoToDatabase(){
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                String url = "http://121.196.149.163:8080/dzwblog/AddGroup_server";

                //创建HTTP对象
                HttpPost httpRequset = new HttpPost(url);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("owner_name", username));
                params.add(new BasicNameValuePair("member_num", Integer.toString(members.size())));
                params.add(new BasicNameValuePair("record_num", Integer.toString(PlanRecords.size())));
                params.add(new BasicNameValuePair("group_name", groupname));
                for(int i = 0;i < members.size();i++){
                    params.add(new BasicNameValuePair("member" + i, members.get(i).getUsername()));
                }
                for(int i = 0;i < PlanRecords.size();i++){
                    params.add(new BasicNameValuePair("city"+i,PlanRecords.get(i).getPlanLoc().getCity()));
                    params.add(new BasicNameValuePair("province"+i,PlanRecords.get(i).getPlanLoc().getProvince()));
                    params.add(new BasicNameValuePair("Date"+i,Long.toString(PlanRecords.get(i).getPlanDate().getTime())));
                }

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
                        boolean result =  myjson.getBoolean("result");

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

    //最后一步
    private void LastDet() {
        /*@setView 装入一个EditView
         */
        final EditText editText = new EditText(GroupAddActivity.this);
        AlertDialog.Builder inputDialog =
                new AlertDialog.Builder(GroupAddActivity.this);
        inputDialog.setTitle("为你的团队取个名字吧").setView(editText);
        inputDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        groupname = editText.getText().toString();
                        CommitGroupCreateInfoToDatabase();
                    }
                }).show();
    }

}