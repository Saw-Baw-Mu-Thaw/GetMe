package com.android.getme.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.getme.Adapters.TabPageAdapter;
import com.android.getme.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;


public class ActivityFragment extends Fragment {

    public ActivityFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_activity, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        configureTabLayout(view);

        super.onViewCreated(view, savedInstanceState);
    }

    private void configureTabLayout(View view) {
        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab()); // tab for ongoing
        tabLayout.addTab(tabLayout.newTab()); // tab for history

        final TabPageAdapter adapter = new TabPageAdapter(getActivity());
        ViewPager2 viewPager2 = view.findViewById(R.id.view_pager);
        viewPager2.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager2,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int i) {
                        if(i == 0) {
                            tab.setText("Ongoing");
                        }else{
                            tab.setText("History");
                        }
                    }
                }).attach();
    }
}