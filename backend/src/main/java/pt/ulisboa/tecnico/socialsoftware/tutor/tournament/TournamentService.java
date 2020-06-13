package pt.ulisboa.tecnico.socialsoftware.tutor.tournament;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuizAnswerRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.NotificationService;
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.NotificationsCreation;
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.domain.Notification;
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.dto.NotificationDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Assessment;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.TopicConjunction;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicConjunctionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.statement.StatementService;
import pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementCreationDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementQuizDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.repository.TournamentRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.UserDto;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.tutor.notifications.NotificationsMessage.*;
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Service
public class TournamentService {

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private StatementService statementService;

    @Autowired
    private QuizAnswerRepository quizAnswerRepository;

    @Autowired
    private TopicConjunctionRepository topicConjunctionRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Retryable(
      value = { SQLException.class },
      backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public TournamentDto createTournament(Integer userId, List<Integer> topicsId, TournamentDto tournamentDto) {
        checkInput(userId, tournamentDto);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TutorException(USER_NOT_FOUND, userId));

        if (user.getRole() != User.Role.STUDENT) {
            throw  new TutorException(USER_NOT_STUDENT, user.getId());
        }

        List<Topic> topics = new ArrayList<>();
        for (Integer topicId : topicsId) {
            Topic topic = topicRepository.findById(topicId)
                    .orElseThrow(() -> new TutorException(TOPIC_NOT_FOUND, topicId));
            topics.add(topic);
        }

        if (topics.isEmpty()) {
            throw new TutorException(TOURNAMENT_NOT_CONSISTENT, "topics");
        }

        Tournament tournament = new Tournament(user, topics, tournamentDto);
        this.entityManager.persist(tournament);

        return new TournamentDto(tournament);
    }

    @Retryable(
    value = { SQLException.class },
    backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void addTopic(Integer userId, TournamentDto tournamentDto, Integer topicId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TutorException(USER_NOT_FOUND, userId));

        Tournament tournament = tournamentRepository.findById(tournamentDto.getId())
                .orElseThrow(() -> new TutorException(TOURNAMENT_NOT_FOUND, tournamentDto.getId()));

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new TutorException(TOPIC_NOT_FOUND, topicId));

        if (tournament.getCreator() != user) {
            throw new TutorException(TOURNAMENT_CREATOR, user.getId());
        }

        tournament.addTopic(topic);
    }

    @Retryable(
    value = { SQLException.class },
    backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void removeTopic(Integer userId, TournamentDto tournamentDto, Integer topicId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TutorException(USER_NOT_FOUND, userId));

        Tournament tournament = tournamentRepository.findById(tournamentDto.getId())
                .orElseThrow(() -> new TutorException(TOURNAMENT_NOT_FOUND, tournamentDto.getId()));

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new TutorException(TOPIC_NOT_FOUND, topicId));

        if (tournament.getCreator() != user) {
            throw new TutorException(TOURNAMENT_CREATOR, user.getId());
        }

        tournament.removeTopic(topic);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void removeTournament(Integer userId, Integer tournamentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TutorException(USER_NOT_FOUND, userId));

        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TutorException(TOURNAMENT_NOT_FOUND, tournamentId));

        if (tournament.getCreator() != user) {
            throw new TutorException(TOURNAMENT_CREATOR, user.getId());
        }

        tournament.remove();
        tournamentRepository.delete(tournament);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<TournamentDto> getTournaments() {
        return tournamentRepository.findAll().stream().map(TournamentDto::new).collect(Collectors.toList());
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<TournamentDto> getOpenedTournaments() {
        return tournamentRepository.getOpenedTournaments().stream().map(TournamentDto::new).collect(Collectors.toList());
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<TournamentDto> getUserTournaments(User user) {
        return tournamentRepository.getUserTournaments(user.getId()).stream().map(TournamentDto::new).collect(Collectors.toList());
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void joinTournament(Integer userId, TournamentDto tournamentDto, String password) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TutorException(USER_NOT_FOUND, userId));

        Tournament tournament = tournamentRepository.findById(tournamentDto.getId())
                .orElseThrow(() -> new TutorException(TOURNAMENT_NOT_FOUND, tournamentDto.getId()));

        if (DateHandler.now().isAfter(tournament.getEndTime())) {
            throw new TutorException(TOURNAMENT_NOT_OPEN, tournament.getId());
        }

        if (tournament.getState() == Tournament.Status.CANCELED) {
            throw new TutorException(TOURNAMENT_CANCELED, tournament.getId());
        }

        if (user.getRole() != User.Role.STUDENT) {
            throw new TutorException(USER_NOT_STUDENT, user.getId());
        }

        if (tournament.getParticipants().contains(user)) {
            throw new TutorException(DUPLICATE_TOURNAMENT_PARTICIPANT, user.getUsername());
        }
        if (!user.getCourseExecutions().contains(tournament.getCourseExecution())) {
            throw new TutorException(STUDENT_NO_COURSE_EXECUTION, user.getId());
        }
        if (tournament.isPrivateTournament() && !password.equals(tournament.getPassword())) {
            throw new TutorException(WRONG_TOURNAMENT_PASSWORD, tournament.getId());
        }

        tournament.addParticipant(user);

        if (!tournament.hasQuiz()) {
            StatementCreationDto quizForm = new StatementCreationDto();
            quizForm.setNumberOfQuestions(tournament.getNumberOfQuestions());

            TopicConjunction topicConjunction = new TopicConjunction();
            List<Topic> topics = tournament.getTopics();
            for (Topic topic: topics) {
                topicConjunction.addTopic(topic);
            }
            topicConjunctionRepository.save(topicConjunction);

            Assessment assessment = new Assessment();
            assessment.setTitle("Generated Assessment");
            assessment.setStatus(Assessment.Status.AVAILABLE);
            assessment.setCourseExecution(tournament.getCourseExecution());
            assessment.addTopicConjunction(topicConjunction);
            topicConjunction.setAssessment(assessment);

            quizForm.setAssessment(assessment.getId());

            StatementQuizDto statementQuizDto = statementService.generateStudentQuiz(tournament.getCreator().getId(), tournament.getCourseExecution().getId(), quizForm);

            Quiz quiz = quizRepository.findById(statementQuizDto.getId())
                    .orElseThrow(() -> new TutorException(QUIZ_NOT_FOUND, statementQuizDto.getId()));

            if (DateHandler.now().isBefore(tournament.getStartTime())){
                quiz.setAvailableDate(tournament.getStartTime());
            }
            quiz.setConclusionDate(tournament.getEndTime());

            tournament.setQuizId(statementQuizDto.getId());
        }
    }



    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void leaveTournament(Integer userId, TournamentDto tournamentDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TutorException(USER_NOT_FOUND, userId));

        Tournament tournament = tournamentRepository.findById(tournamentDto.getId())
                .orElseThrow(() -> new TutorException(TOURNAMENT_NOT_FOUND, tournamentDto.getId()));

        if (!tournament.getParticipants().contains(user)) {
            throw new TutorException(USER_NOT_JOINED, user.getUsername());
        }

        tournament.removeParticipant(user);
        for (Notification notification: tournament.getNotifications()) {
            notificationService.removeNotification(user.getId(), notification.getId());
        }
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public StatementQuizDto solveQuiz(Integer userId, TournamentDto tournamentDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TutorException(USER_NOT_FOUND, userId));

        Tournament tournament = tournamentRepository.findById(tournamentDto.getId())
                .orElseThrow(() -> new TutorException(TOURNAMENT_NOT_FOUND, tournamentDto.getId()));

        if (!tournament.getParticipants().contains(user)) {
            throw  new TutorException(USER_NOT_JOINED, userId);
        }

        if (!tournament.hasQuiz()) {
            throw new TutorException(TOURNAMENT_NO_QUIZ, tournamentDto.getId());
        }

        return statementService.getQuizByQRCode(userId, tournament.getQuizId());
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void cancelTournament(Integer userId, TournamentDto tournamentDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TutorException(USER_NOT_FOUND, userId));

        Tournament tournament = tournamentRepository.findById(tournamentDto.getId())
                .orElseThrow(() -> new TutorException(TOURNAMENT_NOT_FOUND, tournamentDto.getId()));

        if (tournament.getCreator() != user) {
            throw new TutorException(TOURNAMENT_CREATOR, user.getId());
        }

        tournament.setState(Tournament.Status.CANCELED);

        String title = NotificationsCreation.createTitle(CANCEL_TITLE, tournament.getId());
        String content = NotificationsCreation.createContent(CANCEL_CONTENT, tournament.getId());
        tournament.Notify(createNotification(tournament, title, content));
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void editStartTime(Integer userId, TournamentDto tournamentDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TutorException(USER_NOT_FOUND, userId));

        Tournament tournament = tournamentRepository.findById(tournamentDto.getId())
                .orElseThrow(() -> new TutorException(TOURNAMENT_NOT_FOUND, tournamentDto.getId()));

        if (tournament.getCreator() != user) {
            throw new TutorException(TOURNAMENT_CREATOR, user.getId());
        }

        if (DateHandler.isValidDateFormat(tournamentDto.getStartTime())) {
            String oldTime = DateHandler.toString(tournament.getStartTime());
            tournament.setStartTime(DateHandler.toLocalDateTime(tournamentDto.getStartTime()));

            String title = NotificationsCreation.createTitle(EDIT_START_TIME_TITLE, tournament.getId());
            String content = NotificationsCreation.createContent(EDIT_START_TIME_CONTENT, tournament.getId(), oldTime, DateHandler.toString(tournament.getStartTime()));
            tournament.Notify(createNotification(tournament, title, content));
        }
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void editEndTime(Integer userId, TournamentDto tournamentDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TutorException(USER_NOT_FOUND, userId));

        Tournament tournament = tournamentRepository.findById(tournamentDto.getId())
                .orElseThrow(() -> new TutorException(TOURNAMENT_NOT_FOUND, tournamentDto.getId()));

        if (tournament.getCreator() != user) {
            throw new TutorException(TOURNAMENT_CREATOR, user.getId());
        }

        if (DateHandler.isValidDateFormat(tournamentDto.getEndTime())) {
            String oldTime = DateHandler.toString(tournament.getEndTime());
            tournament.setEndTime(DateHandler.toLocalDateTime(tournamentDto.getEndTime()));

            String title = NotificationsCreation.createTitle(EDIT_END_TIME_TITLE, tournament.getId());
            String content = NotificationsCreation.createContent(EDIT_END_TIME_CONTENT, tournament.getId(), oldTime, DateHandler.toString(tournament.getEndTime()));
            tournament.Notify(createNotification(tournament, title, content));
        }
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void editNumberOfQuestions(Integer userId, TournamentDto tournamentDto, Integer numberOfQuestions) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TutorException(USER_NOT_FOUND, userId));

        Tournament tournament = tournamentRepository.findById(tournamentDto.getId())
                .orElseThrow(() -> new TutorException(TOURNAMENT_NOT_FOUND, tournamentDto.getId()));

        if (tournament.getCreator() != user) {
            throw new TutorException(TOURNAMENT_CREATOR, user.getId());
        }

        Integer oldNumberOfQuestions = tournament.getNumberOfQuestions();
        tournament.setNumberOfQuestions(numberOfQuestions);

        String title = NotificationsCreation.createTitle(EDIT_NUMBER_OF_QUESTIONS_TITLE, tournament.getId());
        String content = NotificationsCreation.createContent(EDIT_NUMBER_OF_QUESTIONS_CONTENT, tournament.getId(), oldNumberOfQuestions, tournament.getNumberOfQuestions());
        tournament.Notify(createNotification(tournament, title, content));
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<UserDto> getTournamentParticipants(TournamentDto tournamentDto) {
        Tournament tournament = tournamentRepository.findById(tournamentDto.getId())
                .orElseThrow(() -> new TutorException(TOURNAMENT_NOT_FOUND, tournamentDto.getId()));

        return tournament.getParticipants().stream().map(UserDto::new).collect(Collectors.toList());

    }

    private void checkInput(Integer userId, TournamentDto tournamentDto) {
        if (userId == null) {
            throw new TutorException(USER_MISSING);
        }
        if (tournamentDto.getStartTime() == null) {
            throw new TutorException(TOURNAMENT_MISSING_START_TIME);
        }
        if (tournamentDto.getEndTime() == null) {
            throw new TutorException(TOURNAMENT_MISSING_END_TIME);
        }
        if (tournamentDto.getNumberOfQuestions() == null) {
            throw new TutorException(TOURNAMENT_MISSING_NUMBER_OF_QUESTIONS);
        }
    }

    public Notification createNotification(Tournament tournament, String title, String content) {
        NotificationsCreation notificationsCreation = new NotificationsCreation(title, content);
        NotificationDto response = notificationService.createNotification(notificationsCreation.getNotificationDto());

        Notification notification = notificationService.getNotificationById(response.getId());
        tournament.addNotification(notification);

        return notification;
    }
}
