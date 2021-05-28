package mfdevelopement.eggmanager.fragments;


import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ChartFragmentStateAdapter extends FragmentStateAdapter {

    private final String LOG_TAG = "CollectedEggsChartFragm";

    public ChartFragmentStateAdapter(Fragment fragment) {
        super(fragment);
        Log.d(LOG_TAG, "created a new instance of CollectedEggsChartFragment");
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Return a NEW fragment instance in createFragment(int)
        Log.d(LOG_TAG, "starting createFragment() for fragment at position " + position);

        Fragment fragment = new Fragment();

        switch (position) {
            case 0:
                fragment = new CollectedEggsChartFragment();
                break;
            case 1:
                fragment = new SoldEggsChartFragment();
                break;
        }
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
