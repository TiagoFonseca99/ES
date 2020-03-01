package pt.ulisboa.tecnico.socialsoftware.tutor.submission.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.SubmissionService
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import spock.lang.Specification

@DataJpaTest
class CreateSubmissionTest extends Specification {
    def "create submission"(){
        expect: false
    }

    def "create submission with question not null"(){
        expect: false
    }

    def "student that submits a question exists"(){
        expect: false
    }

    def "student that submits a question enrolled in course"(){
        expect: false
    }

    def "topic exists and not null"(){
        expect: false
    }

}