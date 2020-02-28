package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto

import spock.lang.Specification

class CreateTournamentTest extends Specification {
    static final String START_TIME = "2020-03-03 10:00"
    static final String END_TIME = "2020-03-03 12:00"
    static final String TOPIC_NAME = "Informática"
    static final int NUMBER_QUESTIONS = 5

    def tournamentService
    def topic

    def setup() {
        tournamentService = new TournamentService()
        topic = findTopicByName( , )
    }

    def "create tournament" () {
        given:
        when:
        def result = tournamentService.createNewTournament(tournamentDto)
        then:
        // and exception is thrown
        expect: true
    }

    def "start time is higher then end time" () {
        given:
        when:
        then:
        // and exception is thrown
        expect: false
    }

    def "topic not exists" () {
        given:
        when:
        then:
        // and exception is thrown
        expect: false
    }

    def "number of questions is lower or zero" () {
        given:
        when:
        then:
        // and exception is thrown
        expect: false
    }

    def "start time is empty" () {
        given:
        when:
        then:
        // and exception is thrown
        expect: false
    }

    def "end time is empty" () {
        given:
        when:
        then:
        // and exception is thrown
        expect: false
    }

    def "topic is empty" () {
        given:
        when:
        then:
        // and exception is thrown
        expect: false
    }

    def "number of questions is empty" () {
        given:
        when:
        then:
        // and exception is thrown
        expect: false
    }

    def "invalid arguments" () {
        given:
        when:
        then:
        // and exception is thrown
        expect: false
    }
}
