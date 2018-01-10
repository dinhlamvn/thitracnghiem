package activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.example.administrator.thitracnghiem.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import data.Result;
import data.Values;

/**
 * Created by Administrator on 1/6/2018.
 */

public class DashboardActivity extends AppCompatActivity {


    private GridView grvDashboard;
    private Button btnReContest;
    private TextView tvScore;
    private TextView tvTotalTime;
    private List<Result> results;

    private static String examsGroupId;

    private int score = 0;
    private int totalTime = 0;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        initComponents();

        if (!Values.CURRENT_USER.equals("")) {
            new LoadResultTask().execute();
        }
        else {
            Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
            startActivity(intent);
            DashboardActivity.this.finish();
        }

        grvDashboard.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                QuestionInfoAcitivity.setQuestionId(results.get(i).getQuestionId());
                Intent intent = new Intent(DashboardActivity.this, QuestionInfoAcitivity.class);
                startActivity(intent);
            }
        });

        btnReContest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DashboardActivity.this, ChoserActivity.class);
                startActivity(intent);
                DashboardActivity.this.finish();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Values.CURRENT_USER.equals("")) {
            Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
            startActivity(intent);
            DashboardActivity.this.finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.item_menu_info:
                _info();
                break;
            case R.id.item_menu_logout:
                _logout();
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    private void _info() {
        Intent intent = new Intent(DashboardActivity.this, UserInfoActivity.class);
        startActivity(intent);
    }

    private void _logout() {
        Values.CURRENT_USER = "";
        getSharedPreferences(Values.PREFS_NAME, MODE_PRIVATE).edit().remove("user").apply();
        Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
        startActivity(intent);
        DashboardActivity.this.finish();
    }

    private void initComponents() {
        grvDashboard = findViewById(R.id.grid_result);
        btnReContest = findViewById(R.id.btn_restart);
        tvScore = findViewById(R.id.tv_score);
        tvTotalTime = findViewById(R.id.tv_total_time);
    }

    public static void setExamsGroupId(String examsGroupId) {
        DashboardActivity.examsGroupId = examsGroupId;
    }

    private void createResultList(JSONArray jsonArray) {
        int size = jsonArray.length();
        results = new ArrayList<>();
        try {
            for(int i = 0; i < size; i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                Result result = new Result();
                result.setQuestionId(object.getInt("question_id"));
                result.setStatus((object.getInt("result") == 1 ) ? "Đúng":"Sai");
                result.setTimeAnswer(object.getInt("time"));
                score = score + object.getInt("result");
                totalTime = totalTime + object.getInt("time");
                results.add(result);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void doAfterLoadResult() {
        tvScore.setText(String.valueOf(score) + "/" + Values.NUM_OF_QUESTION);
        tvTotalTime.setText(String.valueOf(totalTime) + "s");
        fillData();
    }


    private void fillData() {
        ArrayList<Result> rs = new ArrayList<Result>();

        for (Result rss : results) {
            rs.add(rss);
        }

        ResultAdapter resultAdapter = new ResultAdapter(this, rs);

        grvDashboard.setAdapter(resultAdapter);
    }


    private class ResultAdapter extends ArrayAdapter<Result> {

        public ResultAdapter(Context context, ArrayList<Result> results) {
            super(context, 0, results);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            Result result = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.items_dashboard, parent, false);
            }

            TextView tvTitle = convertView.findViewById(R.id.tv_title);
            TextView tvStatus = convertView.findViewById(R.id.tv_status);
            TextView tvTime = convertView.findViewById(R.id.tv_time);

            tvTitle.setText(String.valueOf(position + 1));
            tvTime.setText(String.valueOf(result.getTimeAnswer()) + "s");

            if (result.getStatus() == "Sai") {
                tvStatus.setCompoundDrawablesWithIntrinsicBounds(null, null, getDrawable(R.drawable.ic_incorrect_v16), null);
            } else {
                tvStatus.setCompoundDrawablesWithIntrinsicBounds(null, null, getDrawable(R.drawable.ic_correct_v16), null);
            }

            return convertView;
        }
    }


    private class LoadResultTask extends AsyncTask<Void, Void, JSONArray> {

        ProgressDialog progressDialog = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(DashboardActivity.this);
            progressDialog.setTitle("");
            progressDialog.setMessage("Đang tải kết quả...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected JSONArray doInBackground(Void... voids) {

            URLConnection connection = null;
            BufferedReader reader = null;

            try {

                StringBuilder sb = new StringBuilder();

                sb.append(URLEncoder.encode("exams_group_id", "UTF-8") + "=" + URLEncoder.encode(examsGroupId, "UTF-8"));
                sb.append("&" + URLEncoder.encode("icon_user_menu", "UTF-8") + "=" + URLEncoder.encode(Values.CURRENT_USER, "UTF-8"));

                String http = Values.DB_HOST + "/get-result";
                URL url = new URL(http);
                connection = url.openConnection();
                connection.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(sb.toString());
                writer.flush();

                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line = null;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                return new JSONArray(response.toString());

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }



            return null;
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {
            super.onPostExecute(jsonArray);

            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            createResultList(jsonArray);

            doAfterLoadResult();
        }
    }


}
