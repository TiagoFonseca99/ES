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
    private DiscussionDto discussion;
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

        // There will be only 1 due to restrictions on the database, findFirst is correct
        this.discussion = questionAnswer.getQuizQuestion().getQuestion().getDiscussions().stream()
                .filter(questionAnswer.getQuizAnswer().getUser().getDiscussions()::contains)
            .map(DiscussionDto::new)
            .findFirst()
            .orElse(null);
    }

    public DiscussionDto getDiscussion() {
        return discussion;
    }

    public void setDiscussion(DiscussionDto discussion) {
        this.discussion = discussion;
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
