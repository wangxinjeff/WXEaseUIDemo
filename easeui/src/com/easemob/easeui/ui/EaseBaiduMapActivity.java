/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.easemob.easeui.ui;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.easemob.easeui.R;

public class EaseBaiduMapActivity extends EaseBaseActivity implements LocationSource,
AMapLocationListener,OnGeocodeSearchListener {

	private final static String TAG = "map";

	Button sendButton = null;

	EditText indexText = null;
	int index = 0;
	// LocationData locData = null;
	public static EaseBaiduMapActivity instance = null;
	ProgressDialog progressDialog;
	
	private GeocodeSearch geocoderSearch;
	private AMap aMap;
	private MapView mapView=null;
	private OnLocationChangedListener mListener;
	private AMapLocationClient mlocationClient;
	private AMapLocationClientOption mLocationOption;
	private LatLonPoint latLonPoint=null;
	private double longitude;
	private double latitude;
	private String address;
	
	private Marker geoMarker;
	private Marker regeoMarker;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ease_activity_baidumap);
		mapView=(MapView) findViewById(R.id.map);
		mapView.onCreate(savedInstanceState);// 此方法必须重写
		sendButton = (Button) findViewById(R.id.btn_location_send);
		Intent intent = getIntent();
		latitude = intent.getDoubleExtra("latitude", 0);
		if (aMap == null) {
			aMap = mapView.getMap();
		}
		if (latitude == 0) {
			setUpMap();
			showMapWithLocationClient(getResources().getString(R.string.Making_sure_your_location));
		}else {
			longitude = intent.getDoubleExtra("longitude", 0);
			address = intent.getStringExtra("address");
			latLonPoint=new LatLonPoint(latitude,longitude );
			showMap();
		}
	}

	private void showMap() {
		// TODO Auto-generated method stub
		sendButton.setVisibility(View.GONE);
		showMapWithLocationClient(getResources().getString(R.string.Is_to_get_the_address));
		geoMarker = aMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f)
				.icon(BitmapDescriptorFactory
						.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
		regeoMarker = aMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f)
				.icon(BitmapDescriptorFactory
						.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
		geocoderSearch = new GeocodeSearch(this);
		geocoderSearch.setOnGeocodeSearchListener(this);
		RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200,
				GeocodeSearch.AMAP);// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
		geocoderSearch.getFromLocationAsyn(query);// 设置同步逆地理编码请求
	}
	
	/**
	 * 显示进度条对话框
	 */
	public void showMapWithLocationClient(String str) {
		progressDialog = new ProgressDialog(this);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage(str);

		progressDialog.setOnCancelListener(new OnCancelListener() {

			public void onCancel(DialogInterface arg0) {
				if (progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				finish();
			}
		});

		progressDialog.show();
	}
	
	/**
	 * 隐藏进度条对话框
	 */
	public void dismissDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}
	
	/**
	 * 设置一些amap的属性
	 */
	private void setUpMap() {
		aMap.setLocationSource(this);// 设置定位监听
		aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
		// 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
		aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
	}


	/**
	 * 方法必须重写
	 */
	@Override
	protected void onResume() {
		super.onResume();
		mapView.onResume();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onPause() {
		super.onPause();
		mapView.onPause();
		deactivate();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
		if(null != mlocationClient){
			mlocationClient.onDestroy();
		}
	}


	public void back(View v) {
		finish();
	}

	public void sendLocation(View view) {
		Intent intent = this.getIntent();
		intent.putExtra("latitude", latitude);
		intent.putExtra("longitude", longitude);
		intent.putExtra("address", address);
		this.setResult(RESULT_OK, intent);
		finish();
		overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
	}

	/**
	 * 定位成功后回调函数
	 */
	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
		// TODO Auto-generated method stub
		if (mListener != null && amapLocation != null) {
			sendButton.setEnabled(true);
			dismissDialog();
			if (amapLocation != null
					&& amapLocation.getErrorCode() == 0) {
				mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
				longitude=amapLocation.getLongitude();//获取经度
				latitude=amapLocation.getLatitude();//获取维度
				address=amapLocation.getAddress();//获取地址
				Log.e("address", "==============address:"+address);
						Toast.makeText(getApplicationContext(), longitude+","+latitude+","+address, Toast.LENGTH_SHORT).show();
			} else {
				final String errText = "定位失败," + amapLocation.getErrorCode()+ ": " + amapLocation.getErrorInfo();
				Log.e("AmapErr",errText);
						Toast.makeText(getApplicationContext(), errText, Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * 激活定位
	 */
	@Override
	public void activate(OnLocationChangedListener listener) {
		// TODO Auto-generated method stub
		mListener = listener;
		if (mlocationClient == null) {
			mlocationClient = new AMapLocationClient(this);
			mLocationOption = new AMapLocationClientOption();
			//设置定位监听
			mlocationClient.setLocationListener(this);
			//设置为高精度定位模式
			mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
			mLocationOption.setInterval(30000);
			//设置定位参数
			mlocationClient.setLocationOption(mLocationOption);
			// 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
			// 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
			// 在定位结束后，在合适的生命周期调用onDestroy()方法
			// 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
			mlocationClient.startLocation();
		}
	}

	/**
	 * 停止定位
	 */
	@Override
	public void deactivate() {
		// TODO Auto-generated method stub
		mListener = null;
		if (mlocationClient != null) {
			mlocationClient.stopLocation();
			mlocationClient.onDestroy();
		}
		mlocationClient = null;
	}

	/**
	 * 地理编码查询回调
	 */
	@Override
	public void onGeocodeSearched(GeocodeResult arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * 逆地理编码回调
	 */
	@Override
	public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
		dismissDialog();
		if (rCode == 0) {
			if (result != null && result.getRegeocodeAddress() != null
					&& result.getRegeocodeAddress().getFormatAddress() != null) {
				aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
						convertToLatLng(latLonPoint), 15));
				regeoMarker.setPosition(convertToLatLng(latLonPoint));
			} else {
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_result), Toast.LENGTH_SHORT).show();
			}
		} else if (rCode == 27) {
			Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_network), Toast.LENGTH_SHORT).show();
		} else if (rCode == 32) {
			Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_key), Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_other)+rCode, Toast.LENGTH_SHORT).show();
		}
	}
	/**
	 * 把LatLonPoint对象转化为LatLon对象
	 */
	public LatLng convertToLatLng(LatLonPoint latLonPoint) {
		return new LatLng(latLonPoint.getLatitude(), latLonPoint.getLongitude());
	}
}
