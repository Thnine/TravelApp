package com.example.travelapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTabHost;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import com.example.travelapp.Fragment.TabFragment.FFond;
import com.example.travelapp.Fragment.TabFragment.FFriend;
import com.example.travelapp.Fragment.TabFragment.FGroup;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //当前用户名
    String username = "";

    //FragmentTabHost对象
    FragmentTabHost tabhost;
    TabWidget tabs;

    int [] icon_Tabs;//图标ID数组
    String [] text_Tabs;//标题数组

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //获取intent中数据
        username = getIntent().getStringExtra("username");

        //去掉标题栏
        getSupportActionBar().hide();

        //初始化控件
        tabhost = (FragmentTabHost)findViewById(android.R.id.tabhost);
        tabs = (TabWidget)findViewById(android.R.id.tabs);

        //为tab添加fragment
        List<Class> fragmentList = new ArrayList<>();
        fragmentList.add(FFriend.class);
        fragmentList.add(FGroup.class);
        fragmentList.add(FFond.class);
        fragmentList.add(FFond.class);
        tabhost.setup(this,getSupportFragmentManager(),android.R.id.tabcontent);
        icon_Tabs = new int[]{R.drawable.ic_friend,R.drawable.ic_group,R.drawable.ic_search};
        text_Tabs = new String[]{"好友","队伍","推荐"};

        for(int i = 0;i < 3;i++){
            TabHost.TabSpec tabSpec = tabhost.newTabSpec(i+"").setIndicator(getIndicatorView(i));
            Bundle bundle = new Bundle();
            bundle.putString("username",username);
            tabhost.addTab(tabSpec,fragmentList.get(i),bundle);
        }

        tabhost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String s) {
                TabChange(s);
            }
        });
    }

    //Tab选择事件，主要是修改被选中者为绿色
    private void TabChange(String s){
        int tabIndex = tabhost.getCurrentTab();
        for(int i = 0;i < 3;i++){
            View view = tabhost.getTabWidget().getChildAt(i);
            ImageView tab_icon = view.findViewById(R.id.tab_icon);
            TextView tab_text = view.findViewById(R.id.tab_text);
            if(i == tabIndex){
                tab_icon.setColorFilter(Color.BLACK);
                tab_text.setTextColor(Color.BLACK);
            }else{
                tab_icon.setColorFilter(Color.GRAY);
                tab_text.setTextColor(Color.GRAY);
            }
        }
    }


    //获取当前Tab的布局
    private View getIndicatorView(int i){
        View view = LayoutInflater.from(this).inflate(R.layout.main_tab_layout,null,false);
        ImageView tab_icon = view.findViewById(R.id.tab_icon);
        TextView tab_text = view.findViewById(R.id.tab_text);

        tab_icon.setImageResource(icon_Tabs[i]);
        tab_text.setText(text_Tabs[i]);
        if(i==0){
            tab_icon.setColorFilter(Color.BLACK);
            tab_text.setTextColor(Color.BLACK);
        }
        else{
            tab_icon.setColorFilter(Color.GRAY);
            tab_text.setTextColor(Color.GRAY);
        }
        return view;
    }
}