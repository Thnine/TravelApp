package com.example.travelapp.Fragment.FondTabFragment;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTabHost;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import com.example.travelapp.Fragment.TabFragment.FFond;
import com.example.travelapp.Fragment.TabFragment.FFriend;
import com.example.travelapp.Fragment.TabFragment.FGroup;
import com.example.travelapp.R;

import java.util.ArrayList;
import java.util.List;


public class ResultFondFragment extends Fragment {

    String city;
    String username;

    //标题数组
    String [] text_Tabs;

    //FragmentTabHost对象
    FragmentTabHost tabhost;
    TabWidget tabs;

    public ResultFondFragment(String city,String username){
        this.city = city;
        this.username = username;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_result_fond, container, false);

        //初始化控件
        tabhost = (FragmentTabHost)v.findViewById(android.R.id.tabhost);
        tabs = (TabWidget)v.findViewById(android.R.id.tabs);

        //为tab添加fragment
        List<Class> fragmentList = new ArrayList<>();
        fragmentList.add(SiteFondTabFragment.class);
        fragmentList.add(StrategyFondTabFragment.class);
        tabhost.setup(getContext(),getChildFragmentManager(),android.R.id.tabcontent);
        text_Tabs = new String[]{"景点推荐","热门攻略"};

        for(int i = 0;i < 2;i++){
            TabHost.TabSpec tabSpec = tabhost.newTabSpec(i+"").setIndicator(getIndicatorView(i));
            Bundle bundle = new Bundle();
            bundle.putString("city",city);
            bundle.putString("username",username);
            tabhost.addTab(tabSpec,fragmentList.get(i),bundle);
        }

        tabhost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String s) {
                TabChange(s);
            }
        });

        return v;
    }


    //Tab选择事件，主要是修改被选中者为绿色
    private void TabChange(String s){
        int tabIndex = tabhost.getCurrentTab();
        for(int i = 0;i < 2;i++){
            View view = tabhost.getTabWidget().getChildAt(i);
            TextView tab_text = view.findViewById(R.id.fond_tab_text);
            if(i == tabIndex){
                tab_text.setTextColor(Color.YELLOW);
            }else{
                tab_text.setTextColor(Color.WHITE);
            }
        }
    }


    //获取当前Tab的布局
    private View getIndicatorView(int i){
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fond_tab_layout,null,false);
        TextView tab_text = view.findViewById(R.id.fond_tab_text);
        tab_text.setText(text_Tabs[i]);
        if(i==0){
            tab_text.setTextColor(Color.YELLOW);
        }
        else{
            tab_text.setTextColor(Color.WHITE);
        }
        return view;
    }
}