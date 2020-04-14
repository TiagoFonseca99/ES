package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain;

import java.io.Serializable;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

@Embeddable
public class DiscussionId implements Serializable {
    @NotNull
    @Column(name="question_id")
    private Integer questionId;

    @NotNull
    @Column(name="user_id")
    private Integer userId;

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }

    public Integer getQuestionId() {
        return questionId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getUserId() {
        return userId;
    }

    public boolean equals(Object o) {
        if (!(o instanceof DiscussionId)) {
            return false;
        }

        return ((DiscussionId) o).getQuestionId() == this.questionId && ((DiscussionId) o).getUserId() == this.userId;
    }

    public int hashCode() {
        return Objects.hash(questionId, userId);
    }
}
