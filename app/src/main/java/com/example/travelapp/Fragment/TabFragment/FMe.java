package com.example.travelapp.Fragment.TabFragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.travelapp.Control.RoundImageView;
import com.example.travelapp.R;
import com.example.travelapp.utils.IconLoader;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;


/**
 * 个人属性界面UI
 */

public class FMe extends Fragment {

    String username;


    RoundImageView myIcon;
    TextView TextUsername;

    Uri uri;
    IconLoader IL;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            username = getArguments().getString("username");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_f_me, container, false);
        //获取控件
        myIcon = (RoundImageView)v.findViewById(R.id.f_me_user_icon);
        myIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, null);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, 2);
            }
        });
        TextUsername = (TextView)v.findViewById(R.id.f_me_user_name);
        TextUsername.setText(username);

        //获取权限
        //获取读写文件权限
        if (Build.VERSION.SDK_INT >= 23) {
            int REQUEST_CODE_CONTACT = 101;
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE};
            //验证是否许可权限
            for (String str : permissions) {
                if (getContext().checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    //申请权限
                    this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                }
            }
        }
        IL = new IconLoader(this);
        IL.OnBindInfo(myIcon,username);
        IL.setIcon();
        IL.reloadIcon();

        return v;
    }

    //响应打开新窗口的结果的函数
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2) {
            // 从相册返回的数据
            if (data != null) {
                // 得到图片的全路径
                this.uri = data.getData();
                Log.d("FMe",uri.toString());
                uploadIcon();
            }
        }
    }

    //上传头像图片
    public void uploadIcon(){
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                String url = "http://121.196.149.163:8080/dzwblog/AddIcon_server";
                boolean result = false;
                //创建HTTP对象
                HttpPost httpRequset = new HttpPost(url);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("username", username));

                    try {

                        String[] proj = {MediaStore.Images.Media.DATA};
                        Cursor actualimagecursor = getActivity().managedQuery(uri, proj, null, null, null);
                        int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                        actualimagecursor.moveToFirst();
                        String img_path = actualimagecursor.getString(actual_image_column_index);
                        params.add(new BasicNameValuePair("suffix",img_path.split("\\.(?=[^\\.]+$)")[1]));
                        File file = new File(img_path);

                        //读取文件为字节流
                        byte[] buffer = null;
                        try {
                            FileInputStream fis = new FileInputStream(file);
                            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
                            byte[] b = new byte[1000];
                            int n;
                            while ((n = fis.read(b)) != -1) {
                                bos.write(b, 0, n);
                            }
                            fis.close();
                            bos.close();
                            buffer = bos.toByteArray();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        //传输数据
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            params.add(new BasicNameValuePair("icon",Base64.getEncoder().encodeToString(buffer)));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
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

                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("FMe","point1");
                            IL.reloadIcon();
                        }
                    });

                    if (result) {
                        Looper.prepare();
                        Toast.makeText(getContext(),R.string.change_icon_true,Toast.LENGTH_SHORT).show();
                        Looper.loop();

                    } else {
                        Looper.prepare();
                        Toast.makeText(getContext(),R.string.change_icon_false, Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }).start();
    }

}