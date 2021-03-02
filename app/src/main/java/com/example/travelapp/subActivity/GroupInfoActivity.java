package com.example.travelapp.subActivity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.example.travelapp.Control.RoundImageView;
import com.example.travelapp.Fragment.GroupFragment.GroupRoutePlanFragment;
import com.example.travelapp.Fragment.TabFragment.FFriend;
import com.example.travelapp.Listener.MyLocationListener;
import com.example.travelapp.R;
import com.example.travelapp.entity.FriendC;
import com.example.travelapp.entity.GroupC;
import com.example.travelapp.entity.PlanRecord;
import com.example.travelapp.utils.IconLoader;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.kymjs.chat.ChatActivity;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class GroupInfoActivity extends AppCompatActivity{
    private String username;
    private GroupC mygroup;
    private List<LatLng> locs;
    //控件
    private MapView GroupInfoMap;
    private BaiduMap BaiduMapController;
    private LocationClient mLocationClient;
    private RecyclerView GroupInfoMemberRv;
    private GroupInfoMemberAdapter GroupInfoMemberAp;
    private RecyclerView GroupInfoPlanRv;
    private GroupInfoPlanAdapter GroupInfoPlanAp;
    private Button EnterGroupChatButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);
        //初始化数据
        this.mygroup = (GroupC)getIntent().getSerializableExtra("mygroup");
        this.username = getIntent().getStringExtra("username");
        locs = new ArrayList<>();
        initBaiduMap();
        //隐藏任务栏
        getSupportActionBar().hide();
        EnterGroupChatButton = (Button)findViewById(R.id.group_info_enter_chat_button);
        EnterGroupChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupInfoActivity.this,ChatActivity.class);
                intent.putExtra("isGroup",true);
                intent.putExtra("group_id",mygroup.getGroup_id());
                intent.putExtra("from_username",username);
                startActivity(intent);
            }
        });
        //配置RE
        GroupInfoMemberRv = (RecyclerView)findViewById(R.id.group_info_member_rv);
        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(this);
        linearLayoutManager1.setOrientation(LinearLayoutManager.HORIZONTAL);
        GroupInfoMemberRv.setLayoutManager(linearLayoutManager1);
        GroupInfoMemberAp = new GroupInfoMemberAdapter(this.mygroup.getOwner(),this.mygroup.getMembers());
        GroupInfoMemberRv.setAdapter(GroupInfoMemberAp);

        GroupInfoPlanRv = (RecyclerView)findViewById(R.id.group_info_plan_rv);
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(this);
        linearLayoutManager2.setOrientation(LinearLayoutManager.HORIZONTAL);
        GroupInfoPlanRv.setLayoutManager(linearLayoutManager2);
        GroupInfoPlanAp = new GroupInfoPlanAdapter(this.mygroup.getPlanRecords());
        GroupInfoPlanRv.setAdapter(GroupInfoPlanAp);
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

        //配置Location权限
        int checkPermission = ContextCompat.checkSelfPermission(GroupInfoActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (checkPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(GroupInfoActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

        //通过LocationClientOption设置LocationClient相关参数
        LocationClientOption option = new LocationClientOption();
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

        //绘制图标
        for(int i = 0;i < mygroup.getPlanRecords().size();i++){
            getLocByAddress(mygroup.getPlanRecords().get(i));
        }

        //绘制折线
        //TODO


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
                             OverlayOptions mOverlayOptions = new PolylineOptions()
                                     .width(10)
                                     .color(0xAAFF0000)
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

    //移动图片到指定经纬度出

    //成员信息的RV配置
    private class GroupInfoMemberHolder extends RecyclerView.ViewHolder{
        private TextView MemberNameEdit;
        private RoundImageView MemberIcon;

        public GroupInfoMemberHolder(@NonNull View itemView) {
            super(itemView);
            MemberNameEdit = (TextView)itemView.findViewById(R.id.group_info_member_unit_name);
            MemberIcon = (RoundImageView)itemView.findViewById(R.id.group_info_member_unit_icon);
            //配置监听事件
            MemberIcon.setClickable(true);
            MemberIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!username.equals(MemberNameEdit.getText().toString())) {
                        Intent intent = new Intent(GroupInfoActivity.this, ChatActivity.class);
                        intent.putExtra("isGroup",false);
                        intent.putExtra("from_username", username);
                        intent.putExtra("to_username", MemberNameEdit.getText().toString());
                        startActivity(intent);
                    }
                }
            });

        }

        public void repaint(FriendC myfriend){
            MemberNameEdit.setText(myfriend.getUsername());
            IconLoader IL = new IconLoader(GroupInfoActivity.this);
            IL.OnBindInfo(this.MemberIcon,myfriend.getUsername());
            IL.reloadIcon();
        }
    }

    private class GroupInfoMemberAdapter extends RecyclerView.Adapter<GroupInfoMemberHolder>{

        private FriendC owner;

        private List<FriendC> members;

        public GroupInfoMemberAdapter(FriendC owner,List<FriendC> members){
            this.owner = owner;
            this.members = members;
        }

        @NonNull
        @Override
        public GroupInfoMemberHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(GroupInfoActivity.this);
            View view = layoutInflater.inflate(R.layout.group_info_member_unit,parent,false);
            return new GroupInfoMemberHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull GroupInfoMemberHolder holder, int position) {
            if(position == 0) {
                holder.repaint(owner);
            }
            else {
                holder.repaint(members.get(position-1));

            }
        }

        @Override
        public int getItemCount() {
            return members.size() + 1;
        }
    }

    //计划信息的RV配置
    private class GroupInfoPlanHolder extends RecyclerView.ViewHolder{
        private TextView PlanYear;
        private TextView PlanMonth;
        private TextView PlanDay;
        private TextView PlanProvince;
        private TextView PlanCity;
        private ImageView next;

        public GroupInfoPlanHolder(@NonNull View itemView) {
            super(itemView);
            PlanYear = (TextView)itemView.findViewById(R.id.group_info_plan_unit_year);
            PlanMonth = (TextView)itemView.findViewById(R.id.group_info_plan_unit_month);
            PlanDay = (TextView)itemView.findViewById(R.id.group_info_plan_unit_day);
            PlanProvince = (TextView)itemView.findViewById(R.id.group_info_plan_unit_province);
            PlanCity = (TextView)itemView.findViewById(R.id.group_info_plan_unit_city);
            next = (ImageView)itemView.findViewById(R.id.group_info_plan_unit_next);
        }
        public void repaint(PlanRecord myplan,int position){
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(myplan.getPlanDate());
            PlanYear.setText(Integer.toString(calendar.get(Calendar.YEAR)));
            PlanMonth.setText(Integer.toString(calendar.get(Calendar.MONTH)+1));
            PlanDay.setText(Integer.toString(calendar.get(Calendar.DAY_OF_MONTH)));
            PlanProvince.setText(myplan.getPlanLoc().getProvince());
            PlanCity.setText(myplan.getPlanLoc().getCity());
            if(position == mygroup.getPlanRecords().size() - 1){
                next.setVisibility(View.GONE);
            }
        }
    }

    private class GroupInfoPlanAdapter extends RecyclerView.Adapter<GroupInfoPlanHolder>{

        private List<PlanRecord> PlanRecords;

        public GroupInfoPlanAdapter(List<PlanRecord> PlanRecords){
            this.PlanRecords = PlanRecords;
        }

        @NonNull
        @Override
        public GroupInfoPlanHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(GroupInfoActivity.this);
            View view = layoutInflater.inflate(R.layout.group_info_plan_unit,parent,false);
            return new GroupInfoPlanHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull GroupInfoPlanHolder holder, int position) {
            holder.repaint(PlanRecords.get(position),position);
        }

        @Override
        public int getItemCount() {
            return PlanRecords.size();
        }
    }

}