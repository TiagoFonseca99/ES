package pt.ulisboa.tecnico.socialsoftware.tutor.submission;

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import javax.persistence.*;

@Entity
@Table(name = "submissions")
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    public Submission() {}
}
