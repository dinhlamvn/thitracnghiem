package data;

/**
 * Created by Administrator on 1/6/2018.
 */

public class Result {
    private int questionId;
    private int questionGroupId;
    private String status;
    private int timeAnswer;

    public Result() {
        this.questionId = 0;
        this.questionGroupId = 0;
        this.status = "";
        this.timeAnswer = 0;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public void setQuestionGroupId(int questionGroupId) {
        this.questionGroupId = questionGroupId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTimeAnswer(int timeAnswer) {
        this.timeAnswer = timeAnswer;
    }

    public int getQuestionId() {
        return questionId;
    }

    public int getQuestionGroupId() {
        return this.questionGroupId;
    }

    public String getStatus() {
        return status;
    }

    public int getTimeAnswer() {
        return timeAnswer;
    }
}
