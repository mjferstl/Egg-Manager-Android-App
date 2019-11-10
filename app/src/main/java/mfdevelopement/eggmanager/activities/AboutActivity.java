package mfdevelopement.eggmanager.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import mfdevelopement.eggmanager.BuildConfig;
import mfdevelopement.eggmanager.R;

public class AboutActivity extends AppCompatActivity {

    @Override
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
    }

}
