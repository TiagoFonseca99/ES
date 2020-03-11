package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import spock.lang.Specification

@DataJpaTest
class GetTeacherReplyTest extends Specification {
    def "get reply of submitted discussion"(){
        expected: true
    }

    def "get reply of submitted discussion without response"(){
        expected: true
    }

    def "get reply of non-submitted discussion"(){
        expected: true
    }

    def "get reply of answered question without discussion"(){
        expected: true
    }
}
