package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import spock.lang.Specification

class CreateTournamentTest extends Specification {

    def tournamentService

    def setup() {
        tournamentService = new TournamentService()
    }

    def "create tournament" () {
        // and exception is thrown
        expect: true
    }

    def "start time is higher then end time" () {
        // and exception is thrown
        expect: false
    }

    def "topic not exists" () {
        // and exception is thrown
        expect: false
    }

    def "number of questions is lower or zero" () {
        // and exception is thrown
        expect: false
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
