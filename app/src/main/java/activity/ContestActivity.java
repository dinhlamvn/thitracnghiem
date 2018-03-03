package activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.administrator.thitracnghiem.MainActivity;
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

import data.Answer;
import data.Question;
import data.Values;

/**
 * Created by Administrator on 1/5/2018.
 */

public class ContestActivity extends AppCompatActivity {

	private Button btnForward;
	private Button btnFinish;
	private TextView tvCurrentQuestionIndex;
	private TextView tvTimer;
	private TextView tvCorrect;
	private TextView tvIncorrect;
	private RadioButton rbtnChose1;
	private RadioButton rbtnChose2;
	private RadioButton rbtnChose3;
	private RadioButton rbtnChose4;
	private RadioGroup rbtnGroup;
	private RadioGroup rbtnGroup2;
	private ImageView imgvQuestion;

	private List<Question> questions;
	private List<Answer> answers;
	private int currentIndex = 0;
	private int correct = 0;
	private int time = 0;
	private int maxTime = 1800;
	private boolean isFinish = false;

	private boolean isLoadQuestion = true;

	private ArrayList<String> qg;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contest);

        initComponents();

        updateTime();

		new LoadQuestionTask().execute();
		btnForward.setOnClickListener(btnForward_Clicked);
		btnFinish.setOnClickListener(btnFinish_Clicked);



		rbtnGroup.setOnCheckedChangeListener(listener1);
		rbtnGroup2.setOnCheckedChangeListener(listener2);


    }

    private RadioGroup.OnCheckedChangeListener listener1 = new RadioGroup.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup radioGroup, int i) {
			if (i != -1) {
				rbtnGroup2.setOnCheckedChangeListener(null);
				rbtnGroup2.clearCheck();
				rbtnGroup2.setOnCheckedChangeListener(listener2);
			}
		}
	};

    private RadioGroup.OnCheckedChangeListener listener2 = new RadioGroup.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup radioGroup, int i) {
			if (i != -1) {
				rbtnGroup.setOnCheckedChangeListener(null);
				rbtnGroup.clearCheck();
				rbtnGroup.setOnCheckedChangeListener(listener1);
			}
		}
	};

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

	@Override
	public void onBackPressed() {
		AlertDialog dialog = new AlertDialog.Builder(ContestActivity.this)
				.setTitle("Cảnh báo")
				.setMessage("Bạn đang thi, bạn thật sự muốn thoát?")
				.setCancelable(false)
				.setPositiveButton("Có", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						handlerLoadQuestion.removeCallbacksAndMessages(null);
						Intent intent = new Intent(ContestActivity.this, ChoserActivity.class);
						startActivity(intent);
						ContestActivity.this.finish();
					}
				})
				.setNegativeButton("Không", null)
				.create();
		dialog.show();
	}


	Handler handlerLoadQuestion = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message message) {
			return false;
		}
	});

    Runnable runnableLoadQuestion = new Runnable() {
		@Override
		public void run() {
			AlertDialog dialog = new AlertDialog.Builder(ContestActivity.this)
					.setTitle("Thông báo")
					.setMessage("Quá thời gian phản hồi từ server.")
					.setCancelable(false)
					.setPositiveButton("Khởi động lại ứng dụng", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							Values.CURRENT_USER = "";
							Intent intent = new Intent(ContestActivity.this, MainActivity.class);
							startActivity(intent);
							ContestActivity.this.finish();
						}
					})
					.setNegativeButton("Tải câu hỏi lại", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							isLoadQuestion = true;
							new LoadQuestionTask().execute();
						}
					})
					.create();
			dialog.show();
		}
	};

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
    		btnForward.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
		}

    	if (!isFinish) {
			Question q = questions.get(currentIndex);
			tvCurrentQuestionIndex.setText(String.valueOf(currentIndex + 1 ) + "/" + Values.NUM_OF_QUESTION);
			new ImageDownLoadTask(q.getQuestion(), imgvQuestion).execute();
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
    			return false;
    		}
    	});

    	h.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (!isFinish) {
					time = time + 1;
					maxTime = maxTime - 1;
					updateTime();
					if (maxTime == 0) {
						isFinish = true;
						AlertDialog.Builder builder = new AlertDialog.Builder(ContestActivity.this)
								.setTitle("Thông báo")
								.setMessage("Bạn đã hết thời gian làm bài.")
								.setCancelable(false)
								.setNegativeButton("NỘP", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialogInterface, int i) {
										new PostResultTask().execute();
									}
								});
						AlertDialog dialog = builder.create();
						dialog.show();
					} else {
						h.postDelayed(this, 1000);
					}

				} else {
					onStop();
				}
			}
		}, 1000);
    }

    private void onLoadQuestion(final ProgressDialog progressDialog) {

    	handlerLoadQuestion.postDelayed(runnableLoadQuestion, 5000);
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
					if (correct >= 15) {
						Dialog dialog = new Dialog(ContestActivity.this);
						dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
						dialog.setCancelable(false);
						dialog.setContentView(R.layout.dialog_update_best_user);
						dialog.show();
						final TextView tvTitle = dialog.findViewById(R.id.tv_title);
						final TextView tvMessage = dialog.findViewById(R.id.tv_message);
						final EditText edtEmail = dialog.findViewById(R.id.edt_email);
						final EditText edtPhone = dialog.findViewById(R.id.edt_phone);
						final Button btnSubmit = dialog.findViewById(R.id.btn_submit);

						tvTitle.setText("Chúc mừng");
						tvMessage.setText("Bạn đã vượt qua kỳ thi online." +
							"Hãy để lại thông tin để chúng tôi liên hệ bạn.");

						btnSubmit.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View view) {
								String a = edtEmail.getText().toString();
								String b = edtPhone.getText().toString();
								if (a.isEmpty() &&
										b.isEmpty()) {
									AlertDialog.Builder builder1 = new AlertDialog.Builder(ContestActivity.this)
											.setTitle("Thông báo")
											.setMessage("Hãy nhập vào thông tin liên hệ")
											.setCancelable(true)
											.setNegativeButton("OK", null);
									AlertDialog dialog1 = builder1.create();
									dialog1.show();
								} else {
									new PostEmailAndPhone().execute(a, b);
								}
							}
						});
					} else {
						Intent intent = new Intent(ContestActivity.this, DashboardActivity.class);
						intent.putExtra("exams_group_id", questions.get(0).getExamsGroup());
						intent.putStringArrayListExtra("qg", qg);
						startActivity(intent);
						ContestActivity.this.finish();
					}


				}
			});
    		AlertDialog dialog = builder.create();
    		dialog.show();
		} else if (result.equals("failed")){
			AlertDialog.Builder builder = new AlertDialog.Builder(ContestActivity.this);
			builder.setTitle("Thông báo");
			builder.setMessage("Cập nhật kết quả thất bại. Bạn muốn cập nhật lại không?");
			builder.setCancelable(false);
			builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					new PostResultTask().execute();
				}
			});
			builder.setNegativeButton("Không", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					showQuestion();
				}
			});
			AlertDialog dialog = builder.create();
			dialog.show();
		} else {
			AlertDialog dialog = new AlertDialog.Builder(ContestActivity.this)
					.setTitle("Thông báo")
					.setMessage("Không phản hồi từ server. Hãy chắc rằng bạn đang online.")
					.setCancelable(false)
					.setPositiveButton("Nộp lại", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							new PostResultTask().execute();
						}
					})
					.setNegativeButton("Hủy", null).create();
			dialog.show();

		}
	}

	private void doAfterUpdateContacInfo(String s) {

		if (s.equals("success")) {
			AlertDialog dialog = new AlertDialog.Builder(ContestActivity.this)
					.setTitle("Thông báo")
					.setMessage("Cập nhật thành công")
					.setCancelable(false)
					.setNegativeButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							Intent intent = new Intent(ContestActivity.this, DashboardActivity.class);
							intent.putExtra("exams_group_id", questions.get(0).getExamsGroup());
							intent.putStringArrayListExtra("qg", qg);
							startActivity(intent);
							ContestActivity.this.finish();
						}
					})
					.create();
			dialog.show();
		}
	}

    private void initComponents() {
    	btnForward = findViewById(R.id.btn_forward);
    	btnFinish = findViewById(R.id.btn_finish);
    	tvCurrentQuestionIndex = findViewById(R.id.tv_current);
    	tvTimer = findViewById(R.id.tv_timer);
    	tvCorrect = findViewById(R.id.tv_correct);
    	tvIncorrect = findViewById(R.id.tv_incorrect);
    	rbtnChose1 = findViewById(R.id.radio_1);
    	rbtnChose2 = findViewById(R.id.radio_2);
    	rbtnChose3 = findViewById(R.id.radio_3);
    	rbtnChose4 = findViewById(R.id.radio_4);
    	rbtnGroup = findViewById(R.id.radio_group1);
    	rbtnGroup2 = findViewById(R.id.radio_group2);
    	imgvQuestion = findViewById(R.id.img_question);
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

    private void updateTime() {
    	String minutes = String.valueOf(maxTime / 60);
    	String seconds = String.valueOf(maxTime % 60);

    	if (minutes.length() == 1) minutes = "0" + minutes;
    	if (seconds.length() == 1) seconds = "0" + seconds;
    	if (!isFinish) {
    		tvTimer.setText(minutes + ":" + seconds);
		} else {
    		tvTimer.setText("HOÀN THÀNH");
		}
    }

    private void handleNextQuestion() {

    	final ProgressDialog progressDialog = new ProgressDialog(ContestActivity.this);
    	progressDialog.setTitle("");
    	progressDialog.setMessage("Đang kiểm tra kết quả...");
    	progressDialog.setCancelable(false);
    	progressDialog.show();
    	new Handler(new Handler.Callback() {
			@Override
			public boolean handleMessage(Message message) {
				return false;
			}
		}).postDelayed(new Runnable() {
			@Override
			public void run() {
				if (!isFinish) {
					progressDialog.dismiss();
					showQuestion();
					onStop();
				}
			}
		}, 1000);
	}

    private JSONArray createJSONArrayFromAnswerList() {
    	JSONArray jsonArray = new JSONArray();
    	int i = answers.size();
    	while (answers.size() < Values.NUM_OF_QUESTION) {
    		answers.add(new Answer(questions.get(i).getId(), Integer.parseInt(questions.get(i).getQuestionGroup()),0, 0));
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
						btnForward.setEnabled(false);
						if (getChose() != 0) {
							if (getResult() == 1) correct++;
							Answer answer = new Answer(questions.get(currentIndex).getId(), Integer.parseInt(questions.get(currentIndex).getQuestionGroup()), getResult(), time);
							answers.add(answer);
							time = 0;
							//updateTime();
							rbtnChose1.setEnabled(false);
							rbtnChose2.setEnabled(false);
							rbtnChose3.setEnabled(false);
							rbtnChose4.setEnabled(false);
						}
						new PostResultTask().execute();
					}
				});
				builder.setNegativeButton("Trở lại", null);
				AlertDialog dialog = builder.create();
				dialog.show();
			} else {
				new PostResultTask().execute();
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
					builder.setNegativeButton("OK", null);
					AlertDialog dialog = builder.create();
					dialog.show();
				} else {
					btnForward.setEnabled(false);
					isFinish = true;
					if (getResult() == 1) correct++;
					Answer answer = new Answer(questions.get(currentIndex).getId(), Integer.parseInt(questions.get(currentIndex).getQuestionGroup()) ,getResult(), time);
					answers.add(answer);
					time = 0;
					//updateTime();
					rbtnChose1.setEnabled(false);
					rbtnChose2.setEnabled(false);
					rbtnChose3.setEnabled(false);
					rbtnChose4.setEnabled(false);
				}
			} else {
				if (getChose() == 0) {
					AlertDialog.Builder builder = new AlertDialog.Builder(ContestActivity.this);
					builder.setTitle("Thông báo");
					builder.setMessage("Bạn cần chọn câu trả lời để tiếp tục.");
					builder.setNegativeButton("OK", null);
					AlertDialog dialog = builder.create();
					dialog.show();
				} else {
					if (getResult() == 1) correct++;
					Answer answer = new Answer(questions.get(currentIndex).getId(), Integer.parseInt(questions.get(currentIndex).getQuestionGroup()),getResult(), time);
					answers.add(answer);
					time = -1;
					currentIndex++;
					rbtnGroup.clearCheck();
					rbtnGroup2.clearCheck();
					handleNextQuestion();
				}
			}
    	}
    };


    private class LoadQuestionTask extends AsyncTask<Void, Void, JSONArray> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(ContestActivity.this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setTitle("");
            progressDialog.setMessage("Đang tải câu hỏi...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            onLoadQuestion(progressDialog);
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
        		return new JSONArray();
        	} catch(JSONException e) {
        		return new JSONArray();
        	}
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {
            super.onPostExecute(jsonArray);

            if (progressDialog.isShowing()) {
            	progressDialog.dismiss();
            }
			questions = new ArrayList<>();
			answers = new ArrayList<>();
			qg = new ArrayList<>();
			handlerLoadQuestion.removeCallbacksAndMessages(null);
            if (jsonArray.length() < Values.NUM_OF_QUESTION || jsonArray.isNull(0)) {

            	AlertDialog.Builder builder = new AlertDialog.Builder(ContestActivity.this);
            	builder.setTitle("Thông báo");
            	builder.setMessage("Tải dữ liệu thất bại. Hãy chắc rằng bạn đang online.");
            	builder.setCancelable(false);
            	builder.setPositiveButton("Tải lại", new DialogInterface.OnClickListener() {
            		@Override
            		public void onClick(DialogInterface dialogInterface, int i) {
						questions.clear();
						answers.clear();
            			isLoadQuestion = true;
						new LoadQuestionTask().execute();
            		}
            	});
            	builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						Intent intent = new Intent(ContestActivity.this, ChoserActivity.class);
						startActivity(intent);
						ContestActivity.this.finish();
					}
				});
            	AlertDialog dialog = builder.create();
            	dialog.show();
            } else {
            	try {
            		for(int i = 0; i < Values.NUM_OF_QUESTION; i++) {
            			JSONObject object = jsonArray.getJSONObject(i);
            			Question q = new Question();
            			q.setId(object.getInt("question_id"));
            			q.setQuestion(object.getString("question"));
            			q.setQuestionGroup(object.getString("question_group_id"));
            			q.setExamsGroup(String.valueOf(object.getInt("exam_group_id")));
            			q.setCorrectChose(object.getInt("chose_correct"));
            			String s = q.getId() + "," + q.getQuestionGroup();
						Log.e("Q", q.getQuestion().toString());
						questions.add(q);
            			qg.add(s);
            			progressDialog.incrementProgressBy(5);
            		}
					onContest();
            	} catch (JSONException e) {
            		e.printStackTrace();
            	}
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
    			data.append("&" + URLEncoder.encode("user", "UTF-8") + "=" + user);
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
				return "time out";
			}
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

	private class ImageDownLoadTask extends AsyncTask<Void, Void, Bitmap>{

		// Url of image
		private String url;

		// ImageView to set source
		private ImageView imageView;

		public ImageDownLoadTask(String url, ImageView imageView) {
			this.url = Values.RESOURCE_HOST + "/" + url;
			this.imageView = imageView;
		}

		@Override
		protected Bitmap doInBackground(Void... params) {

			try{
				// Create url object
				URL url = new URL(this.url);

				// Open connection
				HttpURLConnection connection = (HttpURLConnection)url.openConnection();
				connection.setDoInput(true);
				connection.connect();

				// Read data from server
				InputStream inputStream = connection.getInputStream();

				// Convert data to bitmap
				Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

				return bitmap;

			}catch(IOException e){
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			imageView.setImageBitmap(bitmap);

		}
	}

	private class PostEmailAndPhone extends AsyncTask<String, Void, String> {


    	ProgressDialog dialog = null;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new ProgressDialog(ContestActivity.this);
			dialog.setTitle("");
			dialog.setMessage("Đang cập nhật...");
			dialog.setCancelable(false);
			dialog.show();
		}

		@Override
		protected String doInBackground(String... strings) {

			URLConnection connection = null;
			BufferedReader reader = null;

			try {
				String data = "";
				data += URLEncoder.encode("email","UTF-8") + "=" + URLEncoder.encode(strings[0], "UTF-8");
				data += "&" + URLEncoder.encode("phone","UTF-8") + "=" + URLEncoder.encode(strings[1], "UTF-8");

				URL url = new URL(Values.DB_HOST + "/update-contact-info");
				connection = url.openConnection();
				connection.setDoOutput(true);
				OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
				writer.write(data);
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
			if (dialog.isShowing()) {
				dialog.dismiss();
			}

			doAfterUpdateContacInfo(s);
		}
	}

}
