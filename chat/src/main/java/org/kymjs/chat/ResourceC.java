package org.kymjs.chat;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

public class ResourceC {

    int file_id;
    int group_id;
    String user;
    String filename;
    String suffix;
    File file;
    long size;//byte为单位

    public ResourceC(int file_id, int group_id, String user, String Allfilename, long size){
        this.file_id = file_id;
        this.group_id = group_id;
        this.user = user;
        this.file = new File("/data/data/com.example.travelapp/files/"+Allfilename);
        filename = Allfilename.split("\\.(?=[^\\.]+$)")[0];
        suffix = Allfilename.split("\\.(?=[^\\.]+$)")[1];
        this.size = size;
    }

    public ResourceC(File file){
        this.file = file;
        this.filename = this.file.getName().split("\\.(?=[^\\.]+$)")[0];
        this.suffix = this.file.getName().split("\\.(?=[^\\.]+$)")[1];
        this.size = this.file.length();
    }

    //属性函数
    public void setGroup_id(int group_id) {
        this.group_id = group_id;
    }

    public int getGroup_id() {
        return group_id;
    }

    public File getFile() {
        return file;
    }

    public int getFile_id() {
        return file_id;
    }

    public String getFilename() {
        return filename;
    }

    public String getSuffix() {
        return suffix;
    }

    public String getUser() {
        return user;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setFile_id(int file_id) {
        this.file_id = file_id;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getSizeStr(){
        if(size < 1024){
            return size + "B";
        }
        else if(size < 1024*1024){
            double result = 1.0 * size / 1024;
            DecimalFormat df = new DecimalFormat("#.00");
            return  df.format(result) + "KB";
        }
        else{
            double result = 1.0 * size / (1024 * 1024);
            DecimalFormat df = new DecimalFormat("#.00");
            return  df.format(result) + "MB";
        }
    }

    public void openFile(Context context){
        new load_Thread(this.file_id,this.file,context).start();
    }

    //加载线程
    private class load_Thread extends Thread {

        private int file_id;
        private File file;
        private Context context;

        public load_Thread(int file_id,File file,Context context){
            super();
            this.file_id = file_id;
            this.file = file;
            this.context = context;
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void run() {
            super.run();
            if (!this.file.exists()) {
                String url = "http://121.196.149.163:8080/dzwblog/GetResource_server";
                //创建HTTP对象
                HttpPost httpRequset = new HttpPost(url);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("resource_id", Integer.toString(this.file_id)));

                try {
                    //装入数据和设置数据格式
                    httpRequset.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));
                    //发出请求
                    CloseableHttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpRequset);
                    //获取服务器响应的资源

                    String line = null;
                    JSONObject resultJson = null;
                    StringBuilder entityStringBuilder = new StringBuilder();
                    try {
                        BufferedReader b = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), "UTF-8"), 8 * 1024);
                        while ((line = b.readLine()) != null) {
                            entityStringBuilder.append(line + "/n");
                        }
                        JSONObject myjson = new JSONObject(entityStringBuilder.toString());
                        String temp_resource = myjson.getString("resource");
                        byte[] toT = new byte[0];
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            toT = Base64.getDecoder().decode(temp_resource);
                        }
                        //byte数组转文件
                        //开始传输文件
                        BufferedOutputStream bos = null;
                        FileOutputStream fos = null;
                        fos = new FileOutputStream(this.file);
                        bos = new BufferedOutputStream(fos);
                        bos.write(toT);
                        bos.flush();
                        bos.close();
                        fos.close();

                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //打开文件
            this.openFile(this.file.getPath());

            }

        /**
         * 打开文件
         * @param filePath 文件的全路径，包括到文件名
         */
        private void openFile(String filePath) {
            File file = new File(filePath);
            if (!file.exists()){
                //如果文件不存在
                Toast.makeText(context, "打开失败，原因：文件已经被移动或者删除", Toast.LENGTH_SHORT).show();
                return;
            }
            /* 取得扩展名 */
            String end = file.getName().substring(file.getName().lastIndexOf(".") + 1, file.getName().length()).toLowerCase(Locale.getDefault());
            /* 依扩展名的类型决定MimeType */
            Intent intent = null;
            if (end.equals("m4a") || end.equals("mp3") || end.equals("mid") || end.equals("xmf") || end.equals("ogg") || end.equals("wav")) {
                intent =  getVideoFileIntent(file);
            } else if (end.equals("3gp") || end.equals("mp4")) {
                intent = getAudioFileIntent(file);
            } else if (end.equals("jpg") || end.equals("gif") || end.equals("png") || end.equals("jpeg") || end.equals("bmp")) {
                intent =getImageFileIntent(this.file);
            } else if (end.equals("apk")) {
                intent = getApkFileIntent(file);
            }else if (end.equals("html") || end.equals("htm")){
                intent = getHtmlFileIntent(file);
            } else if (end.equals("ppt")) {
                intent = getPptFileIntent(file);
            } else if (end.equals("xls") || end.equals("xlsx")) {
                intent = getExcelFileIntent(file);
            } else if (end.equals("doc")) {
                intent = getWordFileIntent(file);
            } else if (end.equals("pdf")) {
                intent = getPdfFileIntent(file);
            } else if (end.equals("chm")) {
                intent = getChmFileIntent(file);
            } else if (end.equals("txt")) {
                intent = getTextFileIntent(file);
            } else {
                intent = getUnKnownFileIntent(file);
            }
            context.startActivity(intent);
        }


        public Uri getUri(File file) {
            Uri uri = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //判断版本是否在7.0以上
                uri =
                        FileProvider.getUriForFile(context,
                                context.getPackageName() + ".provider",
                                file);
            } else {
                uri = Uri.fromFile(file);
            }
            return uri;
        }

        /**
         * 文件打开的content
         */
        // Android获取一个用于打开APK文件的intent
        public Intent getApkFileIntent(File file) {
            Intent intent = null;
            try {
                Uri uri = getUri(file);
                intent = new Intent(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(uri, "application/vnd.android.package-archive");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return intent;
        }

        // Android获取一个用于打开VIDEO文件的intent
        public Intent getVideoFileIntent(File file) {
            Intent intent = null;
            try {
                Uri uri = getUri(file);
                intent = new Intent(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.putExtra("oneshot", 0);
                intent.putExtra("configchange", 0);
                intent.setDataAndType(uri, "video/*");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return intent;
        }

        // Android获取一个用于打开AUDIO文件的intent
        public Intent getAudioFileIntent(File file) {
            Intent intent = null;
            try {
                Uri uri = getUri(file);
                intent = new Intent(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.putExtra("oneshot", 0);
                intent.putExtra("configchange", 0);
                intent.setDataAndType(uri, "audio/*");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return intent;
        }

        // Android获取一个用于打开Html文件的intent
        public Intent getHtmlFileIntent(File file) {
            Uri uri = Uri.parse(file.getPath()).buildUpon().encodedAuthority("com.android.htmlfileprovider").scheme("content")
                    .encodedPath(file.getPath()).build();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "text/html");
            return intent;
        }

        // Android获取一个用于打开图片文件的intent
        public Intent getImageFileIntent(File file) {
            Intent intent = null;
            try {
                Uri uri = getUri(file);
                intent = new Intent(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(uri, "image/*");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return intent;
        }

        // Android获取一个用于打开PPT文件的intent
        public Intent getPptFileIntent(File file) {
            Intent intent = null;
            try {
                Uri uri = getUri(file);
                intent = new Intent(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return intent;
        }

        // Android获取一个用于打开Excel文件的intent
        public Intent getExcelFileIntent(File file) {
            Intent intent = null;
            try {
                Uri uri = getUri(file);
                intent = new Intent(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(uri, "application/vnd.ms-excel");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return intent;
        }

        // Android获取一个用于打开doc,Word文件的intent
        public Intent getWordFileIntent(File file) {
            Intent intent = null;
            try {
                Uri uri = getUri(file);
                intent = new Intent(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(uri, "application/msword");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return intent;
        }

        // Android获取一个用于打开CHM文件的intent
        public Intent getChmFileIntent(File file) {
            Intent intent = null;
            try {
                Uri uri = getUri(file);
                intent = new Intent(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(uri, "application/x-chm");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return intent;
        }

        // Android获取一个用于打开文本文件的intent
        public Intent getTextFileIntent(File file) {
            Intent intent = null;
            try {
                Uri uri = getUri(file);
                intent = new Intent(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(uri, "text/plain");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return intent;
        }

        // Android获取一个用于打开PDF文件的intent
        public Intent getPdfFileIntent(File file) {
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.addCategory("android.intent.category.DEFAULT");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri uri = getUri(file);
            intent.setDataAndType(uri, "application/pdf");
            return intent;
        }

        // Android获取一个用于打开未知文件的intent
        public Intent getUnKnownFileIntent(File file) {
            Intent intent = null;
            try {
                Uri uri = getUri(file);
                intent = new Intent(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(uri, "*/*");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return intent;
        }

    }



}





