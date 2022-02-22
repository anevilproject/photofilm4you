package edu.uoc.pdp.core.model.forum;

import edu.uoc.pdp.db.entity.Question;

public class ForumQuestion {

    private Question question;
    private long responses;


    public ForumQuestion(Question question, long responses) {
        this.question = question;
        this.responses = responses;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public long getResponses() {
        return responses;
    }

    public void setResponses(long responses) {
        this.responses = responses;
    }
}
