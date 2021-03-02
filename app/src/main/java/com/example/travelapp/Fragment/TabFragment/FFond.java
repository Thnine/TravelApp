package com.example.travelapp.Fragment.TabFragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.SurfaceControl;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.travelapp.Fragment.FondTabFragment.DefalutFondFragment;
import com.example.travelapp.Fragment.FondTabFragment.ResultFondFragment;
import com.example.travelapp.R;
import com.example.travelapp.entity.LocationC;
import com.example.travelapp.testActivity;
import com.zaaach.citypicker.CityPicker;
import com.zaaach.citypicker.adapter.OnPickListener;
import com.zaaach.citypicker.model.City;
import com.zaaach.citypicker.model.HotCity;
import com.zaaach.citypicker.model.LocateState;
import com.zaaach.citypicker.model.LocatedCity;

import java.util.ArrayList;
import java.util.List;

public class FFond extends Fragment{

    String username;

    private Button SearchButton;
    private ImageButton LocButton;
    private TextView ShowLocText;

    String city = "";
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
        View v = inflater.inflate(R.layout.fragment_f_fond, container, false);
        //初始化控件
        SearchButton = (Button)v.findViewById(R.id.f_fond_search_button);
        LocButton = (ImageButton)v.findViewById(R.id.f_fond_choose_loc_button);
        ShowLocText = (TextView)v.findViewById(R.id.f_fond_loc_text);
        //配置监听事件
        LocButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectLoc();
            }
        });
        SearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!city.equals("")) {
                    FragmentTransaction begint = getChildFragmentManager().beginTransaction();
                    begint.replace(R.id.f_fond_result_fragment, new ResultFondFragment(city,username)).commit();
                }
            }
        });
        //配置默认Fragment
        FragmentTransaction begint = getChildFragmentManager().beginTransaction();
        begint.replace(R.id.f_fond_result_fragment,new DefalutFondFragment()).commit();
        return v;
    }

    //选取地点
    public void selectLoc(){
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
                            ShowLocText.setText(data.getProvince() + "省" + data.getName() + "市");
                            city = data.getName();
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
                                        .locateComplete(new LocatedCity("长沙", "湖南", "101280601"), LocateState.SUCCESS);
                            }
                        }, 2000);
                    }
                })
                .show();
    }



}