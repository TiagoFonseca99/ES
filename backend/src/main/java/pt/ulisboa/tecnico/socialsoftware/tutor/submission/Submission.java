package pt.ulisboa.tecnico.socialsoftware.tutor.submission;

import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;

import javax.persistence.*;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.COURSE_NAME_IS_EMPTY;

@Entity
@Table(name = "submissions")
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique=true, nullable = false)
    private Integer key;

    @OneToOne(cascade = CascadeType.ALL, fetch=FetchType.LAZY, orphanRemoval=true)
    private Question question;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    //TODO add submission time

    public Submission() {}

    public Submission(Question question, User user, SubmissionDto submissionDto){
        this.id = submissionDto.getId();
        this.key = submissionDto.getKey();
        this.question = question;
        this.user = user;
        user.addSubmission(this);
    }

    public Integer getId() { return id; }

    public void setId(Integer id) { this.id = id; }

    public Integer getKey() { return key; }

    public void setKey(Integer key) { this.key = key; }

    public Question getQuestion() { return question; }

    public void setQuestion(Question question) { this.question = question; }

    public User getUser() { return user; }

    public void setUser(User user) { this.user = user; }

    public int getStudentId() { return this.user.getId(); }
}


