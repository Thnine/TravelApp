package com.example.travelapp.Fragment.TabFragment;


/**
 * 队伍界面的fragment
 */
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.travelapp.R;
import com.example.travelapp.entity.FriendC;
import com.example.travelapp.entity.GroupC;
import com.example.travelapp.entity.LocationC;
import com.example.travelapp.entity.PlanRecord;
import com.example.travelapp.subActivity.GroupAddActivity;
import com.example.travelapp.subActivity.GroupInfoActivity;

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
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class FGroup extends Fragment {

    private String username = "";

    //控件
    private ImageButton AddGroupButton;
    private RecyclerView GroupShowRv;
    private GroupShowAdapter GroupShowAp;
    private SwipeRefreshLayout GroupRefresh;
    //小组信息
    private List<GroupC> Groups;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化一些数据
        this.Groups = new ArrayList<>();
        //获取数据
        username = getActivity().getIntent().getStringExtra("username");
        Log.d("FGroup","username is" + username);
        getGroupFromDataBase();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_f_group, container, false);

        //初始化控件
        AddGroupButton = (ImageButton)v.findViewById(R.id.group_add_imagebutton);
        GroupShowRv = (RecyclerView)v.findViewById(R.id.group_rv);
        GroupShowRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        GroupShowAp = new GroupShowAdapter(this.Groups);
        GroupShowRv.setAdapter(GroupShowAp);
        GroupRefresh = (SwipeRefreshLayout)v.findViewById(R.id.group_re);
        //配置监听事件
        AddGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), GroupAddActivity.class);
                intent.putExtra("username",username);
                startActivity(intent);
            }
        });
        //配置刷新事件
        GroupRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getGroupFromDataBase();
            }
        });


        return v;
    }


    private class GroupShowHolder extends RecyclerView.ViewHolder{
        //数据信息
        private GroupC mygroup;
        //控件信息
        private TextView GroupNameText;

        public GroupShowHolder(@NonNull View itemView) {
            super(itemView);
            //初始化控件
            GroupNameText = (TextView)itemView.findViewById(R.id.group_unit_name);
            //配置字体
            AssetManager mgr = getActivity().getAssets();
            Typeface tf = Typeface.createFromAsset(mgr,"fonts/FZYASHJW_Xi.TTF");
            GroupNameText.setTypeface(tf);
            //配置监听事件
            GroupNameText.setClickable(true);
            GroupNameText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), GroupInfoActivity.class);
                    intent.putExtra("mygroup",mygroup);
                    intent.putExtra("username",username);
                    startActivity(intent);
                }
            });
        }

        public void repaint(GroupC mygroup){
            this.mygroup = mygroup;
            GroupNameText.setText(mygroup.getGroupname());
        }
    }

    private class GroupShowAdapter extends RecyclerView.Adapter<GroupShowHolder>{

        //数据
        private List<GroupC> Groups;

        public GroupShowAdapter(List<GroupC> Groups){
            this.Groups = Groups;
        }

        @NonNull
        @Override
        public GroupShowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.group_unit,parent,false);
            return new GroupShowHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull GroupShowHolder holder, int position) {
            GroupC mygroup = this.Groups.get(position);
            holder.repaint(mygroup);
        }

        @Override
        public int getItemCount() {
            return Groups.size();
        }
    }


    //更新小组信息
    public void getGroupFromDataBase(){
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                String url = "http://121.196.149.163:8080/dzwblog/GetGroup_server";

                //创建HTTP对象
                HttpPost httpRequset = new HttpPost(url);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("username", username));

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
                        //处理Array数据

                        JSONArray myjson = new JSONArray(entityStringBuilder.toString());
                        Groups.clear();
                        for(int i = 0;i < myjson.length();i++){
                            JSONObject temp_ob = myjson.getJSONObject(i);
                            List<FriendC> members = new ArrayList<>();
                            List<PlanRecord> PlanRecords = new ArrayList<>();
                            //获取成员信息
                            int member_num = temp_ob.getInt("member_num");
                            for(int j = 0; j <member_num;j++) {
                                members.add(new FriendC(temp_ob.getString("member_name"+j)));
                            }
                            //获取计划信息
                            int plan_num = temp_ob.getInt("plan_num");
                            for(int j = 0;j < plan_num;j++) {
                                PlanRecord PL = new PlanRecord(new Date(temp_ob.getLong("date"+j)),new LocationC(temp_ob.getString("province"+j),temp_ob.getString("city"+j)),temp_ob.getInt("plan_id"+j));
                                PlanRecords.add(PL);
                                Log.d("FGroup","plan_id" + PL.getPlan_id());
                            }
                            //获取组名信息
                            String group_name = temp_ob.getString("group_name");
                            FriendC owner = new FriendC(temp_ob.getString("group_owner"));
                            GroupC temp_g = new GroupC(group_name,owner,members,PlanRecords);
                            temp_g.setGroup_id(temp_ob.getInt("group_id"));
                            Groups.add(temp_g);
                        }

                    }catch(Exception e){
                        e.printStackTrace();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                Runnable t = new Runnable() {
                    public void run() {
                        GroupRefresh.setRefreshing(false);
                        GroupShowAp.notifyDataSetChanged();
                    }

                };
                getActivity().runOnUiThread(t);


            }
        }).start();
    }



}

