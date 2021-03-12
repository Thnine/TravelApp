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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private RecyclerView GroupInfoMemberRv;
    private GroupInfoMemberAdapter GroupInfoMemberAp;
    private RecyclerView GroupInfoPlanRv;
    private GroupInfoPlanAdapter GroupInfoPlanAp;
    private Button EnterGroupChatButton;
    private Button EnterGroupMapButton;
    private TextView GroupNameText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);
        //初始化数据
        this.mygroup = (GroupC)getIntent().getSerializableExtra("mygroup");
        this.username = getIntent().getStringExtra("username");
        locs = new ArrayList<>();
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
        EnterGroupMapButton = (Button)findViewById(R.id.group_info_enter_map_button);
        EnterGroupMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupInfoActivity.this,MapActivity.class);
                intent.putExtra("mygroup",mygroup);
                intent.putParcelableArrayListExtra("locs", (ArrayList<? extends Parcelable>) locs);
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

        GroupNameText = (TextView)findViewById(R.id.group_info_text_group_name);
        GroupNameText.setText(mygroup.getGroupname());


    }


    //移动图片到指定经纬度

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
        private LinearLayout PlanLay;

        public GroupInfoPlanHolder(@NonNull View itemView) {
            super(itemView);
            PlanYear = (TextView)itemView.findViewById(R.id.group_info_plan_unit_year);
            PlanMonth = (TextView)itemView.findViewById(R.id.group_info_plan_unit_month);
            PlanDay = (TextView)itemView.findViewById(R.id.group_info_plan_unit_day);
            PlanProvince = (TextView)itemView.findViewById(R.id.group_info_plan_unit_province);
            PlanCity = (TextView)itemView.findViewById(R.id.group_info_plan_unit_city);
            next = (ImageView)itemView.findViewById(R.id.group_info_plan_unit_next);
            PlanLay = (LinearLayout)itemView.findViewById(R.id.plan_layout);
        }
        public void repaint(final PlanRecord myplan, int position){
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
            PlanLay.setClickable(true);
            PlanLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(GroupInfoActivity.this,PlanInfoActivity.class);
                    intent.putExtra("plan_id",myplan.getPlan_id());
                    startActivity(intent);
                }
            });
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