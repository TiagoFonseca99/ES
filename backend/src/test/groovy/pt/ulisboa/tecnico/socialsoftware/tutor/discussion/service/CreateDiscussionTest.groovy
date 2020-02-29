package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import spock.lang.Specification

@DataJpaTest
class CreateDiscussionTest extends Specification {

    }

    def "create discussion"(){
        expect: false
    }

    def "ensure user is student"(){
        expect: false
    }

    def "student answered the question in discussion"(){
        expect: false
    }

    def "student can't create 2 discussions to same question"(){
        expect: false
    }
}
