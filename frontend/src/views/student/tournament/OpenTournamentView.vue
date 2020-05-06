<template>
  <v-card class="table">
    <v-data-table
      :headers="headers"
      :items="tournaments"
      :search="search"
      disable-pagination
      :hide-default-footer="true"
      :mobile-breakpoint="0"
      multi-sort
    >
      <template v-slot:top>
        <v-card-title>
          <v-text-field
            v-model="search"
            append-icon="search"
            label="Search"
            class="mx-2"
          />
          <v-spacer />
          <v-btn to="/student/all" color="primary" dark data-cy="createButton"
            >See All Tournaments
          </v-btn>
          <v-btn
            color="primary"
            dark
            @click="newTournament"
            data-cy="createButton"
            >New Tournament
          </v-btn>
        </v-card-title>
      </template>
      <template v-slot:item.state="{ item }">
        <v-chip :color="getStateColor(item.state)">
          {{ getStateName(item.state) }}
        </v-chip>
      </template>
      <template v-slot:item.enrolled="{ item }">
        <v-chip :color="getEnrolledColor(item.enrolled)">
          {{ getEnrolledName(item.enrolled) }}
        </v-chip>
      </template>
      <template v-slot:item.action="{ item }">
        <v-tooltip bottom v-if="isNotEnrolled(item)">
          <template v-slot:activator="{ on }">
            <v-icon
              small
              class="mr-2"
              v-on="on"
              @click="joinTournament(item)"
              data-cy="JoinTournament"
              >fas fa-sign-in-alt</v-icon
            >
          </template>
          <span>Join Tournament</span>
        </v-tooltip>
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon
              small
              class="mr-2"
              v-on="on"
              @click="leaveTournament(item)"
              data-cy="LeaveTournament"
              >fas fa-sign-out-alt</v-icon
            >
          </template>
          <span>Leave Tournament</span>
        </v-tooltip>
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon
              small
              class="mr-2"
              v-on="on"
              @click="solveQuiz(item)"
              data-cy="SolveQuiz"
              >fas fa-pencil-alt</v-icon
            >
          </template>
          <span>Solve Quiz</span>
        </v-tooltip>
      </template>
    </v-data-table>

    <edit-tournament-dialog
      v-if="currentTournament"
      v-model="createTournamentDialog"
      :tournament="currentTournament"
      v-on:new-tournament="onCreateTournament"
      v-on:close-dialog="onCloseDialog"
    />
  </v-card>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import Tournament from '@/models/user/Tournament';
import RemoteServices from '@/services/RemoteServices';
import StatementQuiz from '@/models/statement/StatementQuiz';
import StatementManager from '@/models/statement/StatementManager';
import CreateTournamentDialog from '@/views/student/tournament/CreateTournamentView.vue';

@Component({
  components: {
    'edit-tournament-dialog': CreateTournamentDialog
  }
})
export default class OpenTournamentView extends Vue {
  tournaments: Tournament[] = [];
  currentTournament: Tournament | null = null;
  createTournamentDialog: boolean = false;
  search: string = '';
  headers: object = [
    {
      text: 'Course Acronym',
      value: 'courseAcronym',
      align: 'center',
      width: '10%'
    },
    { text: 'Tournament Number', value: 'id', align: 'center', width: '10%' },
    {
      text: 'Topics',
      value: 'topics',
      align: 'center',
      width: '10%'
    },
    {
      text: 'State',
      value: 'state',
      align: 'center',
      width: '10%'
    },
    {
      text: 'Start Time',
      value: 'startTime',
      align: 'center',
      width: '10%'
    },
    {
      text: 'End Time',
      value: 'endTime',
      align: 'center',
      width: '10%'
    },
    {
      text: 'Number of Questions',
      value: 'numberOfQuestions',
      align: 'center',
      width: '10%'
    },
    {
      text: 'Enrolled',
      value: 'enrolled',
      align: 'center',
      sortable: false,
      width: '10%'
    },
    {
      text: 'Actions',
      value: 'action',
      align: 'center',
      sortable: false,
      width: '10%'
    }
  ];

  async created() {
    await this.$store.dispatch('loading');
    try {
      this.tournaments = await RemoteServices.getOpenTournaments();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  newTournament() {
    this.currentTournament = new Tournament();
    this.createTournamentDialog = true;
  }

  async onCreateTournament(tournament: Tournament) {
    this.tournaments.unshift(tournament);
    this.createTournamentDialog = false;
    this.currentTournament = null;
  }

  onCloseDialog() {
    this.createTournamentDialog = false;
    this.currentTournament = null;
  }

  getStateColor(state: string) {
    if (state === 'NOT_CANCELED') return 'green';
    else return 'red';
  }

  getStateName(state: string) {
    if (state === 'NOT_CANCELED') return 'NOT CANCELED';
    else return 'CANCELED';
  }

  getEnrolledColor(enrolled: string) {
    if (enrolled) return 'green';
    else return 'red';
  }

  getEnrolledName(enrolled: string) {
    if (enrolled) return 'YOU ARE IN';
    else return 'YOU NEED TO JOIN';
  }

  isNotEnrolled(tournamentToJoin: Tournament) {
    return !tournamentToJoin.enrolled;
  }

  async joinTournament(tournamentToJoin: Tournament) {
    const enrolled = tournamentToJoin.enrolled;
    const topics = tournamentToJoin.topics;
    tournamentToJoin.enrolled = undefined;
    tournamentToJoin.topics = [];
    try {
      await RemoteServices.joinTournament(tournamentToJoin);
    } catch (error) {
      await this.$store.dispatch('error', error);
      tournamentToJoin.enrolled = enrolled;
      tournamentToJoin.topics = topics;
      return;
    }
    tournamentToJoin.enrolled = true;
    tournamentToJoin.topics = topics;
  }

  async leaveTournament(tournamentToJoin: Tournament) {
    const enrolled = tournamentToJoin.enrolled;
    const topics = tournamentToJoin.topics;
    tournamentToJoin.enrolled = undefined;
    tournamentToJoin.topics = [];
    try {
      await RemoteServices.leaveTournament(tournamentToJoin);
    } catch (error) {
      await this.$store.dispatch('error', error);
      tournamentToJoin.enrolled = enrolled;
      tournamentToJoin.topics = topics;
      return;
    }
    tournamentToJoin.enrolled = false;
    tournamentToJoin.topics = topics;
  }

  async solveQuiz(tournament: Tournament) {
    const enrolled = tournament.enrolled;
    const topics = tournament.topics;
    tournament.enrolled = undefined;
    tournament.topics = [];
    try {
      let quiz: StatementQuiz = await RemoteServices.solveTournament(
        tournament
      );
      let statementManager: StatementManager = StatementManager.getInstance;
      statementManager.statementQuiz = quiz;
      await this.$router.push({ name: 'solve-quiz' });
      return;
    } catch (error) {
      await this.$store.dispatch('error', error);
      tournament.enrolled = enrolled;
      tournament.topics = topics;
    }
    tournament.enrolled = enrolled;
    tournament.topics = topics;
  }
}
</script>

<style lang="scss" scoped></style>
