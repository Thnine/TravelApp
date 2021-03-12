package com.example.travelapp.subActivity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.travelapp.Fragment.TabFragment.FFriend;
import com.example.travelapp.MainActivity;
import com.example.travelapp.R;
import com.example.travelapp.entity.NoteC;

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

public class PlanInfoActivity extends AppCompatActivity {
    int plan_id;
    List<NoteC> Notes;
    //控件
    private ImageButton NoteAddButton;
    private RecyclerView Noterv;
    private NoteAdapter Noteap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_info);
        //隐藏标题栏
        getSupportActionBar().hide();
        //获取数据
        plan_id = getIntent().getIntExtra("plan_id",0);
        //数据初始化
        Notes = new ArrayList<>();

        NoteAddButton = findViewById(R.id.plan_note_add_button);
        NoteAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText editText = new EditText(PlanInfoActivity.this);
                AlertDialog.Builder inputDialog =
                        new AlertDialog.Builder(PlanInfoActivity.this);
                inputDialog.setTitle("输入笔记").setView(editText);
                inputDialog.setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String note_text = editText.getText().toString();
                                AddNote(note_text,plan_id);
                            }
                        }).show();
            }
        });
        Noterv = (RecyclerView)findViewById(R.id.plan_info_note_re);
        Noterv.setLayoutManager(new LinearLayoutManager(this));
        Noteap = new NoteAdapter(this.Notes);
        Noterv.setAdapter(Noteap);
        GetNote(plan_id);
    }

    /**
     * RecycleView 控件
     */
    private class NoteHolder extends RecyclerView.ViewHolder{

        private ptrNoteC myptrnote;
        //控件
        RadioButton NoteRadio;
        TextView NoteText;
        public NoteHolder(@NonNull View itemView) {
            super(itemView);
            NoteRadio = (RadioButton)itemView.findViewById(R.id.plan_note_finish_radio);
            NoteText = (TextView)itemView.findViewById(R.id.plan_note_text);
            myptrnote = new ptrNoteC();
        }

        //重绘
        public void repaint(NoteC mynote){
            this.myptrnote.mynote = mynote;
            NoteRadio.setChecked(mynote.isNote_isEx());
            NoteRadio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    updateNote(myptrnote.mynote.getNote_id(),isChecked);
                }
            });
            Log.d("PlanInfoActivity","text :" + mynote.getNote_text());
            NoteText.setText(mynote.getNote_text());
            if(mynote.isNote_isEx()){
                NoteText.setPaintFlags(NoteText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
            else {
                NoteText.setPaintFlags(NoteText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }

        }
        private class ptrNoteC{
            public NoteC mynote;
        }
    }

    private class NoteAdapter extends RecyclerView.Adapter<NoteHolder>{

        //笔记信息
        List<NoteC> Notes;

        public NoteAdapter(List<NoteC> Notes){
            this.Notes = Notes;
        }

        @NonNull
        @Override
        public NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(PlanInfoActivity.this);
            View view = layoutInflater.inflate(R.layout.plan_info_note_unit,parent,false);
            return new NoteHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull NoteHolder holder, int position) {
            NoteC mynote = this.Notes.get(position);
            holder.repaint(mynote);
        }

        @Override
        public int getItemCount() {
            return Notes.size();
        }
    }


    /**
     *笔记网络功能
     */

    //上传笔记
    public void AddNote(final String note_text, final int plan_id){
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                //获取账户名和密码
                String url = "http://121.196.149.163:8080/dzwblog/AddNote_server";
                //创建HTTP对象
                HttpPost httpRequset = new HttpPost(url);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("note_text", note_text));
                params.add(new BasicNameValuePair("plan_id",Integer.toString(plan_id)));
                try{
                    //装入数据和设置数据格式
                    httpRequset.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));
                    //发出请求
                    CloseableHttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpRequset);
                    //获取服务器响应的资源

                    String line=null;
                    JSONObject resultJson=null;
                    StringBuilder entityStringBuilder=new StringBuilder();
                    try {
                        BufferedReader b = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), "UTF-8"),8*1024);
                        while ((line=b.readLine())!=null) {
                            entityStringBuilder.append(line+"/n");
                        }
                        JSONObject myjson = new JSONObject(entityStringBuilder.toString());
                        boolean result = Boolean.parseBoolean(myjson.get("result").toString());
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                GetNote(plan_id);
            }
        }).start();
    }

    //接收笔记
    public void GetNote(final int plan_id){
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                //获取账户名和密码
                String url = "http://121.196.149.163:8080/dzwblog/GetNote_server";
                //创建HTTP对象
                HttpPost httpRequset = new HttpPost(url);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("plan_id",Integer.toString(plan_id)));
                try{
                    //装入数据和设置数据格式
                    httpRequset.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));
                    //发出请求
                    CloseableHttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpRequset);
                    //获取服务器响应的资源

                    String line=null;
                    JSONObject resultJson=null;
                    StringBuilder entityStringBuilder=new StringBuilder();
                    try {
                        BufferedReader b = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), "UTF-8"),8*1024);
                        while ((line=b.readLine())!=null) {
                            entityStringBuilder.append(line+"/n");
                        }
                        JSONArray myjson = new JSONArray(entityStringBuilder.toString());
                        Notes.clear();
                        for(int i = 0; i < myjson.length();i++){
                            JSONObject temp_ob = myjson.getJSONObject(i);
                            Notes.add(new NoteC(temp_ob.getString("note_text"),temp_ob.getBoolean("note_isEx"),temp_ob.getInt("note_id")));
                        }

                    }catch(Exception e){
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Runnable t = new Runnable() {
                    public void run() {
                        Noteap.notifyDataSetChanged();
                    }

                };
                runOnUiThread(t);
            }
        }).start();
    }

    //更新笔记状态
    public void updateNote(final int note_id, final boolean state){
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                //获取账户名和密码
                String url = "http://121.196.149.163:8080/dzwblog/UpdateNote_server";
                //创建HTTP对象
                HttpPost httpRequset = new HttpPost(url);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("note_id",Integer.toString(note_id)));
                params.add(new BasicNameValuePair("note_state",Boolean.toString(state)));
                try{
                    //装入数据和设置数据格式
                    httpRequset.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));
                    //发出请求
                    CloseableHttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpRequset);
                    //获取服务器响应的资源

                    String line=null;
                    JSONObject resultJson=null;
                    StringBuilder entityStringBuilder=new StringBuilder();
                    try {
                        BufferedReader b = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), "UTF-8"),8*1024);
                        while ((line=b.readLine())!=null) {
                            entityStringBuilder.append(line+"/n");
                        }
                        JSONObject myjson = new JSONObject(entityStringBuilder.toString());
                        boolean result = Boolean.parseBoolean(myjson.get("result").toString());
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                GetNote(plan_id);
            }
        }).start();
    }

}