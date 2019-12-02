package mfdevelopement.eggmanager.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import mfdevelopement.eggmanager.BuildConfig;
import mfdevelopement.eggmanager.R;

public class AboutActivity extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_about, container, false);

        String versionName = "Version " + BuildConfig.VERSION_NAME;

        TextView txt_app_version = root.findViewById(R.id.txtv_about_appversion);
        txt_app_version.setText(versionName);

        return root;
    }

/*    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Toolbar toolbar = findViewById(R.id.toolbar_about_activity);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String versionName = "Version " + BuildConfig.VERSION_NAME;

        TextView txt_app_version = findViewById(R.id.txtv_about_appversion);
        txt_app_version.setText(versionName);
    }*/

}
