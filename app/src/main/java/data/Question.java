package data;

/**
 * Created by Administrator on 1/4/2018.
 */

public class Question {

	private int id;
	private String question;
	private String questionGroup;
	private String examsGroup;
	private int correctChose;


	public Question() {
		this.id = 0;
		this.question = null;
		this.questionGroup = "";
		this.examsGroup = "";
		this.correctChose = 0;
	}

	public Question(int id, String question, String questionGroup, String examsGroup, int correctChose) {
		this.id = id;
		this.question = question;
		this.questionGroup = questionGroup;
		this.examsGroup = examsGroup;
		this.correctChose = correctChose;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public void setQuestionGroup(String questionGroup) {
		this.questionGroup = questionGroup;
	}

	public void setExamsGroup(String examsGroup) {
		this.examsGroup = examsGroup;
	}

	public void setCorrectChose(int correctChose) {
		this.correctChose = correctChose;
	}

	public int getId() {
		return this.id;
	}

	public String getQuestion() {
		return this.question;
	}

	public String getQuestionGroup() {
		return this.questionGroup;
	}

	public String getExamsGroup() {
		return this.examsGroup;
	}
	public int getCorrectChose() {
		return correctChose;
	}
}
