package activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

import data.Answer;
import data.Question;
import data.Values;

/**
 * Created by Administrator on 1/5/2018.
 */

public class ContestActivity extends AppCompatActivity{

	private Button btnForward;
	private Button btnFinish;
	private TextView tvCurrentQuestionIndex;
	private TextView tvCurrentQuestionText;
	private TextView tvTimer;
	private TextView tvCorrect;
	private TextView tvIncorrect;
	private RadioButton rbtnChose1;
	private RadioButton rbtnChose2;
	private RadioButton rbtnChose3;
	private RadioButton rbtnChose4;
	private RadioGroup rbtnGroup;

	private List<Question> questions;
	private List<Answer> answers;
	private int currentIndex = 0;
	private int correct = 0;
	private int time = 0;
	private boolean isFinish = false;

	private PostResultTask postResultTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contest);

        initComponents();

        postResultTask = new PostResultTask();

        updateTime();

		new LoadQuestionTask().execute();
		btnForward.setOnClickListener(btnForward_Clicked);
		btnFinish.setOnClickListener(btnFinish_Clicked);

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

	@Override
	protected void onStart() {
		super.onStart();
		super.onStart();
		if (Values.CURRENT_USER.equals("")) {
			Intent intent = new Intent(ContestActivity.this, LoginActivity.class);
			startActivity(intent);
			ContestActivity.this.finish();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

	}


	private void _info() {
    	Intent intent = new Intent(ContestActivity.this, UserInfoActivity.class);
    	startActivity(intent);
	}

	private void _logout() {
    	Values.CURRENT_USER = "";
		getSharedPreferences(Values.PREFS_NAME, MODE_PRIVATE).edit().remove("user").apply();
    	Intent intent = new Intent(ContestActivity.this, LoginActivity.class);
    	startActivity(intent);
    	ContestActivity.this.finish();
	}

	private void showQuestion() {
    	if (currentIndex == Values.NUM_OF_QUESTION - 1) {
    		btnForward.setText("HOÀN THÀNH");
		}

    	if (!isFinish) {
			resetChose();
			Question q = questions.get(currentIndex);
			tvCurrentQuestionIndex.setText(String.valueOf(currentIndex + 1 ) + "/" + Values.NUM_OF_QUESTION);
			tvCurrentQuestionText.setText(q.getQuestion());
			rbtnChose1.setText(q.getChose1());
			rbtnChose2.setText(q.getChose2());
			rbtnChose3.setText(q.getChose3());
			rbtnChose4.setText(q.getChose4());
			tvCorrect.setText(String.valueOf(correct));
			tvIncorrect.setText(String.valueOf(currentIndex - correct));
		}
    	else {
			tvCorrect.setText(String.valueOf(correct));
			tvIncorrect.setText(String.valueOf(Values.NUM_OF_QUESTION - correct));
			rbtnChose1.setEnabled(false);
			rbtnChose2.setEnabled(false);
			rbtnChose3.setEnabled(false);
			rbtnChose4.setEnabled(false);
		}
    }

    private void onContest() {

    	showQuestion();
    	
    	final Handler h = new Handler(new Handler.Callback() {
    		@Override 
    		public boolean handleMessage(Message msg) {
    			time = time + 1;
    			tvTimer.setText("" + time);
    			return false;
    		}
    	});

    	h.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (!isFinish) {
					time = time + 1;
					updateTime();
					h.postDelayed(this, 1000);
				} else {
					onStop();
				}
			}
		}, 1000);

    }

    private void doAfterPostResult(String result) {
    	if (result.equals("success")) {
    		AlertDialog.Builder builder = new AlertDialog.Builder(ContestActivity.this);
    		builder.setTitle("Thông báo");
    		builder.setMessage("Cập nhật kết quả thành công.");
    		builder.setCancelable(false);
    		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					DashboardActivity.setExamsGroupId(questions.get(0).getExamsGroup());
					Intent intent = new Intent(ContestActivity.this, DashboardActivity.class);
					startActivity(intent);
					ContestActivity.this.finish();
				}
			});
    		AlertDialog dialog = builder.create();
    		dialog.show();
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(ContestActivity.this);
			builder.setTitle("Thông báo");
			builder.setMessage("Cập nhật kết quả thất bại. Bạn muốn cập nhật lại không?");
			builder.setCancelable(false);
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					postResultTask.cancel(true);
					postResultTask.execute();
				}
			});
			builder.setNegativeButton("Trở lại", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					postResultTask.cancel(true);
					showQuestion();
				}
			});
			AlertDialog dialog = builder.create();
			dialog.show();
		}
	}

    private void initComponents() {
    	btnForward = findViewById(R.id.btn_forward);
    	btnFinish = findViewById(R.id.btn_finish);
    	tvCurrentQuestionIndex = findViewById(R.id.tv_current);
    	tvCurrentQuestionText = findViewById(R.id.tv_question);
    	tvTimer = findViewById(R.id.tv_timer);
    	tvCorrect = findViewById(R.id.tv_correct);
    	tvIncorrect = findViewById(R.id.tv_incorrect);
    	rbtnChose1 = findViewById(R.id.radio_1);
    	rbtnChose2 = findViewById(R.id.radio_2);
    	rbtnChose3 = findViewById(R.id.radio_3);
    	rbtnChose4 = findViewById(R.id.radio_4);
    	rbtnGroup = findViewById(R.id.radio_group);
    }

    private int getChose() {
    	if (rbtnChose1.isChecked()) return 1;
    	if (rbtnChose2.isChecked()) return 2;
    	if (rbtnChose3.isChecked()) return 3;
    	if (rbtnChose4.isChecked()) return 4;
    	return 0;
    }

    private int getResult() {
		if (getChose() == questions.get(currentIndex).getCorrectChose()) return 1;
		return 0;
	}

    private void resetChose() {
    	rbtnChose1.setEnabled(true);
    	rbtnChose2.setEnabled(true);
    	rbtnChose3.setEnabled(true);
    	rbtnChose4.setEnabled(true);
    	rbtnGroup.clearCheck();
    }

   
    private void updateTime() {
    	String minutes = String.valueOf(time / 60);
    	String seconds = String.valueOf(time % 60);

    	if (minutes.length() == 1) minutes = "0" + minutes;
    	if (seconds.length() == 1) seconds = "0" + seconds;
    	if (!isFinish) {
    		tvTimer.setText(minutes + ":" + seconds);
		} else {
    		tvTimer.setText("HOÀN THÀNH");
		}
    }

    private JSONArray createJSONArrayFromAnswerList() {
    	JSONArray jsonArray = new JSONArray();
    	int i = answers.size();
    	while (answers.size() < Values.NUM_OF_QUESTION) {
    		answers.add(new Answer(questions.get(i).getId(), 0, 0));
    		i++;
		}
    	for(i = 0; i < answers.size(); i++) {
    		jsonArray.put(answers.get(i).getJSONObject());
		}
		return jsonArray;
	}

    View.OnClickListener btnFinish_Clicked = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			if (answers.size() < Values.NUM_OF_QUESTION) {
				AlertDialog.Builder builder = new AlertDialog.Builder(ContestActivity.this);
				builder.setTitle("Cảnh báo");
				builder.setMessage("Bạn vẫn chưa hoàn thành hết câu hỏi, bạn vẫn nộp ?");
				builder.setPositiveButton("Nộp", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						isFinish = true;
						postResultTask.execute();

					}
				});
				builder.setNegativeButton("Trở lại", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						postResultTask.cancel(true);
					}
				});
				AlertDialog dialog = builder.create();
				dialog.show();
			} else {
				postResultTask.execute();
			}
		}
	};


    View.OnClickListener btnForward_Clicked = new View.OnClickListener() {
    	@Override
    	public void onClick(View view) {
    		if (currentIndex == Values.NUM_OF_QUESTION - 1) {
				if (getChose() == 0) {
					AlertDialog.Builder builder = new AlertDialog.Builder(ContestActivity.this);
					builder.setTitle("Thông báo");
					builder.setMessage("Bạn cần chọn câu trả lời để tiếp tục.");
					builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							// Do no thing
						}
					});
					AlertDialog dialog = builder.create();
					dialog.show();
				} else {
					btnForward.setEnabled(false);
					isFinish = true;
					if (getResult() == 1) correct++;
					Answer answer = new Answer(questions.get(currentIndex).getId(), getResult(), time);
					answers.add(answer);
					time = 0;
					updateTime();
				}
			} else {
				if (getChose() == 0) {
					AlertDialog.Builder builder = new AlertDialog.Builder(ContestActivity.this);
					builder.setTitle("Thông báo");
					builder.setMessage("Bạn cần chọn câu trả lời để tiếp tục.");
					builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							// Do no thing
						}
					});
					AlertDialog dialog = builder.create();
					dialog.show();
				} else {
					if (getResult() == 1) correct++;
					Answer answer = new Answer(questions.get(currentIndex).getId(), getResult(), time);
					answers.add(answer);
					time = 0;
					updateTime();
					currentIndex++;
				}
			}
			showQuestion();
    	}
    };


    private class LoadQuestionTask extends AsyncTask<Void, Void, JSONArray> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(ContestActivity.this);
            progressDialog.setTitle("");
            progressDialog.setMessage("Đang tải câu hỏi...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected JSONArray doInBackground(Void... voids) {
            
        	URLConnection conn = null;
        	BufferedReader reader = null;

        	try {      		
        		String http = Values.DB_HOST + "/get-question";
        		URL url = new URL(http);
      		
        		conn = url.openConnection();
        		conn.setDoOutput(true);
        		reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        		StringBuilder sb = new StringBuilder();
        		String line = null;

        		while ((line = reader.readLine()) != null) {
        			sb.append(line);
        		}

        		return new JSONArray(sb.toString());
                
        	} catch(IOException e) {
        		e.printStackTrace();
        	} catch(JSONException e) {
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

            if (jsonArray.length() < Values.NUM_OF_QUESTION) {

            	AlertDialog.Builder builder = new AlertDialog.Builder(ContestActivity.this);
            	builder.setTitle("Thông báo");
            	builder.setMessage("Tải dữ liệu thất bại, bạn muốn tải lại không?");
            	builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            		@Override
            		public void onClick(DialogInterface dialogInterface, int i) {

            		}
            	});
            	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            		@Override
            		public void onClick(DialogInterface dialogInterface, int i) {
            			
            		}
            	});

            	AlertDialog dialog = builder.create();
            	dialog.show();
            } else {
            	try {
            		questions = new ArrayList<>();
					answers = new ArrayList<>();				
            		for(int i = 0; i < Values.NUM_OF_QUESTION; i++) {
            			JSONObject object = jsonArray.getJSONObject(i);
            			Question q = new Question();
            			q.setId(object.getInt("question_id"));
            			q.setQuestion(object.getString("question"));
            			q.setChose1(object.getString("chose1"));
            			q.setChose2(object.getString("chose2"));
            			q.setChose3(object.getString("chose3"));
            			q.setChose4(object.getString("chose4"));
            			q.setExamsGroup(String.valueOf(object.getInt("exam_group_id")));
            			q.setCorrectChose(object.getInt("chose_correct"));
            			questions.add(q);
            		}
            	} catch (JSONException e) {
            		e.printStackTrace();
            	}
            	onContest();         	
            }         
        }
    }

    private class PostResultTask extends AsyncTask<Void, Void, String> {


    	ProgressDialog progressDialog = null;

    	@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = new ProgressDialog(ContestActivity.this);
			progressDialog.setTitle("");
			progressDialog.setMessage("Đang cập nhật kết quả");
			progressDialog.setCancelable(false);
			progressDialog.show();
		}

		@Override
		protected String doInBackground(Void... voids) {

    		URLConnection connection = null;
			BufferedReader reader = null;

    		try {
    			String examsGroupId = URLEncoder.encode(String.valueOf(questions.get(0).getExamsGroup()), "UTF-8");
    			String user = URLEncoder.encode(Values.CURRENT_USER, "UTF-8");
    			String dataArray = URLEncoder.encode(createJSONArrayFromAnswerList().toString(),"UTF-8");


    			StringBuffer data = new StringBuffer();

    			data.append(URLEncoder.encode("exams_group_id", "UTF-8") + "=" + examsGroupId);
    			data.append("&" + URLEncoder.encode("icon_user_menu", "UTF-8") + "=" + user);
    			data.append("&" + URLEncoder.encode("data", "UTF-8") + "=" + dataArray);

    			String http = Values.DB_HOST + "/post-result";
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

    		return "hehe";
		}

		@Override
		protected void onPostExecute(String s) {
			super.onPostExecute(s);

			if (progressDialog.isShowing()) {
				progressDialog.dismiss();
			}


			doAfterPostResult(s);



		}
	}
}
