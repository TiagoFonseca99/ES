package pt.ulisboa.tecnico.socialsoftware.tutor.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import pt.ulisboa.tecnico.socialsoftware.tutor.announcement.AnnouncementService;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.DiscussionService;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.ImpExpService;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.AssessmentService;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.TopicService;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService;
import pt.ulisboa.tecnico.socialsoftware.tutor.statement.StatementService;
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.SubmissionService;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserService;
import pt.ulisboa.tecnico.socialsoftware.tutor.worker.WorkerService;

@Component
public class ScheduledTasks {
	@Autowired
	private ImpExpService impExpService;

    @Autowired
    private QuizService quizService;

	@Autowired
	private StatementService statementService;

    @Autowired
    private UserService userService;

    @Autowired
    private TopicService topicService;

    @Autowired
    private AssessmentService assessmentService;

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private DiscussionService discussionService;

    @Autowired
    private WorkerService workerService;

	@Scheduled(cron = "0 0 1,13 * * *")
	public void exportAll() {
		impExpService.exportAll();
	}

	@Scheduled(cron = "0 0/15 * * * *")
	public void completeOpenQuizAnswers() {
		statementService.completeOpenQuizAnswers();
	}

    @Scheduled(cron = "0 0 1 * * *")
    public void resetDemoInfo() {
        userService.resetDemoStudent();
	    quizService.resetDemoQuizzes();
        topicService.resetDemoTopics();
        assessmentService.resetDemoAssessments();
        announcementService.resetDemoAnnouncements();
        tournamentService.resetDemoTournaments();
        submissionService.resetDemoSubmissions();
        discussionService.resetDemoDiscussions();
        workerService.resetDemoSubscriptions();
    }
}
