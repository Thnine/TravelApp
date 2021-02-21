/*
 * Copyright (c) 2015, 张涛.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kymjs.chat;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.sun.jna.platform.unix.Resource;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.kymjs.chat.adapter.ChatAdapter;
import org.kymjs.chat.bean.Emojicon;
import org.kymjs.chat.bean.Faceicon;
import org.kymjs.chat.bean.Message;
import org.kymjs.chat.emoji.DisplayRules;
import org.kymjs.chat.widget.KJChatKeyboard;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * 聊天主界面
 */
public class ChatActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_GETIMAGE_BYSDCARD = 0x1;

    public KJChatKeyboard box;
    public ListView mRealListView;

    public boolean isGroup;
    public String from_username;
    public String to_username;
    public int group_id;

    List<Message> datas = new ArrayList<Message>();
    public ChatAdapter adapter;

    public TextView user_info_text;
    public ImageButton ResourceButton;

    public JSONArray myjson;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        box = (KJChatKeyboard) findViewById(R.id.chat_msg_input_box);
        mRealListView = (ListView) findViewById(R.id.chat_listview);
        isGroup = getIntent().getBooleanExtra("isGroup",false);
        from_username = getIntent().getStringExtra("from_username");
        if(isGroup){
            group_id = getIntent().getIntExtra("group_id",0);
        }
        else {
            to_username = getIntent().getStringExtra("to_username");
        }
        mRealListView.setSelector(android.R.color.transparent);
        initMessageInputToolBox();
        initListView();
        //去掉标题栏
        getSupportActionBar().hide();
        //设置用户信息
        this.user_info_text = (TextView) findViewById(R.id.activity_chat_userinfo);
        user_info_text.setText(this.to_username);
        ResourceButton = (ImageButton)findViewById(R.id.activity_chat_document_button);
        ResourceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatActivity.this, ResourceActivity.class);
                intent.putExtra("group_id",group_id);
                intent.putExtra("username",from_username);
                startActivity(intent);

            }
        });
        if(!isGroup){
            ResourceButton.setVisibility(View.GONE);
        }
    }

    /**
    @Override
    protected void onDestroy() {
        //停止自更新线程
        this.AutoUpdateMessageThread.stop();
        super.onDestroy();
    }**/

    public void initMessageInputToolBox() {
        /**重构发送事件**/
        box.setOnOperationListener(new OnOperationListener() {
            @Override
            public void send(String content) {
                Message message = new Message(Message.MSG_TYPE_TEXT, Message.MSG_STATE_SUCCESS,
                        from_username, "avatar", isGroup?"":to_username,
                        "avatar", content, true, true, new Date());
                UploadMessageToDatabase(message);
                GetMessageFromDatabase();
                adapter.refresh(datas);
            }

            @Override
            public void selectedFace(Faceicon content) {
                Message message = new Message(Message.MSG_TYPE_FACE, Message.MSG_STATE_SUCCESS,
                        "Tom", "avatar", "Jerry", "avatar", content.getPath(), true, true, new
                        Date());
                datas.add(message);
                adapter.refresh(datas);
                createReplayMsg(message);
            }

            @Override
            public void selectedEmoji(Emojicon emoji) {
                Log.d("ChatActivity","selectedEmoji has been executed!");
                box.getEditTextBox().append(emoji.getValue());
                Log.d("ChatActivity", "the content is" + String.valueOf(box.getEditTextBox().getText()));
            }

            @Override
            public void selectedBackSpace(Emojicon back) {
                DisplayRules.backspace(box.getEditTextBox());
            }

            @Override
            public void selectedFunction(int index) {
                switch (index) {
                    case 0:
                        goToAlbum();
                        break;
                    case 1:
                        Toast.makeText(getApplication(), "跳转相机，只做演示", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        List<String> faceCagegory = new ArrayList<>();
        File faceList = new File("");
        if (faceList.isDirectory()) {
            File[] faceFolderArray = faceList.listFiles();
            for (File folder : faceFolderArray) {
                if (!folder.isHidden()) {
                    faceCagegory.add(folder.getAbsolutePath());
                }
            }
        }

        box.setFaceData(faceCagegory);
        mRealListView.setOnTouchListener(getOnTouchListener());
    }

    public void initListView() {
        byte[] emoji = new byte[]{
                (byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x81
        };
        adapter = new ChatAdapter(this, datas, getOnChatItemClickListener());
        mRealListView.setAdapter(adapter);
        GetMessageFromDatabase();
        //自动更新线程

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    GetMessageFromDatabase();
                }
            }
        }).start();
    }

    public void createReplayMsg(Message message) {
        final Message reMessage = new Message(message.getType(), Message.MSG_STATE_SUCCESS, "Tom",
                "avatar", "Jerry", "avatar", message.getType() == Message.MSG_TYPE_TEXT ? "返回:"
                + message.getContent() : message.getContent(), false,
                true, new Date());
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000 * (new Random().nextInt(3) + 1));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            datas.add(reMessage);
                            adapter.refresh(datas);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    //更新数据，更新UI上的信息
    public void GetMessageFromDatabase(){
        //启动一个更新线程
        if(!isGroup) {
            new Thread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void run() {
                    String url = "http://121.196.149.163:8080/dzwblog/GetMessage_server";
                    //创建HTTP对象
                    HttpPost httpRequset = new HttpPost(url);
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("from_username", from_username));
                    params.add(new BasicNameValuePair("to_username", to_username));

                    try {
                        //装入数据和设置数据格式
                        httpRequset.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));
                        //发出请求
                        CloseableHttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpRequset);
                        //获取服务器响应的资源

                        String line = null;
                        StringBuilder entityStringBuilder = new StringBuilder();
                        BufferedReader b = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), "UTF-8"), 8 * 1024);
                        while ((line = b.readLine()) != null) {
                            entityStringBuilder.append(line + "/n");
                        }
                        myjson = new JSONArray(entityStringBuilder.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Runnable t = new Runnable() {
                        public void run() {
                            //处理Array数据
                            datas.clear();
                            try {

                                for (int i = 0; i < myjson.length(); i++) {
                                    JSONObject temp_ob = myjson.getJSONObject(i);
                                    String content = temp_ob.getString("content");
                                    String s_from_username = temp_ob.getString("message_from_username");
                                    String s_to_username = temp_ob.getString("message_to_username");
                                    Timestamp message_time = Timestamp.valueOf(temp_ob.getString("message_time"));
                                    Message toadd;
                                    if (s_from_username.equals(from_username)) {
                                        toadd = new Message(Message.MSG_TYPE_TEXT, Message.MSG_STATE_SUCCESS, s_from_username,
                                                "avatar", s_to_username,
                                                "avatar", content, true, true, (Date) message_time);
                                    } else {
                                        toadd = new Message(Message.MSG_TYPE_TEXT, Message.MSG_STATE_SUCCESS, s_from_username,
                                                "avatar", s_to_username,
                                                "avatar", content, false, true, (Date) message_time);
                                    }
                                    datas.add(toadd);
                                }
                                adapter.refresh(datas);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    };
                    runOnUiThread(t);
                }
            }).start();
        }
        else{
            //群组聊天模式
            new Thread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void run() {
                    String url = "http://121.196.149.163:8080/dzwblog/GetGroupMessage_server";
                    //创建HTTP对象
                    HttpPost httpRequset = new HttpPost(url);
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("group_id",Integer.toString(group_id)));

                    try {
                        //装入数据和设置数据格式
                        httpRequset.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));
                        //发出请求
                        CloseableHttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpRequset);
                        //获取服务器响应的资源

                        String line = null;
                        StringBuilder entityStringBuilder = new StringBuilder();
                        BufferedReader b = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), "UTF-8"), 8 * 1024);
                        while ((line = b.readLine()) != null) {
                            entityStringBuilder.append(line + "/n");
                        }
                        myjson = new JSONArray(entityStringBuilder.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Runnable t = new Runnable() {
                        public void run() {
                            //处理Array数据
                            datas.clear();
                            try {

                                for (int i = 0; i < myjson.length(); i++) {
                                    JSONObject temp_ob = myjson.getJSONObject(i);
                                    String content = temp_ob.getString("content");
                                    String s_from_username = temp_ob.getString("message_from_username");
                                    Timestamp message_time = Timestamp.valueOf(temp_ob.getString("message_time"));
                                    Message toadd;
                                    if (s_from_username.equals(from_username)) {
                                        toadd = new Message(Message.MSG_TYPE_TEXT, Message.MSG_STATE_SUCCESS, s_from_username,
                                                "avatar", "",
                                                "avatar", content, true, true, (Date) message_time);
                                    } else {
                                        toadd = new Message(Message.MSG_TYPE_TEXT, Message.MSG_STATE_SUCCESS, s_from_username,
                                                "avatar", "",
                                                "avatar", content, false, true, (Date) message_time);
                                    }
                                    datas.add(toadd);
                                }
                                adapter.refresh(datas);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    };
                    runOnUiThread(t);
                }
            }).start();
        }
    }

    //上传message
    public void UploadMessageToDatabase(final Message message){
        //启动一个更新线程

        if(!isGroup) {
            new Thread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void run() {
                    String url = "http://121.196.149.163:8080/dzwblog/AddMessage_server";
                    //创建HTTP对象
                    HttpPost httpRequset = new HttpPost(url);
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    Log.d("ChatActivity","From_user" + message.getFromUserName());
                    Log.d("ChatActivity","To_user" + message.getToUserName());
                    params.add(new BasicNameValuePair("from_username", message.getFromUserName()));
                    params.add(new BasicNameValuePair("to_username", message.getToUserName()));
                    params.add(new BasicNameValuePair("content", message.getContent()));
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
                            boolean result = Boolean.parseBoolean(myjson.get("result").toString());
                            Log.d("ChatActivity","result:" + result);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    GetMessageFromDatabase();
                }
            }).start();
        }
        else
        {
            new Thread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void run() {
                    String url = "http://121.196.149.163:8080/dzwblog/AddGroupMessage_server";
                    //创建HTTP对象
                    HttpPost httpRequset = new HttpPost(url);
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    Log.d("ChatActivity","From_user" + message.getFromUserName());
                    Log.d("ChatActivity","group_id" + group_id);
                    params.add(new BasicNameValuePair("from_username", message.getFromUserName()));
                    params.add(new BasicNameValuePair("group_id", Integer.toString(group_id)));
                    params.add(new BasicNameValuePair("content", message.getContent()));
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
                            boolean result = Boolean.parseBoolean(myjson.get("result").toString());
                            //TODO 是否需要有发送成功与失败的处理
                            Log.d("ChatActivity","result:" + result);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    GetMessageFromDatabase();
                }
            }).start();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && box.isShow()) {
            box.hideLayout();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }


    /**
     * 跳转到选择相册界面
     */
    public void goToAlbum() {
        Intent intent;
        intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "选择图片"),
                REQUEST_CODE_GETIMAGE_BYSDCARD);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_CODE_GETIMAGE_BYSDCARD) {
            Uri dataUri = data.getData();
            if (dataUri != null) {
                File file = FileUtils.uri2File(ChatActivity.this, dataUri);
                Message message = new Message(Message.MSG_TYPE_PHOTO, Message.MSG_STATE_SUCCESS,
                        "Tom", "avatar", "Jerry",
                        "avatar", file.getAbsolutePath(), true, true, new Date());
                datas.add(message);
                adapter.refresh(datas);
            }
        }
    }

    /**
     * 若软键盘或表情键盘弹起，点击上端空白处应该隐藏输入法键盘
     *
     * @return 会隐藏输入法键盘的触摸事件监听器
     */
    private View.OnTouchListener getOnTouchListener() {
        return new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                box.hideLayout();
                box.hideKeyboard(ChatActivity.this);
                return false;
            }
        };
    }

    /**
     * @return 聊天列表内存点击事件监听器
     */
    private OnChatItemClickListener getOnChatItemClickListener() {
        return new OnChatItemClickListener() {
            @Override
            public void onPhotoClick(int position) {
                Log.d("debug", datas.get(position).getContent() + "点击图片的");
                Toast.makeText(ChatActivity.this, datas.get(position).getContent() + "点击图片的", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTextClick(int position) {
            }

            @Override
            public void onFaceClick(int position) {
            }
        };
    }

    /**
     * 聊天列表中对内容的点击事件监听
     */
    public interface OnChatItemClickListener {
        void onPhotoClick(int position);

        void onTextClick(int position);

        void onFaceClick(int position);
    }
}
