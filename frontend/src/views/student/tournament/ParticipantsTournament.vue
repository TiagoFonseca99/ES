<template>
  <v-container
    fluid
    style="height: 100%; position: relative; display: flex; flex-direction: column"
  >
    <h2>Tournament</h2>
    <v-container fluid style="position: relative; max-height: 100%; flex: 1;">
      <v-row style="width: 100%; height: 100%">
        <v-col>
          <v-card class="dashCard flexCard">
            <v-card-title class="justify-center">Ranking</v-card-title>
            <v-data-table
              :headers="headers"
              :items="tournamentScores"
              :hide-default-footer="true"
              :mobile-breakpoint="0"
              class="fill-height"
            >
              <template v-slot:item.score="{ item }">
                <v-chip :color="getPercentageColor(item.score)">
                  {{ item.score }}
                </v-chip>
              </template>
            </v-data-table>
          </v-card>
        </v-col>
      </v-row>
    </v-container>
  </v-container>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator';
import Tournament from '@/models/user/Tournament';
import SolvedQuiz from '@/models/statement/SolvedQuiz';
import TournamentScore from '@/models/user/TournamentScore';
import User from '@/models/user/User';
import RemoteServices from '@/services/RemoteServices';
import AnimatedNumber from '@/components/AnimatedNumber.vue';

@Component({
  components: { AnimatedNumber }
})
export default class ParticipantsTournament extends Vue {
  @Prop({ type: String, required: true }) id!: number;

  tournaments: Tournament[] = [];
  selectedTournament: Tournament | null = null;
  quizzes: SolvedQuiz[] = [];
  tournamentScores: TournamentScore[] = [];
  correctAnswers: number = 0;

  headers: object = [
    { text: 'Ranking', value: 'ranking', align: 'center' },
    { text: 'Name', value: 'name', align: 'center' },
    { text: 'Username', value: 'username', align: 'center' },
    { text: 'Score', value: 'score', align: 'center' }
  ];

  async created() {
    await this.$store.dispatch('loading');
    try {
      this.tournaments = await RemoteServices.getTournaments();
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
    if (a && b) return a.correctAnswers < b.correctAnswers ? 1 : -1;
    else return 0;
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

    if (score == '') return 'NOT SOLVED';
    return score;
  }

  getPercentageColor(score: string) {
    let res = score.split('/');
    let percentage = (parseInt(res[0]) / parseInt(res[1])) * 100;
    if (percentage < 25) return 'red';
    else if (percentage < 50) return 'orange';
    else if (percentage < 75) return 'lime';
    else if (percentage <= 100) return 'green';
    else return 'primary';
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

  .dashInfo {
    display: flex;
    flex-direction: column;
    justify-content: space-around;
    padding: 20px;
    align-items: stretch;
    height: 100%;
  }

  #statsContainer {
    display: grid;
    grid-template-areas: 'a a';
    grid-auto-columns: 1fr;
    grid-auto-rows: 1fr;
    justify-items: stretch;
    justify-content: space-around;
    align-items: stretch;
    align-content: stretch;
    column-gap: 15px;
    row-gap: 15px;
  }

  .square {
    border: 3px solid #ffffff;
    border-radius: 5px;
    display: flex;
    color: #1976d2;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    @include background-opacity(#ffffff, 0.85);

    .num {
      display: block;
      font-size: 30pt;
      transition: all 0.5s;
    }

    .num2 {
      display: block;
      font-size: 20pt;
      transition: all 0.5s;
    }

    .statName {
      display: block;
      font-size: 15pt;
    }
  }

  .square:hover {
    border: 2px solid #1976d2;
    font-weight: bolder;
  }
}

.description {
  font-size: 15pt;
  font-weight: bold;
  color: inherit;
}
</style>
