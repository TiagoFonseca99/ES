package pt.ulisboa.tecnico.socialsoftware.tutor.submission.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "submissions")
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(cascade = CascadeType.ALL, fetch=FetchType.LAZY, orphanRemoval=true)
    private Question question;

    @Column(columnDefinition = "TEXT")
    private String argument;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(columnDefinition = "boolean default false", nullable = false)
    private boolean anonymous;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "submission", fetch=FetchType.LAZY, orphanRemoval=true)
    private Set<Review> reviews = new HashSet<>();

    public Submission() {}

    public Submission(Question question, User user){
        this.question = question;
        this.user = user;
        user.addSubmission(this);
    }

    public Integer getId() { return id; }

    public void setId(Integer id) { this.id = id; }

    public Question getQuestion() { return question; }

    public void setQuestion(Question question) { this.question = question; }

    public User getUser() { return user; }

    public void setUser(User user) { this.user = user; }

    public int getStudentId() { return this.user.getId(); }

    public boolean isAnonymous() { return anonymous; }

    public void setAnonymous(boolean anonymous) { this.anonymous = anonymous; }

    public String getArgument() { return argument; }

    public void setArgument(String argument) { this.argument = argument; }

    @Override
    public String toString() {
        return "Submission{" +
                "id=" + id +
                ", question=" + question +
                ", user='" + user +
                '}';
    }

}
