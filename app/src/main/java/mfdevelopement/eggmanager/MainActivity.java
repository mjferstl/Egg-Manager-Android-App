package mfdevelopement.eggmanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DailyBalanceViewModel dailyBalanceViewModel;
    public static final int NEW_ENTITY_ACTIVITY_REQUEST_CODE = 1;
    public static final int EDIT_ENTITY_ACTIVITY_REQUEST_CODE = 2;
    public static final String EXTRA_REQUEST_CODE_NAME = "requestCode";
    public static final String EXTRA_PRIMATRY_KEY_NAME = "dateKeyPrimary";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NewEntityActivity.class);
                intent.putExtra(EXTRA_REQUEST_CODE_NAME,NEW_ENTITY_ACTIVITY_REQUEST_CODE);
                startActivity(intent);
            }
        });

        dailyBalanceViewModel = new ViewModelProvider(this).get(DailyBalanceViewModel.class);

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        final DailyBalanceListAdapter adapter = new DailyBalanceListAdapter(this, dailyBalanceViewModel);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dailyBalanceViewModel.getAllWords().observe(this, new Observer<List<DailyBalance>>() {
            @Override
            public void onChanged(@Nullable final List<DailyBalance> dates) {
                // Update the cached copy of the words in the adapter.
                adapter.setWords(dates);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
