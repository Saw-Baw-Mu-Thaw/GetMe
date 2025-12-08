package com.android.getme.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.android.getme.Fragments.HistoryTabFragment;
import com.android.getme.Fragments.OngoingTabFragment;

public class TabPageAdapter extends FragmentStateAdapter {

    final int tabCount = 2;
    private int custId;
    private String vehicleType;

    public TabPageAdapter(@NonNull FragmentActivity fragmentActivity, int custId, String vehicleType) {
        super(fragmentActivity);
        this.custId = custId;
        this.vehicleType = vehicleType;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if(position == 0) {

            return OngoingTabFragment.newInstance(custId, vehicleType);
        }else {

            return new HistoryTabFragment();
        }
    }

    @Override
    public int getItemCount() {
        return tabCount;
    }
}
