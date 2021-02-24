package com.enliple.pudding.adapter.shoptree;

import android.content.Context;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.enliple.pudding.fragment.shoptree.PageFragment;

import java.util.ArrayList;

public class PhotoReviewAdapter extends FragmentPagerAdapter {
    private Context context;
    private ArrayList<String> items = new ArrayList<String>();
    private int rootWidth, rootHeight, shortRootHeight;
    private boolean toggleOpen = false;
    private PageFragment fragment;

    public PhotoReviewAdapter(FragmentManager fm, Context context, ArrayList<String> item, int rootWidth, int rootHeight, int shortRootHeight, boolean isOpen) {
        super(fm);
        this.context = context;
        items.addAll(item);
        this.rootWidth = rootWidth;
        this.rootHeight = rootHeight;
        this.shortRootHeight = shortRootHeight;
        this.toggleOpen = isOpen;
    }

    public void setItem(ArrayList<String> item) {
        if (items != null && items.size() > 0) {
            items.clear();
            items = new ArrayList<String>();
        }
        items.addAll(item);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public Fragment getItem(int position) {
        // 해당하는 page의 Fragment를 생성합니다.

//        PageFragment fragment = PageFragment(items.get(position), rootWidth, rootHeight, shortRootHeight);

        return PageFragment.create(items.get(position), rootWidth, rootHeight, shortRootHeight, toggleOpen);
    }

    @Override
    public int getCount() {
        if (items != null && items.size() > 0) {
            return items.size();
        } else {
            return 0;
        }
    }

    public void setToggle(boolean isOpen) {
        this.toggleOpen = isOpen;
//        notifyDataSetChanged();
    }
}