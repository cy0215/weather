package com.example.dell.weatherapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.dell.weatherapp.db.City;
import com.example.dell.weatherapp.db.County;
import com.example.dell.weatherapp.db.Province;
import com.example.dell.weatherapp.util.HttpUtil;
import com.example.dell.weatherapp.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();

    /*
    省列表
    */
    private List<Province> provinceList;

    /*
    * 市列表
    * */
    private List<City> cityList;

    /*
    * 县列表
    */
    private List<County> countyList;

    /*选中的省份*

     */
    private Province selectedProvince;

    /*选中的城市*/
    private  City selectedCity;

    /*当前选中的级别*/
    private int currentLevel;


    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area,container,false);
        titleText = (TextView) view.findViewById(R.id.title_text);//获取title_text
        backButton = (Button) view.findViewById(R.id.back_button);
        listView = (ListView) view.findViewById(R.id.list_view);
        adapter  = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);//初始化数组适配器
        listView.setAdapter(adapter);//将adapter设置为ListView的适配器

        return view;
    }

    @Override
    //ListView和Button设置点击事件
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel ==LEVEL_PROVINCE){
                    selectedProvince = provinceList.get(position);
                    queryCities();
                }
                else if(currentLevel==LEVEL_CITY){
                    selectedCity = cityList.get(position);
                    queryCounties();

                }else if(currentLevel == LEVEL_COUNTY){//条件成立，启动WeatherActivity,并把当前选中县的天气id传递过去
                    String weatherId = countyList.get(position).getWeatherId();
                    if(getActivity() instanceof MainActivity) {
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id", weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    }else if(getActivity() instanceof WeatherActivity){
                        WeatherActivity activity = (WeatherActivity)getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.swipeRefresh.setRefreshing(true);
                        activity.requestWeather(weatherId);
                    }

                }

            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentLevel==LEVEL_COUNTY){
                    queryCities();
                }else if(currentLevel == LEVEL_CITY)
                    queryProvinces();
            }

        });
       queryProvinces();
    }

    /*查询全国所有的省，优先从数据库查，如果没有查询到再去服务器上查询*/
    private void queryProvinces(){
        titleText.setText("中国");//布局标题设置成中国
        backButton.setVisibility(View.GONE);//不可见，且不占用空间
        provinceList = DataSupport.findAll(Province.class);
        if(provinceList.size()>0){
            dataList.clear();
            for(Province province:provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }else{
            String address = "http://guolin.tech/api/china";
            queryFromServer(address,"province");//从服务器上查询数据
        }
    }

    /*查询选中省内所有的市，优先从数据库查询，如果没有查询到再去服务器上查询*/
    private void queryCities() {
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where(
                "provinceid = ?",
                String.valueOf(selectedProvince.getId())).find(City.class);
        if(cityList.size()>0){
            dataList.clear();
            for(City city:cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }else{
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/"+provinceCode;
            queryFromServer(address,"city");
        }
    }

    /*查询选中市内所有的县，优先从数据库查询，如果没有查询到再去服务器上查询*/
    private void queryCounties() {
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid = ?",
                String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size()>0){
            dataList.clear();
            for(County county:countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromServer(address,"county");
        }
    }


    /*根据传入的地址和类型从服务器上查询省市县数据*/
    private void queryFromServer(String address,final String type) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {//向服务器发送请求
            @Override
            public void onResponse(Call call, Response response)
                    throws IOException {//响应的数据回调到onResponse()方法中
            String responseText = response.body().string();//返回JSON数据
                boolean result = false;
                if ("province".equals(type)){
                    result = Utility.handleProvinceResponse(responseText);//解析和处理服务器返回的数据，并存储到数据库中
                }else if("city".equals(type)){
                    result = Utility.handleCityResponse(responseText,selectedProvince.getId());
                }
                else if("county".equals(type)){
                    result = Utility.handleCountyResponse(responseText,selectedCity.getId());
                }
                if (result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)){
                                queryProvinces();//此方法必须在主线程中调用，借助runOnUiThread（）实现从子线程切换到主线程
                            }else if("city".equals(type)){
                                queryCities();
                            }else if("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }

            }
            @Override
            public void onFailure(Call call, IOException e) {
                //通过runOnUiThread()方法回到主线程处理逻辑
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /*显示进度对话框*/
    private void showProgressDialog(){
        if(progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

/*关闭进度对话框*/
    private void closeProgressDialog(){
        if (progressDialog !=null){
            progressDialog.dismiss();
        }
    }


    }


