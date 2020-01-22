package mfdevelopement.eggmanager.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import mfdevelopement.eggmanager.R;

public class DiagramFragment extends Fragment {

    private final String[] tabNames = {"abgenommen", "verkauft"};
    private final String LOG_TAG = "DiagramFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(LOG_TAG,"running onCreateView()");
        return inflater.inflate(R.layout.fragment_diagrams, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        Log.d(LOG_TAG,"starting onViewCreated()");
        CollectedEggsChartFragment collectedEggsChartFragment = new CollectedEggsChartFragment(this);
        ViewPager2 viewPager = view.findViewById(R.id.charts_viewpager);
        viewPager.setAdapter(collectedEggsChartFragment);

        TabLayout tabLayout = view.findViewById(R.id.charts_tabs);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(tabNames[position])).attach();

        Log.d(LOG_TAG,"finished onViewCreated()");
    }

}
