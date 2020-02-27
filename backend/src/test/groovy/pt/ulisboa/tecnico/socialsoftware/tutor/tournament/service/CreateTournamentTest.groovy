package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import spock.lang.Specification

class CreateTournamentTest extends Specification {

    def tournamentService

    def setup() {
        tournamentService = new TournamentService()
    }

    def "start time is lower then end time" () {
        // and exception is thrown
        expect: true
    }

    def "topic exists" () {
        // and exception is thrown
        expect: true
    }

    def "number of questions is higher then zero" () {
        // and exception is thrown
        expect: true
    }

    def "start time is empty" () {
        // and exception is thrown
        expect: false
    }

    def "end time is empty" () {
        // and exception is thrown
        expect: false
    }

    def "topic is empty" () {
        // and exception is thrown
        expect: false
    }

    def "number of questions is empty" () {
        // and exception is thrown
        expect: false
    }
}
