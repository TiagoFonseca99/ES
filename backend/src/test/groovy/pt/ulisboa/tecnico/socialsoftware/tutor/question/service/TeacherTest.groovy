package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

import spock.lang.Specification

@DataJpaTest
class TeacherTest extends Specification {

    def setup() {

    }

    def "teacher accepts a submitted question"() {
        // an exception is thrown
        expect: false
    }

    def "teacher rejects a question without a justification"() {
        // an exception is thrown
        expect: false
    }

    def "teacher does not have question to accept"() {
        // an exception is thrown
        expect: false
    }
}