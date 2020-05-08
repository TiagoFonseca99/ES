package pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto.DiscussionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ImageDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public class StatementQuestionDto implements Serializable {
    private QuestionDto question;
    private List<DiscussionDto> discussions;
    private boolean hasUserDiscussion = false;
    private Integer quizQuestionId;
    private String content;
    private List<StatementOptionDto> options;
    private ImageDto image;
    private Integer sequence;

    public StatementQuestionDto(QuestionAnswer questionAnswer) {
        this.content = questionAnswer.getQuizQuestion().getQuestion().getContent();
        if (questionAnswer.getQuizQuestion().getQuestion().getImage() != null) {
            this.image = new ImageDto(questionAnswer.getQuizQuestion().getQuestion().getImage());
        }

        this.setQuestion(new QuestionDto(questionAnswer.getQuizQuestion().getQuestion()));
        this.quizQuestionId = questionAnswer.getQuizQuestion().getId();
        this.options = questionAnswer.getQuizQuestion().getQuestion().getOptions().stream().map(StatementOptionDto::new)
                .collect(Collectors.toList());
        this.sequence = questionAnswer.getSequence();

        int userId = questionAnswer.getQuizAnswer().getUser().getId();
        this.discussions = questionAnswer.getQuizQuestion().getQuestion().getDiscussions().stream()
                .map(DiscussionDto::new).filter(discussion -> {
                    if (discussion.isAvailable()) {
                        return true;
                    } else if (discussion.getUserId() == userId) {
                        this.hasUserDiscussion = true;
                        return true;
                    } else {
                        return false;
                    }
                }).collect(Collectors.toList());
    }

    public boolean isHasUserDiscussion() {
		return hasUserDiscussion;
	}

	public void setHasUserDiscussion(boolean hasUserDiscussion) {
		this.hasUserDiscussion = hasUserDiscussion;
	}

	public List<DiscussionDto> getDiscussions() {
        return discussions;
    }

    public void addDiscussion(DiscussionDto discussion) {
        this.discussions.add(discussion);
    }

    public QuestionDto getQuestion() {
        return question;
    }

    public void setQuestion(QuestionDto question) {
        this.question = question;
    }

    public Integer getQuizQuestionId() {
        return quizQuestionId;
    }

    public void setQuizQuestionId(Integer quizQuestionId) {
        this.quizQuestionId = quizQuestionId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<StatementOptionDto> getOptions() {
        return options;
    }

    public void setOptions(List<StatementOptionDto> options) {
        this.options = options;
    }

    public ImageDto getImage() {
        return image;
    }

    public void setImage(ImageDto image) {
        this.image = image;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    @Override
    public String toString() {
        return "StatementQuestionDto{" + ", content='" + content + '\'' + ", options=" + options + ", image=" + image
                + ", sequence=" + sequence + '}';
    }
}
