package activity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

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

import data.Question;
import data.Values;

/**
 * Created by Administrator on 1/6/2018.
 */

public class QuestionInfoAcitivity extends AppCompatActivity {

    private static int questionId;

    private TextView tvQuestion;
    private TextView tvChose1;
    private TextView tvChose2;
    private TextView tvChose3;
    private TextView tvChose4;

    private Question question;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_info);

        initComponents();

        new LoadQuestionInfoTask().execute();
    }

    public static void setQuestionId(int questionId) {
        QuestionInfoAcitivity.questionId = questionId;
    }

    private void initComponents() {
        tvQuestion = findViewById(R.id.tv_question);
        tvChose1 = findViewById(R.id.tv_chose1);
        tvChose2 = findViewById(R.id.tv_chose2);
        tvChose3 = findViewById(R.id.tv_chose3);
        tvChose4 = findViewById(R.id.tv_chose4);
    }

    private void createQuestion(JSONObject jsonObject) {
        try {
            question = new Question();
            question.setId(jsonObject.getInt("question_id"));
            question.setQuestion(jsonObject.getString("question"));
            question.setChose1("A. " + jsonObject.getString("chose1"));
            question.setChose2("B. " + jsonObject.getString("chose2"));
            question.setChose3("C. " + jsonObject.getString("chose3"));
            question.setChose4("D. " + jsonObject.getString("chose4"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showQuestion() {
        tvQuestion.setText(question.getQuestion());
        tvChose1.setText(question.getChose1());
        tvChose2.setText(question.getChose2());
        tvChose3.setText(question.getChose3());
        tvChose4.setText(question.getChose4());
    }


    private class LoadQuestionInfoTask extends AsyncTask<Void, Void, JSONObject> {

        ProgressDialog progressDialog = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(QuestionInfoAcitivity.this);
            progressDialog.setTitle("");
            progressDialog.setMessage("Đang lấy dữ liệu chi tiết câu hỏi...");
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected JSONObject doInBackground(Void... voids) {

            URLConnection connection = null;
            BufferedReader reader = null;

            try {

                String data = URLEncoder.encode("questionId", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(questionId), "UTF-8");

                String http = Values.DB_HOST + "/get-question-info";
                URL url = new URL(http);
                connection = url.openConnection();
                connection.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(data);
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
            createQuestion(jsonObject);



            showQuestion();

        }
    }


}
