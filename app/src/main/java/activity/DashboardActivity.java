package activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.administrator.thitracnghiem.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
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

    private String examsGroupId;
    private ArrayList<String> qg;

    private Intent intent;

    private int score = 0;
    private int totalTime = 0;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        initComponents();

        if (intent != null) {
            examsGroupId = intent.getStringExtra("exams_group_id");
            qg = intent.getStringArrayListExtra("qg");
        }

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
                final Dialog dialog = new Dialog(DashboardActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.dialog_guide);

                String a = String.valueOf(results.get(i).getQuestionId());
                String b = String.valueOf(results.get(i).getQuestionGroupId());

                final Button btnOk = dialog.findViewById(R.id.btn_ok);
                final ImageView imageView1 = dialog.findViewById(R.id.img_question);
                final ImageView imageView2 = dialog.findViewById(R.id.img_guide);
                new LoadGuideSrcTask(imageView1, imageView2).execute(a, b);
                btnOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                dialog.show();

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
        intent = getIntent();
    }

    private void createResultList(JSONArray jsonArray) {
        int size = jsonArray.length();
        results = new ArrayList<>();
        try {
            for(int i = 0; i < size; i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                Result result = new Result();
                result.setQuestionId(object.getInt("question_id"));
                result.setQuestionGroupId(object.getInt("question_group_id"));
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
        tvTotalTime.setText(toTimeFormat(totalTime));
        fillData();
    }

    private String toTimeFormat(int time) {
        String minutes = String.valueOf(time / 60);
        String seconds = String.valueOf(time % 60);

        if (minutes.length() == 1) minutes = "0" + minutes;
        if (seconds.length() == 1) seconds = "0" + seconds;

        return minutes + ":" + seconds;
    }

    private void fillData() {
        ArrayList<Result> rs = new ArrayList<Result>();
        for (String item: qg) {
            String[] m = item.split(",");
            for (Result rss : results) {
                String a = String.valueOf(rss.getQuestionId());
                String b = String.valueOf(rss.getQuestionGroupId());

                if (a.equals(m[0]) && b.equals(m[1])) {
                    rs.add(rss);
                    break;
                }
            }
        }
        results = new ArrayList<>(rs);
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
                sb.append("&" + URLEncoder.encode("user", "UTF-8") + "=" + URLEncoder.encode(Values.CURRENT_USER, "UTF-8"));

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

    private class LoadGuideSrcTask extends AsyncTask<String, Void, String> {


        private ImageView imageView1;
        private ImageView imageView2;

        public LoadGuideSrcTask(ImageView imageView1, ImageView imageView2) {
            this.imageView1 = imageView1;
            this.imageView2 = imageView2;
        }


        @Override
        protected String doInBackground(String... strings) {

            URLConnection connection = null;
            BufferedReader reader = null;

            try {

                StringBuilder sb = new StringBuilder();

                sb.append(URLEncoder.encode("question_id", "UTF-8") + "=" + URLEncoder.encode(strings[0], "UTF-8"));
                sb.append("&" + URLEncoder.encode("question_group_id", "UTF-8") + "=" + URLEncoder.encode(strings[1], "UTF-8"));


                String http = Values.DB_HOST + "/get-guide";
                URL url = new URL(http);
                connection = url.openConnection();
                connection.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(sb.toString());
                writer.flush();

                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String reponse = null;
                reponse = reader.readLine();

                return reponse;
            } catch (IOException e) {
                e.printStackTrace();
            }



            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            String[] m = s.split(",");
            new ImageLoadTask(m[0], this.imageView1).execute();
            new ImageLoadTask(m[1], this.imageView2).execute();

        }
    }


    private class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {


        private String url;

        private ImageView imageView;

        public ImageLoadTask(String url, ImageView imageView) {
            this.url = url;
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {

            try {

                URL url = new URL(Values.RESOURCE_HOST + "/" + this.url);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();

                InputStream inputStream = connection.getInputStream();

                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                return bitmap;

            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            this.imageView.setImageBitmap(bitmap);
        }
    }


}
