package com.example.travelapp.Fragment.GroupFragment;

import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.example.travelapp.Control.RoundImageView;
import com.example.travelapp.Fragment.TabFragment.FFriend;
import com.example.travelapp.R;
import com.example.travelapp.entity.FriendC;
import com.example.travelapp.utils.IconLoader;

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


public class GroupMemberAddFragment extends Fragment {

    private String username;
    private List<FriendC> Friends;
    //控件
    private RecyclerView GroupMemberRe;

    private MemberAddAdapter GroupMemberAp;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化一些数据
        username = getActivity().getIntent().getStringExtra("username");
        Friends = new ArrayList<>();
        updateFriend();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_group_member_add, container, false);
        //初始化控件
        GroupMemberRe = v.findViewById(R.id.group_member_add_list);
        GroupMemberRe.setLayoutManager(new LinearLayoutManager(getActivity()));
        GroupMemberAp = new MemberAddAdapter(this.Friends);
        GroupMemberRe.setAdapter(GroupMemberAp);

        return v;
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
                        GroupMemberAp.notifyDataSetChanged();
                    }

                };
                getActivity().runOnUiThread(t);


            }
        }).start();
    }


    private class MemberAddHolder extends RecyclerView.ViewHolder{

        FriendC myfriend;
        //控件
        CheckBox MemberSelectBox;
        RoundImageView MemberSelectIcon;
        TextView MemberSelectUsername;

        public MemberAddHolder(@NonNull View itemView) {
            super(itemView);
            MemberSelectBox = itemView.findViewById(R.id.member_add_unit_checkbox);
            MemberSelectIcon = itemView.findViewById(R.id.member_add_unit_icon);
            MemberSelectUsername = itemView.findViewById(R.id.member_add_unit_username);
            //设置监听事件
            MemberSelectBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    myfriend.setIsSelectedInGroupMemberAdd(isChecked);
                }
            });
        }

        public void repaint(FriendC myfriend){
            this.myfriend = myfriend;
            MemberSelectUsername.setText(myfriend.getUsername());
            IconLoader IL = new IconLoader(GroupMemberAddFragment.this);
            IL.OnBindInfo(MemberSelectIcon,myfriend.getUsername());
            IL.setIcon();
        }

    }

    private class MemberAddAdapter extends RecyclerView.Adapter<MemberAddHolder>{
        private List<FriendC> Friends;

        public MemberAddAdapter(List<FriendC> Friends){
            this.Friends = Friends;
        }

        @NonNull
        @Override
        public MemberAddHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.group_member_add_unit,parent,false);
            return new MemberAddHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MemberAddHolder holder, int position) {
            FriendC myfriend = Friends.get(position);
            holder.repaint(myfriend);
        }

        @Override
        public int getItemCount() {
            return Friends.size();
        }
    }

    public List<FriendC> getFriends(){
        return this.Friends;
    }

}