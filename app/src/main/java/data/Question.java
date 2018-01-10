package data;

/**
 * Created by Administrator on 1/4/2018.
 */

public class Question {

	private int id;
	private String question;
	private String chose1;
	private String chose2;
	private String chose3;
	private String chose4;
	private String examsGroup;
	private int correctChose;


	public Question() {
		this.id = 0;
		this.question = "";
		this.chose1 = "";
		this.chose2 = "";
		this.chose3 = "";
		this.chose4 = "";
		this.examsGroup = "";
		this.correctChose = 0;
	}

	public Question(int id, String question, String chose1, String chose2, String chose3, String chose4, String examsGroup, int correctChose) {
		this.id = id;
		this.question = question;
		this.chose1 = chose1;
		this.chose2 = chose2;
		this.chose3 = chose3;
		this.chose4 = chose4;
		this.examsGroup = examsGroup;
		this.correctChose = correctChose;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public void setChose1(String chose1) {
		this.chose1 = chose1;
	}

	public void setChose2(String chose2) {
		this.chose2 = chose2;
	}

	public void setChose3(String chose3) {
		this.chose3 = chose3;
	}

	public void setChose4(String chose4) {
		this.chose4 = chose4;
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

	public String getChose1() {
		return this.chose1;
	}

	public String getChose2() {
		return this.chose2;
	}

	public String getChose3() {
		return this.chose3;
	}

	public String getChose4() {
		return this.chose4;
	}

	public String getExamsGroup() {
		return this.examsGroup;
	}
	public int getCorrectChose() {
		return correctChose;
	}
}
