package mfdevelopement.eggmanager.fragments;


import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class CollectedEggsChartFragment extends FragmentStateAdapter {

    private final String LOG_TAG = "CollectedEggsChartFragm";

    public CollectedEggsChartFragment(Fragment fragment) {
        super(fragment);
        Log.d(LOG_TAG,"created a new instance of CollectedEggsChartFragment");
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Return a NEW fragment instance in createFragment(int)
        Log.d(LOG_TAG,"starting createFragment() for fragment at position " + position);

        Fragment fragment = new Fragment();
        Bundle args = new Bundle();

        switch (position) {
            case 0:
                fragment  = new ChartFragment();

                args.putString(ChartFragment.ARG_DATA, ChartFragment.NAME_EGGS_COLLECTED);
                fragment.setArguments(args);
                break;
            case 1:
                fragment = new ChartFragment();

                args.putString(ChartFragment.ARG_DATA, ChartFragment.NAME_EGGS_SOLD);
                fragment.setArguments(args);
                break;
        }
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
