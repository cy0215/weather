package com.example.dell.weatherapp.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;
//一个总的实体类来引用刚刚创建的各个实体类
public class Weather {
    public String status;
    public Basic basic;
    public AQI aqi;
    public Now now;
    public Suggestion suggestion;
    //由于daily_forecast中包含的是一个数组，
    //这里使用List集合来引用Forecast类
    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;
}
