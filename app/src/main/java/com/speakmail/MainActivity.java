package com.speakmail;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.speakmail.database.model.Draft;

public class MainActivity extends AppCompatActivity implements DraftsFragment.SendDraft{

    private Toolbar toolbar;
    private static final int PAGES = 2;
    private ViewPager mainActivityViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mainActivityViewPager = findViewById(R.id.main_activity_pager);
        mainActivityViewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));

        mainActivityViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        toolbar.setTitle("Compose Email");
                        break;
                    case 1:
                        toolbar.setTitle("Drafts");
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void mainLayoutClicked(View view) {
        Log.d("MainActivity", "layoutClicked: " + mainActivityViewPager.getCurrentItem());
        ComposeMailFragment composeMailFragment = (ComposeMailFragment) mainActivityViewPager.getAdapter().instantiateItem(mainActivityViewPager, mainActivityViewPager.getCurrentItem());
        composeMailFragment.layoutClicked();
    }

    public void rvLayoutClicked(View view) {
        Log.d("MainActivity", "draftsLayoutClicked: " + mainActivityViewPager.getCurrentItem());
        DraftsFragment draftsFragment = (DraftsFragment) mainActivityViewPager.getAdapter().instantiateItem(mainActivityViewPager, mainActivityViewPager.getCurrentItem());
        draftsFragment.layoutClicked();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }

    @Override
    public void sendData(Draft draft) {
        String tag = "android:switcher:" + R.id.main_activity_pager + ":" + 0;
        ComposeMailFragment composeMailFragment = (ComposeMailFragment) getSupportFragmentManager().findFragmentByTag(tag);
        composeMailFragment.displayReceivedData(draft);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 1:
                    return new DraftsFragment();
                case 0:
                default:
                    toolbar.setTitle("Compose Email");
                    return new ComposeMailFragment();
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title = null;
            if (position == 0) {
                title = "Compose";
            } else if (position == 1) {
                title = "Draft";
            }
            return title;
        }
        @Override
        public int getCount() {
            return MainActivity.PAGES;
        }
    }

    public void setViewPagerItem(int pos) {
        mainActivityViewPager.setCurrentItem(pos);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment fragment = (Fragment) getSupportFragmentManager().getFragments().get(mainActivityViewPager.getCurrentItem());
        fragment.onActivityResult(requestCode, resultCode, data);
    }
}
