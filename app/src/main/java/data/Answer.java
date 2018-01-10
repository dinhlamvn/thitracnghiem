package data;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 1/4/2018.
 */

public class Answer {
	private int questionId;
	private int result;
	private int time;

	public Answer() {
		this.questionId = 0;
		this.result = 0;
		this.time = 0;
	}

	public Answer(int questionId, int result, int time) {
		this.questionId = questionId;
		this.result = result;
		this.time = time;
	}

	public void setQuestionId(int questionId) {
		this.questionId = questionId;
	}

	public void setResult(int result) {
		this.result = result;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public int getQuestionId() {
		return this.questionId;
	}

	public int getResult() {
		return this.result;
	}

	public int getTime() {
		return this.time;
	}


	public JSONObject getJSONObject() {
		JSONObject object = new JSONObject();
		try {
			object.put("questionId", this.questionId);
			object.put("result", this.result);
			object.put("time", this.time);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return object;
	}
}
