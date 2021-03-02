package com.example.travelapp.Fragment.TabFragment;

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

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.travelapp.Control.RoundImageView;
import com.example.travelapp.R;
import com.example.travelapp.entity.FriendC;
import com.example.travelapp.subActivity.SearchFriendActivity;
import com.example.travelapp.utils.IconLoader;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.kymjs.chat.ChatActivity;

public class FFriend extends Fragment {

    //用户名
    String username = "";

    //好友组
    List<FriendC> Friends;

    //控件
    RecyclerView FriendRv;
    FriendAdapter FriendAp;
    ImageButton FriendAddImageButton;
    SwipeRefreshLayout mFriendRefresh;//首页消息的刷新布局
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        username = getActivity().getIntent().getStringExtra("username");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_f_friend, container, false);
        //初始化friends
        this.Friends = new ArrayList<>();
        //初始化控件
        FriendAddImageButton = (ImageButton)v.findViewById(R.id.friend_add_imagebutton);
        //配置按钮监听事件
        FriendAddImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SearchFriendActivity.class);
                intent.putExtra("Friends", (Serializable)Friends);
                intent.putExtra("username",username);
                startActivity(intent);
                updateFriend();
            }
        });
        //配置re
        //设置刷新按钮
        mFriendRefresh = (SwipeRefreshLayout)v.findViewById(R.id.friend_re);
        mFriendRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onRefresh() {
                updateFriend();
            }
        });

        //配置rv
        FriendRv = v.findViewById(R.id.friend_rv);
        FriendRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        FriendAp = new FriendAdapter(this.Friends);
        FriendRv.setAdapter(FriendAp);

        //更新
        updateFriend();
        return v;
    }

    private class FriendHolder extends RecyclerView.ViewHolder{
        //单元控件
        RoundImageView mUnitIcon;
        TextView mUnitUsername;
        TextView mUnitSignature;
        IconLoader IL;


        public FriendHolder(@NonNull View itemView) {
            super(itemView);
            mUnitIcon = (RoundImageView)itemView.findViewById(R.id.friend_unit_icon);
            mUnitUsername = (TextView)itemView.findViewById(R.id.friend_unit_username);
            AssetManager mgr = getActivity().getAssets();
            Typeface tf = Typeface.createFromAsset(mgr,"fonts/FZYASHJW_Xi.TTF");
            mUnitUsername.setTypeface(tf);
            mUnitSignature = (TextView)itemView.findViewById(R.id.friend_unit_signature);
            mUnitUsername.setClickable(true);
            mUnitUsername.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), ChatActivity.class);
                    intent.putExtra("isGroup",false);
                    intent.putExtra("from_username",username);
                    intent.putExtra("to_username",mUnitUsername.getText());
                    startActivity(intent);
                }
            });
        }

        //重绘函数
        public void repaint(FriendC myfriend){
            this.mUnitUsername.setText(myfriend.getUsername());
            //this.mUnitLastMessage.setText(myfriend.getLastMessage());
            IL = new IconLoader(FFriend.this);
            IL.OnBindInfo(this.mUnitIcon,myfriend.getUsername());
            IL.setIcon();
        }
    }

    private class FriendAdapter extends RecyclerView.Adapter<FriendHolder>{

        //好友信息
        List<FriendC> friends;

        public FriendAdapter(List<FriendC> friends){
            this.friends = friends;
        }
        @NonNull
        @Override
        public FriendHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.friend_unit,parent,false);
            return new FriendHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FriendHolder holder, int position) {
            FriendC myfriend = friends.get(position);
            holder.repaint(myfriend);
        }

        @Override
        public int getItemCount() {
            return friends.size();
        }
    }

    //更新好友列表
    private void updateFriend(){
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                String url = "http://121.196.149.163:8080/dzwblog/GetFriend_server";

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
                        Friends.clear();
                        for(int i = 0;i < myjson.length();i++){
                            JSONObject temp_ob = myjson.getJSONObject(i);
                            FriendC temp_Friend = new FriendC(temp_ob.getString("friendname"));
                            Friends.add(temp_Friend);
                        }

                    }catch(Exception e){
                        e.printStackTrace();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                Runnable t = new Runnable() {
                    public void run() {
                        mFriendRefresh.setRefreshing(false);
                        FriendAp.notifyDataSetChanged();
                    }

                };
                getActivity().runOnUiThread(t);


            }
        }).start();
    }

}