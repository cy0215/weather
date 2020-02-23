package com.example.dell.weatherapp.ViewPager;

import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class ViewPagerAdapter {
    private List<View> mListViews;

    public ViewPagerAdapter(List<View> mListViews)
    {
        this.mListViews = mListViews;//构造方法，参数是我们的页卡，这样比较方便。
    }

    //直接继承PagerAdapter

    public void destroyItem(ViewGroup container, int position, Object object)
    {
        container.removeView(mListViews.get(position));//删除页卡
    }


    public Object instantiateItem(ViewGroup container, int position)
    {
        //这个方法用来实例化页卡
        container.addView(mListViews.get(position), 0);//添加页卡
        return mListViews.get(position);
    }


    public int getCount()
    {
        return mListViews.size();//返回页卡的数量
    }


    public boolean isViewFromObject(View arg0, Object arg1)
    {
        return arg0 == arg1;
    }

}
