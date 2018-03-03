package activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.example.administrator.thitracnghiem.R;

import data.Values;

/**
 * Created by Administrator on 1/4/2018.
 */

public class ChoserActivity extends AppCompatActivity {
    
	private Button btnRandom;
	private Button btnExist;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choser);
        initComponents();

        btnRandom.setOnClickListener(btnRandom_Clicked);
        btnExist.setOnClickListener(btnExist_Clicked);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Values.CURRENT_USER.equals("")) {
            Intent intent = new Intent(ChoserActivity.this, LoginActivity.class);
            startActivity(intent);
            ChoserActivity.this.finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_menu_logout:
                _logout();
                break;
            case R.id.item_menu_info:
                _info();
                break;
            default:

        }
        return super.onOptionsItemSelected(item);
    }

    
    private void initComponents() {
    	btnRandom = findViewById(R.id.btn_random);
    	btnExist = findViewById(R.id.btn_exist);
    }

    View.OnClickListener btnRandom_Clicked = new View.OnClickListener() {
    	@Override
    	public void onClick(View view) {
            Intent intent = new Intent(ChoserActivity.this, ContestActivity.class);
            startActivity(intent);
    	}

    };

    View.OnClickListener btnExist_Clicked = new View.OnClickListener() {
    	@Override
    	public void onClick(View view) {
    		// Call contest
    	}
    };


    private void _logout() {
        Values.CURRENT_USER = "";
        getSharedPreferences(Values.PREFS_NAME, MODE_PRIVATE).edit().remove("user").apply();
        Intent intent = new Intent(ChoserActivity.this, LoginActivity.class);
        startActivity(intent);
        ChoserActivity.this.finish();
    }

    private void _info() {
        Intent intent = new Intent(ChoserActivity.this, UserInfoActivity.class);
        startActivity(intent);
    }
}
