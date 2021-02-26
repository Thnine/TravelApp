package org.kymjs.chat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class ResourceActivity extends AppCompatActivity {

    //数据信息
    private int group_id;
    private String username;
    private List<ResourceC> Rs;

    //控件信息
    private RecyclerView r_Rv;
    private ResourceAdapter r_Ap;
    private Button ResourceAddButton;
    //加载对话框
    LoadingDialog LD;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resource);
        getSupportActionBar().hide();
        //初始化数据
        group_id = getIntent().getIntExtra("group_id",0);
        username = getIntent().getStringExtra("username");
        Rs = new ArrayList<>();

        //初始化工具
        LD = new LoadingDialog(ResourceActivity.this);

        //初始化控件
        r_Rv = (RecyclerView) findViewById(R.id.resource_rv);
        r_Rv.setLayoutManager(new LinearLayoutManager(ResourceActivity.this));
        r_Ap = new ResourceAdapter(this.Rs);
        r_Rv.setAdapter(r_Ap);
        ResourceAddButton = (Button)findViewById(R.id.resource_upload_button);
        ResourceAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, 1);
            }
        });
        updateResource();
        /**
         * 动态申请权限
         */
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M
                && this.checkSelfPermission
                (Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
        {
            this.requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }

    }


    private class ResourceHolder extends RecyclerView.ViewHolder{
        //初始化控件
        ImageView r_image;
        TextView r_name;
        TextView r_size;
        TextView r_user;
        ResourceC myR;

        public ResourceHolder(@NonNull View itemView) {
            super(itemView);
            r_image = (ImageView)itemView.findViewById(R.id.resource_unit_image);
            r_name = (TextView)itemView.findViewById(R.id.resource_unit_name);
            r_size = (TextView)itemView.findViewById(R.id.resource_unit_size);
            r_user = (TextView)itemView.findViewById(R.id.resource_unit_user);
            r_image.setClickable(true);
            r_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    myR.openFile(LD);
                }
            });
        }
        public void repaint(ResourceC myr){
            String suffix = myr.getSuffix();
            this.myR = myr;
            if(suffix.equals("jpg") || suffix.equals("JPG") || suffix.equals("png") || suffix.equals("PNG")) {
                r_image.setImageResource(R.drawable.ic_picture);
            }
            else if(suffix.equals("mp3") || suffix.equals("MP3")){
                r_image.setImageResource(R.drawable.ic_mp3);
            }
            else if(suffix.equals("mp4") || suffix.equals("MP4")){
                r_image.setImageResource(R.drawable.ic_mp4);
            }
            else if(suffix.equals("zip") || suffix.equals("ZIP")){
                r_image.setImageResource(R.drawable.ic_zip);
            }
            else if(suffix.equals("rar") || suffix.equals("RAR")){
                r_image.setImageResource(R.drawable.ic_rar);
            }
            else if(suffix.equals("doc") || suffix.equals("docx")){
                r_image.setImageResource(R.drawable.ic_word);
            }
            else if(suffix.equals("xls") || suffix.equals("xlsx")){
                r_image.setImageResource(R.drawable.ic_excel);
            }
            else{
                r_image.setImageResource(R.drawable.ic_unknown);
            }
            r_name.setText(myr.getFilename());
            r_size.setText(myr.getSizeStr());
            r_user.setText(myr.getUser());

        }
    }

    private class ResourceAdapter extends RecyclerView.Adapter<ResourceHolder>{
        private List<ResourceC> Rs;

        public ResourceAdapter(List<ResourceC> Rs){
            this.Rs = Rs;
        }
        @NonNull
        @Override
        public ResourceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(ResourceActivity.this);
            View view = layoutInflater.inflate(R.layout.resource_unit,parent,false);
            return new ResourceHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ResourceHolder holder, int position) {
            holder.repaint(Rs.get(position));
        }

        @Override
        public int getItemCount() {
            return Rs.size();
        }
    }

    //更新资源框架
    private void updateResource(){
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                String url = "http://121.196.149.163:8080/dzwblog/GetResourceInfo_server";

                //创建HTTP对象
                HttpPost httpRequset = new HttpPost(url);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("group_id", "" + group_id));

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
                        Rs.clear();
                        for(int i = 0;i < myjson.length();i++){
                            JSONObject temp_ob = myjson.getJSONObject(i);
                            int resource_id = temp_ob.getInt("resource_id");
                            Long size = temp_ob.getLong("size");
                            String name = temp_ob.getString("resource_name");
                            String user = temp_ob.getString("username");
                            Rs.add(new ResourceC(resource_id,group_id,user,name,size));
                        }

                    }catch(Exception e){
                        e.printStackTrace();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                Runnable t = new Runnable() {
                    public void run() {
                        r_Ap.notifyDataSetChanged();
                    }

                };
                runOnUiThread(t);


            }
        }).start();
    }


    // 获取文件的真实路径
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            // 用户未选择任何文件，直接返回
            return;
        }
        Uri uri = data.getData(); // 获取用户选择文件的URI
        File file = getFilePathForN(uri);
        //包装成ResourceC
        Log.d("ResourceActivity",file.getPath());
        ResourceC myR = new ResourceC(file);
        LD.showLoadingDialog("正在上传");
        uploadResource(myR);
    }

    //上传资源
    private void uploadResource(final ResourceC myR){
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                String url = "http://121.196.149.163:8080/dzwblog/AddResource_server";

                //显示加载对话框
                boolean result = false;
                //创建HTTP对象
                HttpPost httpRequset = new HttpPost(url);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("username", username));
                params.add(new BasicNameValuePair("group_id",Integer.toString(group_id)));
                params.add(new BasicNameValuePair("resource_name",myR.getFilename() + "." + myR.getSuffix()));
                //读取文件为字节流
                byte[] buffer = null;
                try {
                    FileInputStream fis = new FileInputStream(myR.getFile());
                    ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
                    byte[] b = new byte[1000];
                    int n;
                    while ((n = fis.read(b)) != -1) {
                        bos.write(b, 0, n);
                    }
                    fis.close();
                    bos.close();
                    buffer = bos.toByteArray();
                    Log.d("ResourceActivity","length is:" + buffer.length);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    params.add(new BasicNameValuePair("resource", Base64.getEncoder().encodeToString(buffer)));
                }


                try {
                    //装入数据和设置数据格式
                    httpRequset.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));
                    //发出请求
                    CloseableHttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpRequset);
                    //获取服务器响应的资源
                    String line = null;
                    StringBuilder entityStringBuilder = new StringBuilder();
                    try {
                        BufferedReader b = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), "UTF-8"), 8 * 1024);
                        while ((line = b.readLine()) != null) {
                            entityStringBuilder.append(line + "/n");
                        }
                        JSONObject myjson = new JSONObject(entityStringBuilder.toString());
                        result = Boolean.parseBoolean(myjson.get("result").toString());
                        Log.d("ResourceActivity","result is:" + result);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                LD.closeLoadingDialog();
                updateResource();

            }
        }).start();
    }




    /**
     * 拷被要上传的文件
     * @param uri
     * @return
     */
    private File getFilePathForN(Uri uri) {
        try {
            Cursor returnCursor = getContentResolver().query(uri, null, null, null, null);
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            returnCursor.moveToFirst();
            String name = (returnCursor.getString(nameIndex));
            File file = new File(getFilesDir(), name);
            InputStream inputStream = getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(file);
            int read = 0;
            int maxBufferSize = 1 * 1024 * 1024;
            int bytesAvailable = inputStream.available();
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);
            final byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }
            returnCursor.close();
            inputStream.close();
            outputStream.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}