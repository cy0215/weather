# weather
                                                            
     这是一款简单易操作的天气预报软件，具备查看全国的省市县、查询任意城市的天气，自由切换城市，手动更新天气，后台自动更新天气等功能。
 
                                                            功能概述

1、用户进入App后要选择所要查询的省，点击所选省份后弹出该省的市级地区，接着用户点击所选市，弹出该市的县级地区，再次选择后出现该县的天气情况。HTTP请求调用了sendOKHTTPRequest（）方法，handleProvinceResponse（）、handleCityResponse（）、handleCountyResponse（）用于解析和处理服务器返回的省级、市级和县级数据，使用JSONArray和JSONObject将数据解析出来。

2、在省级和县级地区展示页面可以通过返回按钮返回上层页面。

3、在预报天气情况的界面用户能够看到当天的天气情况，空气质量以及一些生活小建议，背景图片与必应同步更新。用GSON解析和风天气返回的天气情况、空气质量和生活建议。

4、用户在天气界面可通过下拉页面手动更新天气情况，此处的加载效果用到了ProgressBar。

5、用户在此页面可以向右拖动页面，出现侧滑栏，侧滑栏出现省级信息，选择省后，依次展示市级、县级信息，返回按钮依旧有效，用户可以重新选择地区。

                                                         截图

![](https://github.com/cy0215/weather/blob/master/WeatherApp%E5%9B%BE%E7%89%871.png)  ![](https://github.com/cy0215/weather/blob/master/WeatherApp%E5%9B%BE%E7%89%872.png)                                        
                     
