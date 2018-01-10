package com.example.administrator.thitracnghiem;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import activity.ChoserActivity;
import activity.LoginActivity;
import data.Values;

public class MainActivity extends AppCompatActivity {

	private Button btnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initComponents();

        btnStart.setOnClickListener(new View.OnClickListener() {
        	
        	@Override
        	public void onClick(View view) {
        	    String currentUser = getSharedPreferences(Values.PREFS_NAME, MODE_PRIVATE).getString("user", "not exist");

        	    if (currentUser.equals("not exist")) {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    MainActivity.this.finish();
                }
        	    else {
                    Values.CURRENT_USER = currentUser;
                    Intent intent = new Intent(MainActivity.this, ChoserActivity.class);
                    startActivity(intent);
                    MainActivity.this.finish();
                }
        	}

        	
        });

    }

    private void initComponents() {
        btnStart = findViewById(R.id.btn_start);
    }
}
