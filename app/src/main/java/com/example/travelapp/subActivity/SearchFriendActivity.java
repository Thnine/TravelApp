package com.example.travelapp.subActivity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.travelapp.Control.RoundImageView;
import com.example.travelapp.Fragment.TabFragment.FFriend;
import com.example.travelapp.R;
import com.example.travelapp.entity.FriendC;

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


public class SearchFriendActivity extends AppCompatActivity {

    private String username;//当前的用户名

    private List<FriendC> Friends;//当前用户的好友

    private List<FriendC> SearchResult;//搜索结果

    //控件
    EditText SearchText;
    private RecyclerView ShowRe;
    private ShowAdapter ShowAp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_friend);
        //去掉标题栏
        getSupportActionBar().hide();
        this.SearchResult = new ArrayList<>();
        //获取控件
        SearchText = (EditText)findViewById(R.id.search_friend_edit);
        ShowRe = (RecyclerView)findViewById(R.id.search_friend_showList);
        //获取来自FFriend的intent的数据
        this.username = getIntent().getStringExtra("username");
        this.Friends = (List<FriendC>)getIntent().getSerializableExtra("Friends");
        //配置rv
        ShowRe.setLayoutManager(new LinearLayoutManager(SearchFriendActivity.this));
        ShowAp = new ShowAdapter(this.SearchResult);
        ShowRe.setAdapter(ShowAp);
        //配置编辑框事件
        SearchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                ;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ;
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateAll();
            }
        });
    }


    private class ShowHolder extends RecyclerView.ViewHolder{
        //单元控件
        RoundImageView mUnitIcon;
        TextView mUnitUsername;
        TextView mUnitSignature;
        ImageButton mUnitAddButton;

        public ShowHolder(@NonNull View itemView) {
            super(itemView);
            mUnitIcon = (RoundImageView)itemView.findViewById(R.id.search_unit_icon);
            mUnitUsername = (TextView)itemView.findViewById(R.id.search_unit_username);
            mUnitSignature = (TextView)itemView.findViewById(R.id.search_unit_signature);
            mUnitAddButton = (ImageButton) itemView.findViewById(R.id.search_unit_add_button);
            mUnitAddButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AddFriendToDatabase(mUnitUsername.getText().toString());
                }
            });
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public void repaint(FriendC resultPeople){
            this.mUnitUsername.setText(resultPeople.getUsername());
            Drawable dr_add = getDrawable(R.drawable.ic_add2);
            mUnitAddButton.setBackground(dr_add);
            for(int i = 0;i < Friends.size();i++){
                if(resultPeople.getUsername().equals(Friends.get(i).getUsername())){
                    Drawable dr_added = getDrawable(R.drawable.ic_added);
                    mUnitAddButton.setBackground(dr_added);
                    mUnitAddButton.setClickable(false);
                }
            }

        }
    };

    private class ShowAdapter extends RecyclerView.Adapter<ShowHolder>{

        private List<FriendC> SearchResults;

        public ShowAdapter(List<FriendC> SearchResults){
            this.SearchResults = SearchResults;
        }

        @NonNull
        @Override
        public ShowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(SearchFriendActivity.this);
            View view = layoutInflater.inflate(R.layout.search_friend_unit,parent,false);
            return new ShowHolder(view);
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onBindViewHolder(@NonNull ShowHolder holder, int position) {
            FriendC resultPeople = SearchResults.get(position);
            holder.repaint(resultPeople);
        }

        @Override
        public int getItemCount() {
            return SearchResults.size();
        }
    }

    //根据当前搜索框中的内容，匹配返回搜索结果
    public void GetSearchResultFromDatabase(){
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                String url = "http://121.196.149.163:8080/dzwblog/SearchFriend_server";

                //创建HTTP对象
                HttpPost httpRequset = new HttpPost(url);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("username", username));
                if(SearchText.getText().toString().equals(""))
                    return ;
                params.add(new BasicNameValuePair("search_text",SearchText.getText().toString()));

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
                        SearchResult.clear();
                        for(int i = 0;i < myjson.length();i++){
                            JSONObject temp_ob = myjson.getJSONObject(i);
                            if(!temp_ob.getString("friendname").equals(username)) {
                                FriendC temp_Result = new FriendC(temp_ob.getString("friendname"));
                                SearchResult.add(temp_Result);
                            }
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                Runnable t = new Runnable() {
                    public void run() {
                        ShowAp.notifyDataSetChanged();
                    }

                };
                runOnUiThread(t);
            }
        }).start();
    }

    //添加好友，只发送请求，不更新数据和UI
    public void AddFriendToDatabase(final String otherUsername){
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                String url = "http://121.196.149.163:8080/dzwblog/AddFriend_server";
                //创建HTTP对象
                HttpPost httpRequset = new HttpPost(url);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("user1", username));
                params.add(new BasicNameValuePair("user2", otherUsername));
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
                        boolean result = Boolean.parseBoolean(myjson.get("result").toString());
                        //TODO 是否需要有发送成功与失败的处理
                        GetSearchResultFromDatabase();
                    }catch(Exception e){
                        e.printStackTrace();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                updateAll();
            }
        }).start();
    }

    //更新好友列表
    private void updateAll(){
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
                        ShowAp.notifyDataSetChanged();
                    }

                };
                runOnUiThread(t);
                GetSearchResultFromDatabase();


            }
        }).start();
    }


}