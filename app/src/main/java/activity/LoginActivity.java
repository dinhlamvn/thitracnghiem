
package activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.administrator.thitracnghiem.MainActivity;
import com.example.administrator.thitracnghiem.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import data.Values;


/**
 * Created by Administrator on 1/2/2018.
 */

public class LoginActivity extends AppCompatActivity {

    private EditText textAccount;
    private EditText textPassword;
    private Button btnLogin;
    private Button btnBack;
    private TextView ctvRegister;




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initComponents();

        getSharedPreferences(Values.PREFS_NAME, MODE_PRIVATE).edit().remove("user").apply();

        btnLogin.setOnClickListener(btnLogin_Clicked);
        btnBack.setOnClickListener(btnBack_Clicked);
        ctvRegister.setOnClickListener(ctvRegister_Clicked);
    }

    private void initComponents() {
        textAccount = findViewById(R.id.itext_account);
        textPassword = findViewById(R.id.itext_password);
        btnLogin = findViewById(R.id.btn_login);
        btnBack = findViewById(R.id.btn_back);
        ctvRegister = findViewById(R.id.ctv_register);
    }


    View.OnClickListener btnLogin_Clicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (textAccount.getText().toString().isEmpty() || textAccount.getText().toString().isEmpty()) {
                AlertDialog dialog = createNotifyDialog("Thông báo", "Bạn cần nhập đầy đủ thông tin");
                dialog.show();
            }
            else {
                new CheckLoginTask().execute();
            }
        }
    };

    View.OnClickListener btnBack_Clicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            LoginActivity.this.finish();
        }
    };

    View.OnClickListener ctvRegister_Clicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        }
    };


    private AlertDialog createNotifyDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setNegativeButton("Trở lại", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        return builder.create();
    }

    private void doAfterLogin(String status) {

        if (status.equals("error")) {
            AlertDialog dialog = createNotifyDialog("Thông báo", "Sai tài khoản hoặc mật khẩu.");
            dialog.show();
        } else if (status.equals("unexisted")) {
            AlertDialog dialog = createNotifyDialog("Thông báo", "Tài khoản không tồn tại.");
            dialog.show();
        } else if (status.equals("error connect")) {
            AlertDialog dialog = createNotifyDialog("Cảnh báo", "Lỗi kết nối.");
            dialog.show();
        } else {
            Values.CURRENT_USER = status.split(",")[1];
            saveUserLogin(Values.CURRENT_USER);
            Intent intent = new Intent(LoginActivity.this, ChoserActivity.class);
            startActivity(intent);
            LoginActivity.this.finish();
        }
    }

    private void saveUserLogin(String user) {
        SharedPreferences preferences = getSharedPreferences(Values.PREFS_NAME, MODE_PRIVATE);
        preferences.edit().putString("user", user).apply();
    }


    private class CheckLoginTask extends AsyncTask<Void, String, String> {

        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(LoginActivity.this);
            progressDialog.setTitle("");
            progressDialog.setMessage("Đang kiểm tra đăng nhập...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... voids) {

            URLConnection connection = null;
            BufferedReader reader = null;
            try {
                StringBuffer data = new StringBuffer();

                String encodeAccount = URLEncoder.encode(textAccount.getText().toString(), "UTF-8");
                String encodePassword = URLEncoder.encode(textPassword.getText().toString(), "UTF-8");

                data.append(URLEncoder.encode("account", "UTF-8") + "=" + encodeAccount);
                data.append("&" + URLEncoder.encode("password", "UTF-8") + "=" + encodePassword);

                // Define url to post data
                String httpAddress = Values.DB_HOST + "/check-login";
                URL url = new URL(httpAddress);

                // Post data to server
                connection = url.openConnection();
                connection.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(data.toString());
                writer.flush();

                // Get response from server
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String response = null;
                response = reader.readLine();

                return response;

            } catch (IOException e) {
                return "error connect";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();

            }
            doAfterLogin(s);
        }
    }
}