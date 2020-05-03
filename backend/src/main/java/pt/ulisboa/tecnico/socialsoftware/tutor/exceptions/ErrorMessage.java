package pt.ulisboa.tecnico.socialsoftware.tutor.exceptions;

public enum ErrorMessage {

    INVALID_ACADEMIC_TERM_FOR_COURSE_EXECUTION("Invalid academic term for course execution"),
    INVALID_ACRONYM_FOR_COURSE_EXECUTION("Invalid acronym for course execution"),
    INVALID_CONTENT_FOR_OPTION("Invalid content for option"),
    INVALID_CONTENT_FOR_QUESTION("Invalid content for question"),
    INVALID_NAME_FOR_COURSE("Invalid name for course"),
    INVALID_NAME_FOR_TOPIC("Invalid name for topic"),
    INVALID_SEQUENCE_FOR_OPTION("Invalid sequence for option"),
    INVALID_SEQUENCE_FOR_QUESTION_ANSWER("Invalid sequence for question answer"),
    INVALID_TITLE_FOR_ASSESSMENT("Invalid title for assessment"),
    INVALID_TITLE_FOR_QUESTION("Invalid title for question"),
    INVALID_URL_FOR_IMAGE("Invalid url for image"),
    INVALID_TYPE_FOR_COURSE("Invalid type for course"),
    INVALID_TYPE_FOR_COURSE_EXECUTION("Invalid type for course execution"),
    INVALID_AVAILABLE_DATE_FOR_QUIZ("Invalid available date for quiz"),
    INVALID_CONCLUSION_DATE_FOR_QUIZ("Invalid conclusion date for quiz"),
    INVALID_RESULTS_DATE_FOR_QUIZ("Invalid results date for quiz"),
    INVALID_TITLE_FOR_QUIZ("Invalid title for quiz"),
    INVALID_TYPE_FOR_QUIZ("Invalid type for quiz"),
    INVALID_QUESTION_SEQUENCE_FOR_QUIZ("Invalid question sequence for quiz"),

    COURSE_EXECUTION_NOT_FOUND("Course execution not found with id %d"),
    OPTION_NOT_FOUND("Option not found with id %d"),
    QUESTION_ANSWER_NOT_FOUND("Question answer not found with id %d"),
    QUESTION_NOT_FOUND("Question not found with id %d"),
    QUESTION_NOT_ANSWERED("Question not answered with id %d"),
    USER_NOT_FOUND("User not found with id %d"),
    ASSESSMENT_NOT_FOUND("Assessment not found with id %d"),
    TOPIC_CONJUNCTION_NOT_FOUND("Topic Conjunction not found with id %d"),

    QUIZ_ANSWER_NOT_FOUND("Quiz answer not found with id %d"),
    QUIZ_NOT_FOUND("Quiz not found with id %d"),
    QUIZ_QUESTION_NOT_FOUND("Quiz question not found with id %d"),
    TOPIC_NOT_FOUND("Topic not found with id %d"),
    DISCUSSION_NOT_FOUND("Discussion not found with user id %d and question id %d"),
    SUBMISSION_NOT_FOUND("Submission not found with id %d"),

    REVIEW_MISSING_JUSTIFICATION("Missing justification for review"),
    REVIEW_MISSING_SUBMISSION("Missing submission for review"),
    REVIEW_MISSING_STUDENT("Missing student for review"),
    REVIEW_MISSING_STATUS("Missing student for review"),

    SUBMISSION_MISSING_QUESTION("Missing question for submission"),
    SUBMISSION_MISSING_STUDENT("Missing student for submission"),
    REPLY_NOT_FOUND("Reply not found with teacher %d"),
    USER_MISSING("Missing student"),

    TOURNAMENT_MISSING_START_TIME("Missing start time for tournament"),
    TOURNAMENT_MISSING_END_TIME("Missing end time for tournament"),
    TOURNAMENT_MISSING_NUMBER_OF_QUESTIONS("Missing number of questions for tournament"),
    COURSE_NOT_FOUND("Course not found with name %s"),

    CANNOT_DELETE_COURSE_EXECUTION("The course execution cannot be deleted %s"),
    USERNAME_NOT_FOUND("Username %d not found"),
    TOURNAMENT_NOT_FOUND("Tournament %s not found"),
    USER_NOT_STUDENT("Username %s is not a student"),
    USER_NOT_TEACHER("Username %s is not a teacher"),
    QUESTION_ALREADY_SUBMITTED("User %s has already submitted this question"),
    QUESTION_ALREADY_APPROVED("User %s has already approved this question"),

    QUIZ_USER_MISMATCH("Quiz %s is not assigned to student %s"),
    QUIZ_MISMATCH("Quiz Answer Quiz %d does not match Quiz Question Quiz %d"),
    QUESTION_OPTION_MISMATCH("Question %d does not have option %d"),
    COURSE_EXECUTION_MISMATCH("Course Execution %d does not have quiz %d"),
    TOURNAMENT_TOPIC_MISMATCH("Tournament %s does not have topic %d"),
    TOURNAMENT_CREATOR("User %s can not cancel this tournament"),

    DUPLICATE_TOPIC("Duplicate topic: %s"),
    DUPLICATE_USER("Duplicate user: %s"),
    DUPLICATE_COURSE_EXECUTION("Duplicate course execution: %s"),
    DUPLICATE_DISCUSSION("Duplicate discussion for user id %d and question id %d"),
    DUPLICATE_TOURNAMENT_TOPIC("Duplicate tournament topic: %s"),
    DUPLICATE_TOURNAMENT_PARTICIPANT("Duplicate tournament participant: %s"),
    TOURNAMENT_NOT_OPEN("Tournament not open: %s"),
    TOURNAMENT_CANCELED("Tournament canceled: %s"),
    STUDENT_NO_COURSE_EXECUTION("Student has no matching course execution : %s"),
    TOURNAMENT_NO_QUIZ("Tournament has no quiz: %s"),
    USER_NOT_JOINED("User has not joined tournament: %s"),

    USERS_IMPORT_ERROR("Error importing users: %s"),
    QUESTIONS_IMPORT_ERROR("Error importing questions: %s"),
    TOPICS_IMPORT_ERROR("Error importing topics: %s"),
    ANSWERS_IMPORT_ERROR("Error importing answers: %s"),
    QUIZZES_IMPORT_ERROR("Error importing quizzes: %s"),

    QUESTION_IS_USED_IN_QUIZ("Question is used in quiz %s"),
    QUIZ_NOT_CONSISTENT("Field %s of quiz is not consistent"),
    TOURNAMENT_NOT_CONSISTENT("Field %s of tournament is not consistent"),
    USER_NOT_ENROLLED("%s - Not enrolled in any available course"),
    QUIZ_NO_LONGER_AVAILABLE("This quiz is no longer available"),
    QUIZ_NOT_YET_AVAILABLE("This quiz is not yet available"),
    TOURNAMENT_HAS_ONLY_ONE_TOPIC("This tournament has only one topic left"),
    TOURNAMENT_TOPIC_COURSE("Tournament topics must be of the same course execution"),
    DISCUSSION_NOT_SUBMITTED_BY_REQUESTER("Discussion was not created by user with id %d"),

    NO_CORRECT_OPTION("Question does not have a correct option"),
    NOT_ENOUGH_QUESTIONS("Not enough questions to create a quiz"),
    ONE_CORRECT_OPTION_NEEDED("Questions need to have 1 and only 1 correct option"),
    CANNOT_CHANGE_ANSWERED_QUESTION("Can not change answered question"),
    QUIZ_HAS_ANSWERS("Quiz already has answers"),
    QUIZ_ALREADY_COMPLETED("Quiz already completed"),
    QUIZ_ALREADY_STARTED("Quiz was already started"),
    QUIZ_QUESTION_HAS_ANSWERS("Quiz question has answers"),
    DISCUSSION_MISSING_DATA("Missing information for discussion"),
    DISCUSSION_NOT_STUDENT_CREATOR("Teacher cannot create discussion"),
    FENIX_ERROR("Fenix Error"),
    AUTHENTICATION_ERROR("Authentication Error"),
    FENIX_CONFIGURATION_ERROR("Incorrect server configuration files for fenix"),

    REPLY_MISSING_DATA("Missing information for reply"),
    REPLY_NOT_TEACHER_CREATOR("Student cannot give reply"),
    DUPLICATE_REPLY("Duplicate Reply for teacher: %d"),

    ACCESS_DENIED("You do not have permission to view this resource"),
    CANNOT_OPEN_FILE("Cannot open file");

    public final String label;

    ErrorMessage(String label) {
        this.label = label;
    }
}
