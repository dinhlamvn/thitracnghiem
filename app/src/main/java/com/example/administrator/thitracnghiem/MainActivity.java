package com.example.administrator.thitracnghiem;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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

        btnStart.setOnClickListener(btnStart_Clicked);
    }

    private void initComponents() {
        btnStart = findViewById(R.id.btn_start);
    }

    View.OnClickListener btnStart_Clicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!isNetworkConnected()) createNetworkDialog();
            else _go();
        }
    };

    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();
    }

    private void createNetworkDialog() {
        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Cảnh báo")
                            .setMessage("Bạn đang offline, hãy kết nối vào internet để sử dụng ứng dụng.")
                            .setCancelable(false)
                            .setPositiveButton("Kết nối wifi", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                                    if (!wifiManager.isWifiEnabled()) {
                                        Toast.makeText(getApplicationContext(), "Tự động mở wifi....", Toast.LENGTH_SHORT).show();
                                        wifiManager.setWifiEnabled(true);
                                    }
                                }
                            })
                            .setNegativeButton("Vẫn tiếp tục", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    _go();
                                }
                            })
                            .create();
        dialog.show();
    }

    private void _go() {
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
}
