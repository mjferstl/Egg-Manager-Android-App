package mfdevelopement.eggmanager.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import mfdevelopement.eggmanager.BuildConfig;
import mfdevelopement.eggmanager.R;

public class AboutFragment extends Fragment {

    private final String LOG_TAG = "AboutFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.d(LOG_TAG, "starting onCreateView()");

        View root = inflater.inflate(R.layout.fragment_about, container, false);

        String versionName = "Version " + BuildConfig.VERSION_NAME;

        TextView txt_app_version = root.findViewById(R.id.txtv_about_appversion);
        txt_app_version.setText(versionName);

        return root;
    }
}
