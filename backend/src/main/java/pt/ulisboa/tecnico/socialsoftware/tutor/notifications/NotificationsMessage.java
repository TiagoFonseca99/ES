package pt.ulisboa.tecnico.socialsoftware.tutor.notifications;

public enum NotificationsMessage {

    // tdp

    EDIT_START_TIME_TITLE("New start time - Tournament %s"),
    EDIT_END_TIME_TITLE("New end time - Tournament %s"),
    EDIT_NUMBER_OF_QUESTIONS_TITLE("New number of questions - Tournament %s"),
    ADD_TOPIC_TITLE("Topic added - Tournament %s"),
    REMOVE_TOPIC_TITLE("Topic removed - Tournament %s"),
    CANCEL_TITLE("Tournament %s Canceled"),

    EDIT_START_TIME_CONTENT("Tournament %s start time has been changed from %s to %s"),
    EDIT_END_TIME_CONTENT("Tournament %s end time has been changed from %s to %s"),
    EDIT_NUMBER_OF_QUESTIONS_CONTENT("Tournament %s number of questions has been changed from %s to %s"),
    ADD_TOPIC_CONTENT("Topic %s has been added to the tournament %s"),
    REMOVE_TOPIC_CONTENT("Topic %s has been removed from the tournament %s"),
    CANCEL_CONTENT("Tournament %s has been canceled"),

    // adp

    ADD_ANNOUNCEMENT_TITLE("New announcement - %s"),
    ADD_ANNOUNCEMENT_CONTENT("'%s' has been added by %s");

    public final String label;

    NotificationsMessage(String label) {
        this.label = label;
    }

    public String getMessage() { return label; }
}


