package com.example.travelapp.Fragment.FondTabFragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.travelapp.Control.RoundImageView;
import com.example.travelapp.Fragment.TabFragment.FFriend;
import com.example.travelapp.R;
import com.example.travelapp.entity.FriendC;
import com.example.travelapp.entity.StrategyC;
import com.example.travelapp.subActivity.StrategyAddActivity;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;


public class StrategyFondTabFragment extends Fragment {
    private List<StrategyC> ss;
    private String city;
    private String username;
    private Button AddStrategyButton;
    private RecyclerView Srv;
    private StrategyAdapter SAp;
    private SwipeRefreshLayout Sre;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            city = getArguments().getString("city");
            username = getArguments().getString("username");
            ss = new ArrayList<>();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_strategy_fond_tab, container, false);
        AddStrategyButton = (Button)v.findViewById(R.id.fond_add_strategy_button);
        Sre = (SwipeRefreshLayout)v.findViewById(R.id.strategy_re);
        Sre.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateStrategyFromDatabase();
            }
        });
        AddStrategyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), StrategyAddActivity.class);
                intent.putExtra("city",city);
                intent.putExtra("username",username);
                startActivity(intent);
            }
        });
        Srv = v.findViewById(R.id.strategy_rv);
        Srv.setLayoutManager(new LinearLayoutManager(getActivity()));
        SAp = new StrategyAdapter(this.ss);
        Srv.setAdapter(SAp);
        //更新
        updateStrategyFromDatabase();
        return v;
    }

    private class StrategyHolder extends RecyclerView.ViewHolder{
        //控件
        private TextView s_title;
        private TextView s_text;
        private RatingBar s_score;
        private TextView s_username;
        private RoundImageView s_icon;

        public StrategyHolder(@NonNull View itemView) {
            super(itemView);
            //初始化控件
            s_title = (TextView)itemView.findViewById(R.id.strategy_title);
            s_text = (TextView)itemView.findViewById(R.id.strategy_text);
            s_score = (RatingBar)itemView.findViewById(R.id.strategy_score);
            s_username = (TextView)itemView.findViewById(R.id.strategy_name);
            s_icon = (RoundImageView)itemView.findViewById(R.id.strategy_icon);
        }

        public void repaint(StrategyC mys){
            s_title.setText(mys.getTitle());
            s_text.setText(mys.getText());
            s_score.setRating(mys.getScore());
            s_username.setText(mys.getWriter());
        }
    }

    private class StrategyAdapter extends RecyclerView.Adapter<StrategyHolder>{
        private List<StrategyC> ss;

        public StrategyAdapter(List<StrategyC> ss){
            this.ss = ss;
        }
        @NonNull
        @Override
        public StrategyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.strategy_unit,parent,false);
            return new StrategyHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull StrategyHolder holder, int position) {
            holder.repaint(ss.get(position));
        }

        @Override
        public int getItemCount() {
            return ss.size();
        }
    }

    public void updateStrategyFromDatabase(){
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                String url = "http://121.196.149.163:8080/dzwblog/GetStrategy_server";

                //创建HTTP对象
                HttpPost httpRequset = new HttpPost(url);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("city", city));

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
                        ss.clear();
                        for(int i = 0;i < myjson.length();i++){
                            JSONObject temp_ob = myjson.getJSONObject(i);
                            String username = temp_ob.getString("username");
                            float score = (float)temp_ob.getDouble("score");
                            String title = temp_ob.getString("title");
                            String text = temp_ob.getString("text");
                            String city = temp_ob.getString("city");
                            StrategyC temp_s = new StrategyC(username,score,title,text,city);
                            ss.add(temp_s);
                        }

                    }catch(Exception e){
                        e.printStackTrace();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                Runnable t = new Runnable() {
                    public void run() {
                        Sre.setRefreshing(false);
                        SAp.notifyDataSetChanged();
                    }

                };
                getActivity().runOnUiThread(t);


            }
        }).start();
    }
}