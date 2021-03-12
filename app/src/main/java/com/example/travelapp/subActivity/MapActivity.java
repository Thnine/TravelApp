package com.example.travelapp.subActivity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.example.travelapp.Listener.MyLocationListener;
import com.example.travelapp.R;
import com.example.travelapp.entity.GroupC;
import com.example.travelapp.entity.PlanRecord;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity {
    private GroupC mygroup;
    private List<LatLng> locs;
    private MapView GroupInfoMap;
    private BaiduMap BaiduMapController;
    private LocationClient mLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        //隐藏任务栏
        getSupportActionBar().hide();
        //获取数据
        mygroup = (GroupC) getIntent().getSerializableExtra("mygroup");
        locs = getIntent().getParcelableArrayListExtra("locs");
        //初始化控件
        initBaiduMap();
    }

    @Override
    protected void onResume() {
        super.onResume();
        GroupInfoMap.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        GroupInfoMap.onPause();
    }

    @Override
    protected void onDestroy() {
        mLocationClient.stop();
        BaiduMapController.setMyLocationEnabled(false);
        GroupInfoMap.onDestroy();
        GroupInfoMap = null;
        super.onDestroy();
    }

    //初始化百度地图
    private void initBaiduMap(){
        //获取定位权限
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(this, permissions, 1);
        }
        //初始化地图
        GroupInfoMap= (MapView)findViewById(R.id.group_info_map_view);
        BaiduMapController =  GroupInfoMap.getMap();
        //初始化定位
        BaiduMapController.setMyLocationEnabled(true);
        //定位初始化
        mLocationClient = new LocationClient(this);
        //通过LocationClientOption设置LocationClient相关参数
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        option.setIsNeedAddress(true);
        //设置locationClientOption
        mLocationClient.setLocOption(option);
        //注册LocationListener监听器
        MyLocationListener myLocationListener = new MyLocationListener(GroupInfoMap,BaiduMapController);
        mLocationClient.registerLocationListener(myLocationListener);
        //开启地图定位图层
        mLocationClient.start();
        //绘制图标与折线
        for(int i = 0;i < mygroup.getPlanRecords().size();i++){
            getLocByAddress(mygroup.getPlanRecords().get(i));
        }
    }

    public void getLocByAddress(final PlanRecord myplan) {
        final StringBuilder json = new StringBuilder();
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                String key = "mGYWG3hZghwGkxsKhfRenQx7Zv9ccat2";
                String url = "http://api.map.baidu.com/geocoding/v3/";
                HttpPost httpRequset = new HttpPost(url);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("address", myplan.getPlanLoc().getProvince() + "省" + myplan.getPlanLoc().getCity()+"市"));
                params.add(new BasicNameValuePair("output", "json"));
                params.add(new BasicNameValuePair("ak", key));
                params.add(new BasicNameValuePair("callback", "showLocation"));
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
                        double longitude = myjson.getJSONObject("result").getJSONObject("location").getDouble("lng");
                        double latitude = myjson.getJSONObject("result").getJSONObject("location").getDouble("lat");
                        Log.d("GroupInfoActivity","log is：" + longitude + "lat is：" + latitude);
                        myplan.getPlanLoc().setLocation(latitude,longitude);
                        //绘制overlay
                        LatLng point = new LatLng(latitude,longitude);
                        BitmapDescriptor bitmap = BitmapDescriptorFactory
                                .fromResource(R.drawable.destination_png);
                        OverlayOptions Ooption = new MarkerOptions()
                                .position(point)
                                .icon(bitmap)
                                .scaleX(0.1f).scaleY(0.1f);
                        BaiduMapController.addOverlay(Ooption);
                        locs.add(point);

                        if(locs.size() == mygroup.getPlanRecords().size()){
                            Bitmap bitmap_0= BitmapFactory.decodeResource(getResources(), R.drawable.red_arrow);
                            Bitmap bitmap_= resizeImage(bitmap_0,16,64);
                            BitmapDescriptor mRedTexture=BitmapDescriptorFactory.fromBitmap(bitmap_);
                            List<BitmapDescriptor> textureList = new ArrayList<BitmapDescriptor>();
                            textureList.add(mRedTexture);
                            // 添加纹理图片对应的顺序
                            List<Integer> textureIndexs = new ArrayList<Integer>();
                            for (int i=0;i<locs.size();i++){
                                textureIndexs.add(0);
                            }
                            OverlayOptions mOverlayOptions = new PolylineOptions()
                                    .textureIndex(textureIndexs)//设置分段纹理index数组
                                    .customTextureList(textureList)//设置线段的纹理，建议纹理资源长宽均为2的n次方
                                    .dottedLine(true)
                                    .color(0xAAFF0000)
                                    .width(15)
                                    .points(locs);
                            Overlay mPolyline = BaiduMapController.addOverlay(mOverlayOptions);
                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    public Bitmap resizeImage(Bitmap bitmap, int w, int h)
    {
        Bitmap BitmapOrg = bitmap;
        int width = BitmapOrg.getWidth();
        int height = BitmapOrg.getHeight();
        int newWidth = w;
        int newHeight = h;

        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // if you want to rotate the Bitmap
        // matrix.postRotate(45);
        Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
                height, matrix, true);
        return resizedBitmap;
    }


}