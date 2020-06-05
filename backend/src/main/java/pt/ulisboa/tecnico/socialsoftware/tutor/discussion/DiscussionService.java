package pt.ulisboa.tecnico.socialsoftware.tutor.discussion;

import java.sql.SQLException;
import java.util.stream.Collectors;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Discussion;
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
    private ReplyRepository replyRepository;

    @Autowired
    private UserRepository userRepository;

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
        List<ReplyDto> replies = discussionDto.getReplies();
        if (replies != null && !replies.isEmpty()) {
            for (ReplyDto reply : replies) {
                User teacher = userRepository.findById(reply.getUserId())
                        .orElseThrow(() -> new TutorException(USER_NOT_FOUND, reply.getUserId()));

                this.entityManager.persist(new Reply(teacher, discussion, reply));
                this.entityManager.persist(discussion);
            }
        }

        return new DiscussionDto(discussion);
    }

    @Retryable(value = { SQLException.class }, backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public DiscussionDto setAvailability(DiscussionDto discussionDto) {
        Discussion discussion = discussionRepository
                .findByUserIdQuestionId(discussionDto.getUserId(), discussionDto.getQuestionId())
                .orElseThrow(() -> new TutorException(DISCUSSION_NOT_FOUND, discussionDto.getUserId(),
                        discussionDto.getQuestionId()));
        discussion.setAvailability(discussionDto.isAvailable());
        return discussionDto;
    }

    @Retryable(value = { SQLException.class }, backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ReplyDto giveReply(ReplyDto replyDto, DiscussionDto discussionDto) {
        checkReplyDto(replyDto);
        checkDiscussionDto(discussionDto);

        User user = userRepository.findById(replyDto.getUserId())
                .orElseThrow(() -> new TutorException(USER_NOT_FOUND, replyDto.getUserId()));

        checkUserAndDiscussion(user, discussionDto);

        Discussion discussion = discussionRepository
                .findByUserIdQuestionId(discussionDto.getUserId(), discussionDto.getQuestionId())
                .orElseThrow(() -> new TutorException(DISCUSSION_NOT_FOUND, discussionDto.getUserId(),
                        discussionDto.getQuestionId()));

        Reply reply = new Reply(user, discussion, replyDto);
        this.entityManager.persist(reply);
        this.entityManager.merge(discussion);

        return new ReplyDto(reply);
    }

    @Retryable(value = { SQLException.class }, backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public boolean removeReply(Integer userId, Integer replyId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new TutorException(USER_NOT_FOUND, userId));

        Reply reply = replyRepository.findById(replyId).orElseThrow(() -> new TutorException(REPLY_NOT_FOUND, replyId));

        if (!hasPermission(user, reply.getId())) {
            throw new TutorException(REPLY_UNAUTHORIZED_DELETER, userId);
        }

        reply.remove();

        this.entityManager.remove(reply);

        return true;
    }

    @Retryable(value = { SQLException.class }, backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public boolean removeDiscussion(Integer userId, Integer creatorId, Integer questionId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new TutorException(USER_NOT_FOUND, userId));

        Discussion discussion = discussionRepository.findByUserIdQuestionId(creatorId, questionId)
                .orElseThrow(() -> new TutorException(DISCUSSION_NOT_FOUND, creatorId, questionId));

        if (!hasPermission(user, creatorId)) {
            throw new TutorException(DISCUSSION_UNAUTHORIZED_DELETER, userId);
        }

        discussion.remove();

        this.entityManager.remove(discussion);

        return true;
    }

    @Retryable(value = { SQLException.class }, backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public DiscussionDto editDiscussion(Integer userId, DiscussionDto discussionDto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new TutorException(USER_NOT_FOUND, userId));

        checkDiscussionDto(discussionDto);

        Discussion discussion = discussionRepository
                .findByUserIdQuestionId(discussionDto.getUserId(), discussionDto.getQuestionId())
                .orElseThrow(() -> new TutorException(DISCUSSION_NOT_FOUND, discussionDto.getUserId(),
                        discussionDto.getQuestionId()));

        if (!hasPermission(user, discussionDto)) {
            throw new TutorException(DISCUSSION_UNAUTHORIZED_EDITOR);
        }

        if (user.isTeacher()) {
            discussion.setAvailability(discussionDto.isAvailable());
        }

        discussion.setContent(discussionDto.getContent());

        this.entityManager.merge(discussion);

        return new DiscussionDto(discussion);
    }

    private boolean hasPermission(User user, Integer userId) {
        return user.isTeacher() || user.getId().equals(userId);
    }

    private boolean hasPermission(User user, DiscussionDto discussion) {
        return hasPermission(user, discussion.getUserId());
    }

    private void checkReplyDto(ReplyDto replyDto) {
        if (replyDto.getUserId() == null || replyDto.getMessage() == null || replyDto.getMessage().equals("")) {
            throw new TutorException(REPLY_MISSING_DATA);
        }
    }

    private void checkUserAndDiscussion(User user, DiscussionDto discussion) {
        if (!hasPermission(user, discussion) && !discussion.isAvailable()) {
            throw new TutorException(REPLY_UNAUTHORIZED_USER);
        }
    }

    private void checkDiscussionDto(DiscussionDto discussion) {
        if (discussion.getQuestion() == null || discussion.getUserId() == null || discussion.getContent() == null
                || discussion.getContent().trim().length() == 0) {
            throw new TutorException(DISCUSSION_MISSING_DATA);
        }
    }

    private void checkUserAndQuestion(User user, Question question) {
        if (user.getRole() == User.Role.TEACHER) {
            throw new TutorException(DISCUSSION_NOT_STUDENT_CREATOR);
        }

        if (discussionRepository.findByUserIdQuestionId(user.getId(), question.getId()).isPresent()) {
            throw new TutorException(DUPLICATE_DISCUSSION, user.getId(), question.getId());
        }

        checkUserAnswered(user, question);
    }

    private void checkUserAnswered(User user, Question question) {
        if (!user.checkQuestionAnswered(question)) {
            throw new TutorException(QUESTION_NOT_ANSWERED, question.getId());
        }
    }
}
