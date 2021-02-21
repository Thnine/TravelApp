package com.example.travelapp.Fragment.FondTabFragment;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.baidu.mapapi.map.HoleOptions;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class SiteFondTabFragment extends Fragment {
    List<String> titles;
    List<String> texts;
    String province;
    String city;


    //初始化控件
    private RecyclerView Site_rv;
    private SiteAdapter Site_ap;


    @Override
    public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            titles = new ArrayList<>();
            texts = new ArrayList<>();
            city = getArguments().getString("city");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_site_fond_tab, container, false);
        //初始化控件
        Site_rv = v.findViewById(R.id.fond_site_rv);
        Site_rv.setLayoutManager(new LinearLayoutManager(getActivity()));
        Site_ap = new SiteAdapter(this.titles,this.texts);
        Site_rv.setAdapter(Site_ap);
        getSiteFromInternet();
        return v;
    }

    private class SiteHolder extends RecyclerView.ViewHolder{
        //控件信息
        private TextView site_title;
        private TextView site_text;
        public SiteHolder(@NonNull View itemView) {
            super(itemView);
            //初始化控件
            site_title = (TextView)itemView.findViewById(R.id.fond_site_unit_title);
            site_text = (TextView)itemView.findViewById(R.id.fond_site_unit_text);
        }
        public void repaint(String title,String text){
            this.site_title.setText(title);
            this.site_text.setText(text);
        }
    }

    public class SiteAdapter extends RecyclerView.Adapter<SiteHolder>{

        List<String> titles;
        List<String> texts;

        public SiteAdapter(List<String> titles,List<String> texts){
            this.texts = texts;
            this.titles = titles;
        }

        @NonNull
        @Override
        public SiteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.fond_site_unit,parent,false);
            return new SiteHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SiteHolder holder, int position) {
            holder.repaint(titles.get(position),texts.get(position));
        }

        @Override
        public int getItemCount() {
            return texts.size();
        }
    }

    public void getSiteFromInternet(){
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                String url = "http://api.tianapi.com/txapi/scenic/index";

                //创建HTTP对象
                HttpPost httpRequset = new HttpPost(url);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("key", "d18af9c7a1057b6a6ec27b9ac8e02aa9"));
                params.add(new BasicNameValuePair("num", "10"));
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
                        Log.d("SiteFondTabFragment",entityStringBuilder.toString());
                        JSONObject temp_ob = new JSONObject(entityStringBuilder.toString());
                        JSONArray temp_array = temp_ob.getJSONArray("newslist");
                        titles.clear();
                        texts.clear();
                        for(int i = 0;i < temp_array.length();i++){
                            titles.add(temp_array.getJSONObject(i).getString("name"));
                            texts.add(temp_array.getJSONObject(i).getString("content").replaceAll("<br>",""));
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Runnable t = new Runnable() {
                    public void run() {
                        Site_ap.notifyDataSetChanged();
                    }

                };
                getActivity().runOnUiThread(t);
            }
        }).start();
    }

}