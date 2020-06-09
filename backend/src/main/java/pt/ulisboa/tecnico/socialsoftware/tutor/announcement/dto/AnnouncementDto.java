package pt.ulisboa.tecnico.socialsoftware.tutor.announcement.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.announcement.domain.Announcement;
import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler;

public class AnnouncementDto {
    private Integer id;
    private Integer userId;
    private Integer courseExecutionId;
    private String title;
    private String content;
    private String creationDate;

    public AnnouncementDto(){}

    public AnnouncementDto(Announcement announcement) {
        this.id = announcement.getId();
        this.userId = announcement.getUser().getId();
        this.courseExecutionId = announcement.getCourseExecution().getId();
        this.title = announcement.getTitle();
        this.content = announcement.getContent();
        this.creationDate = DateHandler.toISOString(announcement.getCreationDate());
    }

    public Integer getId() { return id; }

    public void setId(Integer id) { this.id = id; }

    public Integer getUserId() { return userId; }

    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getCourseExecutionId() { return courseExecutionId; }

    public void setCourseExecutionId(Integer courseExecutionId) { this.courseExecutionId = courseExecutionId; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }

    public void setContent(String content) {
        this.content = content; }

    public String getCreationDate() { return creationDate; }

    public void setCreationDate(String creationDate) { this.creationDate = creationDate; }
}
