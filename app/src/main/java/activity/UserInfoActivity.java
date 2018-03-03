package activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.example.administrator.thitracnghiem.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import data.Values;

/**
 * Created by Administrator on 1/9/2018.
 */

public class UserInfoActivity extends AppCompatActivity {

    private EditText textAccount;
    private EditText textPassword;
    private EditText textName;
    private EditText textIdentity;
    private EditText textSchool;
    private EditText textClasses;
    private EditText textProvince;
    private EditText textDistrict;
    private EditText textVillage;

    private Button btnSave;
    private Button btnEditPassword;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        initComponents();

        if (!Values.CURRENT_USER.equals("")) {
            new LoadUserInfoTask().execute();
        } else {
            Intent intent = new Intent(UserInfoActivity.this, LoginActivity.class);
            startActivity(intent);
            UserInfoActivity.this.finish();
        }


        btnSave.setOnClickListener(btnSave_Clicked);
        btnEditPassword.setOnClickListener(btnEditPassword_Clicked);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Values.CURRENT_USER.equals("")) {
            Intent intent = new Intent(UserInfoActivity.this, LoginActivity.class);
            startActivity(intent);
            UserInfoActivity.this.finish();
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
            case R.id.item_menu_info:
                break;
            case R.id.item_menu_logout:
                _logout();
                break;
        }


        return super.onOptionsItemSelected(item);
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

        btnSave = findViewById(R.id.btn_save);
        btnEditPassword = findViewById(R.id.btn_edit_pass);
    }

    private void _logout() {
        Values.CURRENT_USER = "";
        getSharedPreferences(Values.PREFS_NAME, MODE_PRIVATE).edit().remove("user").apply();
        Intent intent = new Intent(UserInfoActivity.this, LoginActivity.class);
        startActivity(intent);
        UserInfoActivity.this.finish();
    }

    private String[] getAddressDetail(String s) {
        String[] result = {"", "", ""};
        char[] x = s.toCharArray();
        int[] commaPosition = new int[2];
        int index = 0;

        for (int i = 0; i < x.length; i++) {
            if (x[i] == ',') {
                commaPosition[index] = i;
                index++;
            }
        }

        for (int i = 0; i < commaPosition[0]; i++) result[0] += x[i];
        for (int i = commaPosition[0] + 1; i < commaPosition[1]; i++) result[1] += x[i];
        for (int i = commaPosition[1] + 1; i < x.length; i++) result[2] += x[i];
        return result;
    }


    private void showInfo(JSONObject object) {

        try {
            textAccount.setText(object.getString("user_account"));
            textPassword.setText(new String("password"));
            textName.setText(object.getString("user_name"));
            textIdentity.setText(object.getString("user_indentity_card"));
            textSchool.setText(object.getString("user_school"));
            textClasses.setText(object.getString("user_classes"));
            String[] address = getAddressDetail(object.getString("user_address"));
            textProvince.setText((!address[2].equals(" ")) ? address[2]:"");
            textDistrict.setText((!address[1].equals(" ")) ? address[1]:"");
            textVillage.setText((!address[0].equals(" ")) ? address[0]:"");
            textAccount.setEnabled(false);
            textPassword.setEnabled(false);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    private AlertDialog createDialogChangePassword(String message) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(10, 10, 10, 10);

        final LinearLayout layout = new LinearLayout(UserInfoActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText textOldPassword = new EditText(UserInfoActivity.this);
        final EditText textNewPassword = new EditText(UserInfoActivity.this);
        final EditText textCheckNewPassword = new EditText(UserInfoActivity.this);

        textOldPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        textNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        textCheckNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        textOldPassword.setBackground(getResources().getDrawable(R.drawable.rounded_button));
        textNewPassword.setBackground(getResources().getDrawable(R.drawable.rounded_button));
        textCheckNewPassword.setBackground(getResources().getDrawable(R.drawable.rounded_button));

        textOldPassword.setHint("Nhập mật khẩu cũ");
        textNewPassword.setHint("Nhập mật khẩu mới");
        textCheckNewPassword.setHint("Xác nhận mật khẩu mới");

        textOldPassword.setLayoutParams(params);
        textNewPassword.setLayoutParams(params);
        textCheckNewPassword.setLayoutParams(params);

        layout.addView(textOldPassword);
        layout.addView(textNewPassword);
        layout.addView(textCheckNewPassword);

        AlertDialog.Builder builder = new AlertDialog.Builder(UserInfoActivity.this)
                .setTitle("Cập nhật mật khẩu mới")
                .setMessage(message)
                .setView(layout)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String a = textOldPassword.getText().toString();
                        String b = textNewPassword.getText().toString();
                        String c = textCheckNewPassword.getText().toString();

                        if (a.equals("") || b.equals("") || c.equals("")) {
                            AlertDialog dialog = createDialogChangePassword("Chưa nhập đầy đủ thông tin.");
                            dialog.show();
                        }
                        else if (!b.equals(c)) {
                            AlertDialog dialog = createDialogChangePassword("Mật khẩu mới chưa khớp");
                            dialog.show();
                        } else {
                            new ChangePasswordTask().execute(textOldPassword.getText().toString(),textNewPassword.getText().toString());
                        }
                    }
                })
                .setNegativeButton("Cancel", null);
        return builder.create();
    }

    private void doAfterChageInfo(String status) {
        AlertDialog.Builder builder = new AlertDialog.Builder(UserInfoActivity.this)
                .setTitle("Thông báo")
                .setNegativeButton("OK", null);
        if (status.equals("success")) {
            builder.setMessage("Cập nhật thông tin thành công.");
        } else if (status.equals("failed")){
            builder.setMessage("Cập nhật thông tin thất bại.");
        } else {
            builder.setMessage("Không có kết nối đến server.");
        }
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void doAfterChangePassword(String status) {

        AlertDialog.Builder builder = new AlertDialog.Builder(UserInfoActivity.this)
                .setTitle("Thông báo")
                .setNegativeButton("OK", null);

        if (status.equals("success")) {
            builder.setMessage("Cập nhật mật khẩu thành công.");
        } else if (status.equals("incorrect")){
            builder.setMessage("Mật khẩu cũ không chính xác.");
        } else if (status.equals("failed")) {
            builder.setMessage("Cập nhật mật khẩu thất bại.");
        } else {
            builder.setMessage("Không thể kết nối đến server.");
        }

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void showMessageDialog(String title, String message, String chose) {
        AlertDialog dialog = new AlertDialog.Builder(UserInfoActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(true)
                .setNegativeButton(chose, null)
                .create();
        dialog.show();
    }

    private void createChangePasswordDialog() {
        final Dialog dialog = new Dialog(UserInfoActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_change_password);


        dialog.show();

        final EditText edtOldPassword = dialog.findViewById(R.id.edt_oldpass);
        final EditText edtNewPassword = dialog.findViewById(R.id.edt_newpass);
        final EditText edtRenewPassword = dialog.findViewById(R.id.edt_renewpass);
        final Button btnSave = dialog.findViewById(R.id.btn_save);
        final Button btnExit = dialog.findViewById(R.id.btn_exit);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String a = edtOldPassword.getText().toString();
                String b = edtNewPassword.getText().toString();
                String c = edtRenewPassword.getText().toString();

                if (a.isEmpty() || b.isEmpty() || c.isEmpty()) {
                    showMessageDialog("Thông báo", "Bạn cần nhập đủ thông tin", "OK");
                } else if (!b.equals(c)) {
                    showMessageDialog("Thông báo", "Mật khẩu mới chưa khớp", "OK");
                } else {
                    new ChangePasswordTask().execute(a, b);
                }

            }
        });

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }


    View.OnClickListener btnSave_Clicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (textName.getText().toString().trim().equals("")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(UserInfoActivity.this)
                    .setTitle("Thông báo")
                    .setMessage("Tên còn trống.")
                    .setNegativeButton("OK", null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
            new ChangeInfoTask().execute();
        }
    };

    View.OnClickListener btnEditPassword_Clicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // Call change password
            createChangePasswordDialog();
        }
    };



    private class LoadUserInfoTask extends AsyncTask<Void, Void, JSONObject> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(UserInfoActivity.this);
            progressDialog.setTitle("");
            progressDialog.setMessage("Đang lấy dữ liệu người dùng...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected JSONObject doInBackground(Void... voids) {

            URLConnection connection = null;
            BufferedReader reader = null;

            try {

                String id = URLEncoder.encode("user_id","UTF-8") + "=" + URLEncoder.encode(Values.CURRENT_USER, "UTF-8");

                String http = Values.DB_HOST + "/get-user-info";
                URL url = new URL(http);
                connection = url.openConnection();
                connection.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(id);
                writer.flush();

                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String response = null;

                while ((response = reader.readLine()) != null) {
                    sb.append(response);
                }

                return new JSONObject(sb.toString());

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);

            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }



            showInfo(jsonObject);
        }
    }


    private class ChangeInfoTask extends  AsyncTask<Void, Void, String> {
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(UserInfoActivity.this);
            progressDialog.setTitle("");
            progressDialog.setMessage("Đang thực hiện...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            URLConnection connection = null;
            BufferedReader reader = null;

            try {

                StringBuilder data = new StringBuilder();

                data.append(URLEncoder.encode("user_id","UTF-8") + "=" + URLEncoder.encode(Values.CURRENT_USER, "UTF-8"));
                data.append("&" + URLEncoder.encode("user_name","UTF-8") + "=" + URLEncoder.encode(textName.getText().toString(), "UTF-8"));
                data.append("&" + URLEncoder.encode("user_identity", "UTF-8") + "=" + URLEncoder.encode(textIdentity.getText().toString(),"UTF-8"));
                data.append("&" + URLEncoder.encode("user_school","UTF-8") + "=" + URLEncoder.encode(textSchool.getText().toString(), "UTF-8"));
                data.append("&" + URLEncoder.encode("user_classes","UTF-8") + "=" + URLEncoder.encode(textClasses.getText().toString(), "UTF-8"));
                data.append("&" + URLEncoder.encode("user_province","UTF-8") + "=" + URLEncoder.encode(textProvince.getText().toString().equals("") ? " ":textProvince.getText().toString(), "UTF-8"));
                data.append("&" + URLEncoder.encode("user_district","UTF-8") + "=" + URLEncoder.encode(textDistrict.getText().toString().equals("") ? " ":textDistrict.getText().toString(), "UTF-8"));
                data.append("&" + URLEncoder.encode("user_village","UTF-8") + "=" + URLEncoder.encode(textVillage.getText().toString().equals("") ? " ":textVillage.getText().toString(), "UTF-8"));

                String http = Values.DB_HOST + "/update-info";
                URL url = new URL(http);
                connection = url.openConnection();
                connection.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(data.toString());
                writer.flush();

                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String status = null;
                status = reader.readLine();

                return status;

            } catch (IOException e) {
                return "not connect";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            doAfterChageInfo(s);
        }
    }

    private class ChangePasswordTask extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(UserInfoActivity.this);
            progressDialog.setTitle("");
            progressDialog.setMessage("Đang thực hiện...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            URLConnection connection = null;
            BufferedReader reader = null;

            try {

                StringBuilder data = new StringBuilder();

                data.append(URLEncoder.encode("user_id","UTF-8") + "=" + URLEncoder.encode(Values.CURRENT_USER, "UTF-8"));
                data.append("&" + URLEncoder.encode("old_pass", "UTF-8") + "=" + URLEncoder.encode(strings[0], "UTF-8"));
                data.append("&" + URLEncoder.encode("new_pass","UTF-8") + "=" + URLEncoder.encode(strings[1], "UTF-8"));

                String http = Values.DB_HOST + "/update-password";
                URL url = new URL(http);
                connection = url.openConnection();
                connection.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(data.toString());
                writer.flush();

                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String status = null;
                status = reader.readLine();

                return status;

            } catch (IOException e) {
                return "not connect";
            }

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            doAfterChangePassword(s);
        }
    }
}
