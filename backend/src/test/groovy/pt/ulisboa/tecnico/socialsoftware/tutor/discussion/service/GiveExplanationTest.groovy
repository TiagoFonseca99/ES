package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.DiscussionService
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import spock.lang.Specification

@DataJpaTest
class GiveExplanationTest extends Specification {
    def "explanation not null"(){
        expect: false
    }

    def "discussion not null"(){
        expect: false
    }

    def "discussion exists"(){
        expect: false
    }

    def "teacher enrolled in discussion's course"(){
        expect: false
    }

}