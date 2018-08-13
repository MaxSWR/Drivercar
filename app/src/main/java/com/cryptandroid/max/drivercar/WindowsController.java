package com.cryptandroid.max.drivercar;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.View;

public class WindowsController extends FragmentActivity {
    private ViewPager pager;
    private PagerAdapter windowAdapter;
    private final int PAGE_COUNT = 2;
    private Fragment[] fragmentsList = new Fragment[PAGE_COUNT];
    private final String[] titles = {"Controls","Bluetooth"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        pager = (ViewPager) findViewById(R.id.pager);
        windowAdapter = new WindowAdapter(getSupportFragmentManager());
        pager.setAdapter(windowAdapter);
        TabLayout tabLayout = findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(pager);
    }

    /**
     * @class Адаптер перелистывания окон приложения
     */
    private class WindowAdapter extends FragmentPagerAdapter {

        public WindowAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    fragmentsList[position] = new ControllerWindow();
                    break;

                case 1:
                    fragmentsList[position] = new BluetoothWindow();
                    break;
                default:
                    position = 0;
                    fragmentsList[position] = new ControllerWindow();
            }

            return fragmentsList[position];
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }
    }
}
