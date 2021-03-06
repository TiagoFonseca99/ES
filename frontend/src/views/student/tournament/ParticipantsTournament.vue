<template>
  <v-container
    fluid
    style="height: 100%; position: relative; display: flex; flex-direction: column"
    v-if="this.$store.getters.isLoggedIn"
  >
    <v-container fluid style="position: relative; max-height: 100%;">
      <v-row style="width: 100%; height: 100%">
        <v-col>
          <v-card class="dashCard flexCard">
            <v-card-title class="justify-center"
              >Tournament {{ this.id }} - Ranking</v-card-title
            >
            <v-data-table
              :headers="headers"
              :items="tournamentScores"
              :hide-default-footer="true"
              :mobile-breakpoint="0"
              class="fill-height"
              disable-sort
            >
              <template v-slot:item.ranking="{ item }">
                <v-chip
                  v-if="item.ranking < 4 && item.ranking != 0"
                  :color="getRankingColor(item.ranking)"
                  text-color="white"
                >
                  {{ item.ranking }}
                </v-chip>
                <span v-else-if="item.ranking != 0">
                  {{ item.ranking }}
                </span>
              </template>
              <template v-slot:item.username="{ item }">
                <v-chip
                  color="primary"
                  small
                  @click="openStudentDashboardDialog(item)"
                >
                  <v-icon left small v-if="isCurrentUser(item)" color="white"
                    >star</v-icon
                  >
                  <span> {{ item.username }} </span>
                </v-chip>
              </template>
              <template v-slot:item.score="{ item }">
                <v-chip :color="getPercentageColor(item.score)">
                  {{ item.score === '' ? 'NOT SOLVED' : item.score }}
                </v-chip>
              </template>
            </v-data-table>
          </v-card>
        </v-col>
      </v-row>
    </v-container>
    <show-dashboard-dialog
      v-if="currentUsername"
      v-model="dashboardDialog"
      :username="currentUsername"
      v-on:close-show-dashboard-dialog="onCloseShowDashboardDialog"
    />
  </v-container>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator';
import Tournament from '@/models/user/Tournament';
import SolvedQuiz from '@/models/statement/SolvedQuiz';
import TournamentScore from '@/models/user/TournamentScore';
import User from '@/models/user/User';
import RemoteServices from '@/services/RemoteServices';
import ShowDashboardDialog from '@/views/student/dashboard/DashboardDialogView.vue';

@Component({
  components: { 'show-dashboard-dialog': ShowDashboardDialog }
})
export default class ParticipantsTournament extends Vue {
  @Prop({ type: String, required: true }) id!: number;

  tournaments: Tournament[] = [];
  selectedTournament: Tournament | null = null;
  quizzes: SolvedQuiz[] = [];
  tournamentScores: TournamentScore[] = [];
  correctAnswers: number = 0;
  currentUsername: string | null = null;
  dashboardDialog: boolean = false;

  headers: object = [
    { text: 'Ranking', value: 'ranking', align: 'center' },
    { text: 'Name', value: 'name', align: 'center' },
    { text: 'Username', value: 'username', align: 'center' },
    { text: 'Score', value: 'score', align: 'center' }
  ];

  async created() {
    await this.$store.dispatch('loading');
    try {
      this.tournaments = await RemoteServices.getAllTournaments();
      this.tournaments.map(tournament => {
        if (tournament.id == this.id) this.selectedTournament = tournament;
      });
      if (this.selectedTournament)
        for (const participant of this.selectedTournament.participants) {
          const score = await this.getScore(participant);
          this.tournamentScores.push(
            new TournamentScore(participant, score, this.correctAnswers)
          );
        }
      this.tournamentScores.sort((a, b) => this.sortByScore(a, b));
      this.setRankings();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  sortByScore(a: TournamentScore, b: TournamentScore) {
    if (a && b) {
      return a.score < b.score ? 1 : -1;
    } else return 0;
  }

  setRankings() {
    for (let i = 1; i <= this.tournamentScores.length; i++) {
      this.tournamentScores[i - 1].ranking = i;
    }
  }
  calculateScore(quiz: SolvedQuiz) {
    let correct = 0;
    for (let i = 0; i < quiz.statementQuiz.questions.length; i++) {
      if (
        quiz.statementQuiz.answers[i] &&
        quiz.correctAnswers[i].correctOptionId ===
          quiz.statementQuiz.answers[i].optionId
      ) {
        correct += 1;
      }
    }
    this.correctAnswers = correct;
    return `${correct}/${quiz.statementQuiz.questions.length}`;
  }

  async getScore(participant: User) {
    this.quizzes = await RemoteServices.getSolvedQuizzes(participant.username);

    let score = '';
    this.quizzes.map(quiz => {
      if (
        this.selectedTournament &&
        quiz.statementQuiz.id == this.selectedTournament.quizId
      ) {
        score = this.calculateScore(quiz);
      }
    });
    return score;
  }

  getPercentageColor(score: string) {
    let res = score.split('/');
    let percentage = (parseInt(res[0]) / parseInt(res[1])) * 100;
    if (percentage < 25) return 'red';
    else if (percentage < 50) return 'orange';
    else if (percentage < 75) return 'lime';
    else if (percentage <= 100) return 'green';
  }

  getRankingColor(ranking: number) {
    if (ranking == 1) return '#d4Af37';
    else if (ranking == 2) return '#c0c0c0';
    else if (ranking == 3) return '#b9722d';
    else return 'primary';
  }

  isCurrentUser(a: TournamentScore) {
    return a.name === this.$store.getters.getUser.name;
  }

  openStudentDashboardDialog(tournamentScore: TournamentScore) {
    this.dashboardDialog = true;
    this.currentUsername = tournamentScore.username;
  }

  onCloseShowDashboardDialog() {
    this.dashboardDialog = false;
  }
}
</script>

<style lang="scss" scoped>
@mixin background-opacity($color, $opacity: 1) {
  $red: red($color);
  $green: green($color);
  $blue: blue($color);
  background: rgba($red, $green, $blue, $opacity) !important;
}

.flexCard {
  display: flex;
  flex-direction: column;
}

.dashCard {
  height: 100%;
  @include background-opacity(#fff, 0.6);
}
</style>
