package com.example.travelapp.Fragment.GroupFragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.codbking.widget.DatePickDialog;
import com.codbking.widget.OnSureLisener;
import com.codbking.widget.bean.DateType;
import com.example.travelapp.R;
import com.example.travelapp.entity.LocationC;
import com.example.travelapp.entity.PlanRecord;
import com.example.travelapp.subActivity.LocationAddActivity;
import com.zaaach.citypicker.CityPicker;
import com.zaaach.citypicker.adapter.OnPickListener;
import com.zaaach.citypicker.model.City;
import com.zaaach.citypicker.model.HotCity;
import com.zaaach.citypicker.model.LocateState;
import com.zaaach.citypicker.model.LocatedCity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;


public class GroupRoutePlanFragment extends Fragment {

    //选中的城市数据
    private List<PlanRecord> PlanRecords;

    //列表控件
    RecyclerView PlanRe;
    RoutePlanAdapter PlanAp;

    //添加按钮
    Button PlanCreateButton;

    //临时新增时间的数据
    PlanRecord tempPlan;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化一些数据
        PlanRecords = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_group_route_plan, container, false);
        //配置Re
        PlanRe = v.findViewById(R.id.route_add_list);
        PlanRe.setLayoutManager(new LinearLayoutManager(getActivity()));
        PlanAp = new RoutePlanAdapter(this.PlanRecords);
        PlanRe.setAdapter(PlanAp);
        //配置添加按钮与监听事件
        PlanCreateButton = (Button)v.findViewById(R.id.group_route_plan_create_button);
        PlanCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tempPlan = new PlanRecord();
                selectLoc();
            }
        });

        return v;
    }

    //选取地点
    private void selectLoc(){
        List<HotCity> hotCities = new ArrayList<>();
        hotCities.add(new HotCity("北京", "北京", "101010100"));
        hotCities.add(new HotCity("上海", "上海", "101020100"));
        hotCities.add(new HotCity("广州", "广东", "101280101"));
        hotCities.add(new HotCity("深圳", "广东", "101280601"));
        hotCities.add(new HotCity("杭州", "浙江", "101210101"));

        CityPicker.getInstance()
                .setFragmentManager(getActivity().getSupportFragmentManager())  //此方法必须调用
                .enableAnimation(true)  //启用动画效果
                .setLocatedCity(new LocatedCity("长沙", "湖南", "101210101"))  //APP自身已定位的城市，默认为null（定位失败）
                .setHotCities(hotCities)  //指定热门城市
                .setOnPickListener(new OnPickListener() {
                    @Override
                    public void onPick(int position, City data) {
                        if(data != null) {
                            tempPlan.setPlanLoc(new LocationC(data.getProvince(), data.getName(), data.getCode()));
                            selectDate();
                        }
                    }

                    @Override
                    public void onLocate() {
                        //开始定位，这里模拟一下定位
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //定位完成之后更新数据
                                //TODO
                                CityPicker.getInstance()
                                        .locateComplete(new LocatedCity("深圳", "广东", "101280601"), LocateState.SUCCESS);
                            }
                        }, 2000);
                    }
                })
                .show();
    }

    //选取时间
    private void selectDate(){
        DatePickDialog dialog = new DatePickDialog(getActivity());
        //设置上下年分限制
        dialog.setYearLimt(5);
        //设置标题
        dialog.setTitle("选择日期");
        //设置类型
        dialog.setType(DateType.TYPE_YMD);
        //设置消息体的显示格式，日期格式
        dialog.setMessageFormat("yyyy-MM-dd HH:mm");
        //设置选择回调
        dialog.setOnChangeLisener(null);
        //设置点击确定按钮回调
        dialog.setOnSureLisener(new OnSureLisener() {
            @Override
            public void onSure(Date date) {
                tempPlan.setPlanDate(new Date(date.getTime()));
                PlanRecords.add(new PlanRecord(tempPlan));
                PlanAp.notifyDataSetChanged();
            }
        });
        dialog.show();
    }

    //Holder与Adapter
    private class RoutePlanHolder extends RecyclerView.ViewHolder{
        //控件信息
        TextView RoutePlanYear;
        TextView RoutePlanMonth;
        TextView RoutePlanDay;
        TextView RoutePlanProvince;
        TextView RoutePlanCity;

        public RoutePlanHolder(@NonNull View itemView) {
            super(itemView);
            RoutePlanYear = (TextView)itemView.findViewById(R.id.group_route_plan_year);
            RoutePlanMonth = (TextView)itemView.findViewById(R.id.group_route_plan_month);
            RoutePlanDay = (TextView)itemView.findViewById(R.id.group_route_plan_day);
            RoutePlanProvince = (TextView)itemView.findViewById(R.id.group_route_plan_province);
            RoutePlanCity = (TextView)itemView.findViewById(R.id.group_route_plan_city);
        }

        public void repaint(PlanRecord myplan){
            if(myplan != null && myplan.getPlanLoc() != null && myplan.getPlanDate() != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(myplan.getPlanDate());
                RoutePlanYear.setText(Integer.toString(calendar.get(Calendar.YEAR)));
                RoutePlanMonth.setText(Integer.toString(calendar.get(Calendar.MONTH)+1));
                RoutePlanDay.setText(Integer.toString(calendar.get(Calendar.DAY_OF_MONTH)));
                RoutePlanProvince.setText(myplan.getPlanLoc().getProvince());
                RoutePlanCity.setText(myplan.getPlanLoc().getCity());
            }
        }
    }

    private class RoutePlanAdapter extends RecyclerView.Adapter<RoutePlanHolder>{

        private List<PlanRecord> PlanRecords;

        public RoutePlanAdapter(List<PlanRecord> PlanRecords){
            this.PlanRecords = PlanRecords;
        }

        @NonNull
        @Override
        public RoutePlanHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.group_route_plan_show_unit,parent,false);
            return new RoutePlanHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RoutePlanHolder holder, int position) {
            PlanRecord myplan = PlanRecords.get(position);
            holder.repaint(myplan);

        }

        @Override
        public int getItemCount() {
            return PlanRecords.size();
        }
    }

    public List<PlanRecord> getPlanRecords(){
        return  this.PlanRecords;
    }

}