package com.example.travelapp.Listener;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.example.travelapp.R;

import java.util.Map;

public class MyLocationListener extends BDAbstractLocationListener {
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    boolean firstflag = true;

    public MyLocationListener(MapView mMapView,BaiduMap mBaiduMap){
        this.mMapView = mMapView;
        this.mBaiduMap = mBaiduMap;
    }

    @Override
    public void onReceiveLocation(BDLocation location) {
        //mapView 销毁后不在处理新接收的位置
        if (location == null || mMapView == null){
            return;
        }
        MyLocationData locData = new MyLocationData.Builder()
                .accuracy(location.getRadius())
                // 此处设置开发者获取到的方向信息，顺时针0-360
                .direction(location.getDirection()).latitude(location.getLatitude())
                .longitude(location.getLongitude()).build();
        mBaiduMap.setMyLocationData(locData);
        //移动
        if(firstflag) {
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            MapStatus.Builder builder = new MapStatus.Builder();
            builder.target(ll).zoom(18.0f);
            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            firstflag = false;
        }
        chooseMyLocation(location.getLatitude(),location.getLongitude());
    }

    private void chooseMyLocation(double la,double lo) {
        // 开启定位功能
        mBaiduMap.setMyLocationEnabled(true);
        // 构造定位数据
        MyLocationData locationData = new MyLocationData.Builder()
                .latitude(la)
                .longitude(lo)
                .build();
        // 设置定位数据
        mBaiduMap.setMyLocationData(locationData);
        // 自定以图表
        BitmapDescriptor marker = BitmapDescriptorFactory
                .fromResource(R.drawable.ic_destination);
        // 设置定位图层的配置，设置图标跟随状态（图标一直在地图中心）
        MyLocationConfiguration config = new MyLocationConfiguration(
                MyLocationConfiguration.LocationMode.NORMAL, true, marker);
        mBaiduMap.setMyLocationConfigeration(config);
        // 当不需要定位时，关闭定位图层
        // baiduMap.setMyLocationEnabled(false);

    }

}
