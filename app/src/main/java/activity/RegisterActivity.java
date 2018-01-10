package activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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
 * Created by Administrator on 1/4/2018.
 */

public class RegisterActivity extends AppCompatActivity {


    private EditText textAccount;
    private EditText textPassword;
    private EditText textName;
    private EditText textIdentity;
    private EditText textSchool;
    private EditText textClasses;
    private EditText textProvince;
    private EditText textDistrict;
    private EditText textVillage;

    private Button btnRegister;
    private Button btnBack;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initComponents();

        btnRegister.setOnClickListener(btnRegister_Clicked);
        btnBack.setOnClickListener(btnBack_Clicked);


    }


    private void initComponents() {
        textAccount = findViewById(R.id.itext_account);
        textPassword = findViewById(R.id.itext_password);
        textName = findViewById(R.id.itext_username);
        textIdentity = findViewById(R.id.itext_identity);
        textSchool = findViewById(R.id.itext_school);
        textClasses = findViewById(R.id.itext_classes);
        textProvince = findViewById(R.id.itext_province);
        textDistrict = findViewById(R.id.itext_district);
        textVillage = findViewById(R.id.itext_village);

        btnRegister = findViewById(R.id.btn_register);
        btnBack = findViewById(R.id.btn_back);
    }

    private void doInRegisterSuccess(String s) {
        Values.CURRENT_USER = s.split(",")[1];
        Intent intent = new Intent(RegisterActivity.this, ChoserActivity.class);
        startActivity(intent);
        RegisterActivity.this.finish();
    }

    View.OnClickListener btnRegister_Clicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String a = textAccount.getText().toString();
            String b = textAccount.getText().toString();
            String c = textName.getText().toString();

            if (a.trim().isEmpty() || b.trim().isEmpty() || c.trim().isEmpty()) {
                AlertDialog dialog = createNotifyDialog("Thông báo", "Chưa đầy đủ thông tin");
                dialog.show();
            } else new RegisterProc().execute();        
        }
    };

    View.OnClickListener btnBack_Clicked = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            RegisterActivity.this.finish();
        }
    };


    private AlertDialog createNotifyDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("Trở lại", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Do no thing
            }
        });

        return builder.create();
    }


    private class RegisterProc extends AsyncTask<Void, Void, String> {


        ProgressDialog processDialog = new ProgressDialog(RegisterActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            processDialog.setTitle("");
            processDialog.setMessage("Đang xử lý...");
            processDialog.setCancelable(true);
            processDialog.show();
        }

        @Override
        protected String doInBackground(Void... voids) {

            URLConnection connection = null;
            BufferedReader reader = null;

            try {

                StringBuffer data = new StringBuffer();

                String account = URLEncoder.encode(textAccount.getText().toString(), "UTF-8");
                String password = URLEncoder.encode(textPassword.getText().toString(), "UTF-8");
                String name = URLEncoder.encode(textName.getText().toString(), "UTF-8");
                String identity = URLEncoder.encode(textIdentity.getText().toString(), "UTF-8");
                String school = URLEncoder.encode(textSchool.getText().toString(), "UTF-8");
                String classes = URLEncoder.encode(textClasses.getText().toString(), "UTF-8");
                String province = URLEncoder.encode(textProvince.getText().toString().equals("") ? " ":textProvince.getText().toString(), "UTF-8");
                String district = URLEncoder.encode(textDistrict.getText().toString().equals("") ? " ":textDistrict.getText().toString(), "UTF-8");
                String village = URLEncoder.encode(textVillage.getText().toString().equals("") ? " ":textVillage.getText().toString(), "UTF-8");


                data.append(URLEncoder.encode("account", "UTF-8") + "=" + account);
                data.append("&" + URLEncoder.encode("password", "UTF-8") + "=" + password);
                data.append("&" + URLEncoder.encode("name", "UTF-8") + "=" + name);
                data.append("&" + URLEncoder.encode("identity", "UTF-8") + "=" + identity);
                data.append("&" + URLEncoder.encode("school", "UTF-8") + "=" + school);
                data.append("&" + URLEncoder.encode("classes", "UTF-8") + "=" + classes);
                data.append("&" + URLEncoder.encode("province", "UTF-8") + "=" + province);
                data.append("&" + URLEncoder.encode("district", "UTF-8") + "=" + district);
                data.append("&" + URLEncoder.encode("village", "UTF-8") + "=" + village);

                String http = Values.DB_HOST + "/register";
                URL url = new URL(http);

                connection = url.openConnection();
                connection.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(data.toString());
                writer.flush();

                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                String response = null;
                response = reader.readLine();

                return response;

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (processDialog.isShowing()) {
                processDialog.dismiss();
            }

            if (s.equals("existed")) {
                AlertDialog dialog = createNotifyDialog("Thông báo", "Tài khoản bạn chọn đã tồn tại.");
                dialog.show();
            } else if (s.equals("fail")) {
                AlertDialog dialog = createNotifyDialog("Thông báo", "Đăng ký thất bại, vui lòng kiểm tra lại.");
                dialog.show();
            }  else {
                doInRegisterSuccess(s);
            }

        }
    }
}
