package pt.ulisboa.tecnico.socialsoftware.tutor.discussion;

import java.sql.SQLException;
import java.time.LocalTime;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.constraints.Null;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Discussion;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.DiscussionId;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Reply;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto.DiscussionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto.ReplyDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.repository.DiscussionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.repository.ReplyRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Service
public class DiscussionService {
    @Autowired
    private DiscussionRepository discussionRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReplyRepository replyRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Retryable(value = { SQLException.class }, backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<DiscussionDto> findDiscussionsByQuestionId(Integer questionId) {
        return discussionRepository.findByQuestionId(questionId).stream().map(DiscussionDto::new)
                .collect(Collectors.toList());
    }

    @Retryable(value = { SQLException.class }, backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<DiscussionDto> findDiscussionsByUserId(Integer userId) {
        return discussionRepository.findByUserId(userId).stream().map(DiscussionDto::new).collect(Collectors.toList());
    }

    @Retryable(value = { SQLException.class }, backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public DiscussionDto findDiscussionByUserIdAndQuestionId(Integer userId, Integer questionId) {
        return discussionRepository.findByUserIdQuestionId(userId, questionId).map(DiscussionDto::new)
                .orElseThrow(() -> new TutorException(DISCUSSION_NOT_FOUND, userId, questionId));
    }

    @Retryable(value = { SQLException.class }, backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public DiscussionDto createDiscussion(DiscussionDto discussionDto) {
        checkDiscussionDto(discussionDto);

        User user = userRepository.findById(discussionDto.getUserId())
                .orElseThrow(() -> new TutorException(USER_NOT_FOUND, discussionDto.getUserId()));
        Question question = questionRepository.findById(discussionDto.getQuestionId())
                .orElseThrow(() -> new TutorException(QUESTION_NOT_FOUND, discussionDto.getQuestionId()));

        checkUserAndQuestion(user, question);

        Discussion discussion = new Discussion(user, question, discussionDto);
        this.entityManager.persist(discussion);
        if (discussionDto.getReplyDto() != null) {

            User teacher = userRepository.findById(discussionDto.getReplyDto().getTeacherId())
                        .orElseThrow(() -> new TutorException(USER_NOT_FOUND, discussionDto.getReplyDto().getTeacherId()));
                        
            Reply reply = new Reply(teacher, discussion, discussionDto.getReplyDto());
            this.entityManager.refresh(discussion);
            this.entityManager.persist(reply);

        }
        return new DiscussionDto(discussion);
    }

    @Retryable(value = { SQLException.class }, backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ReplyDto giveReply(ReplyDto replyDto, Discussion discussion) {
        checkReplyDto(replyDto);

        User teacher = userRepository.findById(replyDto.getTeacherId())
        .orElseThrow(() -> new TutorException(USER_NOT_FOUND, replyDto.getTeacherId()));
        checkTeacherAndDiscussion(teacher, discussion);

        Reply reply = new Reply(teacher, discussion, replyDto);
        this.entityManager.persist(reply);
        discussion = this.entityManager.merge(discussion);

        return new ReplyDto(reply);

    }

    private void checkReplyDto(ReplyDto replyDto) {
        if (replyDto.getTeacherId() == null || replyDto.getMessage() == null) {
            throw new TutorException(REPLY_MISSING_DATA);
        }
    }

    private void checkTeacherAndDiscussion(User teacher, Discussion discussion) {
        if(teacher.getRole() != User.Role.TEACHER) {
            throw new TutorException(REPLY_NOT_TEACHER_CREATOR);
        }
        if(!replyRepository.findByTeacherIdDiscussionId(teacher.getId(), discussion.getId().getUserId(), discussion.getId().getQuestionId()).isEmpty()){
            throw new TutorException(DUPLICATE_REPLY, teacher.getId());
        }
    }

    private void checkDiscussionDto(DiscussionDto discussion) {
        if (discussion.getQuestion() == null || discussion.getUserId() == null) {
            throw new TutorException(DISCUSSION_MISSING_DATA);
        }

    }

    private void checkUserAndQuestion(User user, Question question) {
        if(user.getRole() == User.Role.TEACHER) {
            throw new TutorException(DISCUSSION_NOT_STUDENT_CREATOR);
        }

        if(!discussionRepository.findByUserIdQuestionId(user.getId(), question.getId()).isEmpty()){
            throw new TutorException(DUPLICATE_DISCUSSION, user.getId(), question.getId());
        }

        if (!user.checkQuestionAnswered(question)) {
            throw new TutorException(QUESTION_NOT_ANSWERED, question.getId());
        }
    }
}
