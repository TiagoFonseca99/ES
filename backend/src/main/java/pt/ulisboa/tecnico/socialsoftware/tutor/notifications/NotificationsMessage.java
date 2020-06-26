package pt.ulisboa.tecnico.socialsoftware.tutor.notifications;

public enum NotificationsMessage {

    // tdp

    EDIT_START_TIME_TITLE("New start time - Tournament %s"),
    EDIT_END_TIME_TITLE("New end time - Tournament %s"),
    EDIT_NUMBER_OF_QUESTIONS_TITLE("New number of questions - Tournament %s"),
    ADD_TOPIC_TITLE("Topic added - Tournament %s"),
    REMOVE_TOPIC_TITLE("Topic removed - Tournament %s"),
    CANCEL_TITLE("Tournament %s Canceled"),

    EDIT_START_TIME_CONTENT("Tournament %s start time changed from %s to %s"),
    EDIT_END_TIME_CONTENT("Tournament %s end time changed from %s to %s"),
    EDIT_NUMBER_OF_QUESTIONS_CONTENT("Tournament %s number of questions changed from %s to %s"),
    ADD_TOPIC_CONTENT("Topic '%s' added to tournament %s"),
    REMOVE_TOPIC_CONTENT("Topic '%s' removed from tournament %s"),
    CANCEL_CONTENT("Tournament %s canceled and no longer accessible"),

    // ddp

    DISCUSSION_CREATE("New Discussion - Question '%s'"),
    DISCUSSION_EDIT("Discussion edited - Question '%s'"),
    DISCUSSION_DELETE("Discussion removed - Question '%s'"),
    DISCUSSION_AVAILABILITY("Change availability - Discussion to Question '%s'"),
    DISCUSSION_REPLY("New Reply - Discussion to Question '%s'"),
    DISCUSSION_EDIT_REPLY("Reply edited - Discussion to Question '%s'"),
    DISCUSSION_DELETE_REPLY("Reply removed - Discussion to Question '%s'"),

    DISCUSSION_CREATE_CONTENT("%s created discussion to question '%s'"),
    DISCUSSION_EDIT_CONTENT("%s edited discussion to question '%s'"),
    DISCUSSION_DELETE_CONTENT("%s removed discussion to question '%s'"),
    DISCUSSION_AVAILABILITY_CONTENT("%s set discussion to question '%s' to %s"),
    DISCUSSION_REPLY_CONTENT("%s replied to discussion to question '%s'"),
    DISCUSSION_EDIT_REPLY_CONTENT("%s edited reply to discussion to question '%s'"),
    DISCUSSION_DELETE_REPLY_CONTENT("%s removed reply to discussion to question '%s'"),

    // ppa

    NEW_REVIEW_TITLE("New Review - Submission '%s'"),
    NEW_REVIEW_CONTENT("Submission '%s' %s by teacher %s"),

    DELETED_QUESTION_TITLE("Deleted Question - Question '%s'"),
    DELETED_QUESTION_CONTENT("Question '%s' deleted by teacher %s"),

    NEW_SUBMISSION_TITLE("New Submission - Question '%s'"),
    NEW_SUBMISSION_CONTENT("Submission created by student %s");

    public final String label;

    NotificationsMessage(String label) {
        this.label = label;
    }

    public String getMessage() { return label; }
}


