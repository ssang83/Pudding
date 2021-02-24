package com.enliple.pudding.adapter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.enliple.pudding.bus.VideoPipBus;
import com.enliple.pudding.commons.data.DialogModel;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.fragment.DialogCartFragment;
import com.enliple.pudding.fragment.DialogProductFragment;
import com.enliple.pudding.model.ProductDialogTab;

import java.util.ArrayList;
import java.util.List;

public class ProductTabPagerAdapter extends FragmentStatePagerAdapter {

    private ViewPager viewPager;
    private FragmentManager fm;
    private List<Fragment> fragments = new ArrayList(ProductDialogTab.values().length);
    private DialogProductFragment pFragment;
    private DialogCartFragment cFragment;
    private ArrayList<DialogModel> items;
    private VideoPipBus bus;
    private boolean isCast;
    public ProductTabPagerAdapter(ViewPager viewPager, @NonNull FragmentManager fm, ArrayList<DialogModel> items, VideoPipBus bus, boolean isCast) {
        super(fm);
        this.fm = fm;
        this.viewPager = viewPager;
        this.items = items;
        this.bus = bus;
        this.isCast = isCast;

        ProductDialogTab[] myTab = ProductDialogTab.values();
        for ( int i = 0 ; i < myTab.length ; i ++ ) {
            Logger.e("mytab ordinal :: " + myTab[i].ordinal());
            if ( myTab[i].ordinal() == ProductDialogTab.PRODUCT_LIST.ordinal() ) {
                Logger.e("product list");
                pFragment = new DialogProductFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("list", items);
                bundle.putSerializable("bus", bus);
                bundle.putBoolean("isCast", isCast);
                Logger.e("items size :: " + items.size());
                pFragment.setArguments(bundle);
                fragments.add(pFragment);
            } else if ( myTab[i].ordinal() == ProductDialogTab.CART_LIST.ordinal() ) {
                Logger.e("cart list");
                cFragment = new DialogCartFragment();
                fragments.add(cFragment);
            } else {
                Logger.e("else");
            }
        }

        viewPager.setAdapter(this);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

//    public void closeSubPop(int position) {
//        Logger.e("closeSubPop position :: " + position);
//        if ( position == 0 ) {
//            if ( pFragment != null ) {
//                pFragment.closeSubPop();
//            }
//        } else {
//
//        }
//    }
}
