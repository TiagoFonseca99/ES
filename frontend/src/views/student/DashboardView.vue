<template>
  <v-container
    fluid
    style="height: 100%; position: relative; display: flex; flex-direction: column"
  >
    <h2>Student Dashboard</h2>
    <v-container fluid style="position: relative; max-height: 100%; flex: 1;">
      <v-row style="width: 100%; height: 100%">
        <v-col>
          <v-card class="dashCard">
            <v-card-title class="justify-center">User Info</v-card-title>
            <div class="text-left" style="padding-left: 25px;">
              <b>Name: </b>
              <span>{{ info !== null ? info.name : 'Unknown user' }}</span
              ><br />
              <b>Username: </b>
              <span>{{ info !== null ? info.username : 'Unknown user' }}</span>
            </div>
            <v-container>
              <v-col>
                <v-card-title class="justify-center">Statistics</v-card-title>
                <div id="statsContainer" v-if="stats !== null">
                  <div class="square text-center">
                    <animated-number class="num" :number="stats.totalQuizzes" />
                    <p class="statName">Total Quizzes Solved</p>
                  </div>
                  <div class="square justify-center">
                    <animated-number class="num" :number="stats.totalAnswers" />
                    <p class="statName">Total Questions Answered</p>
                  </div>
                  <div class="square">
                    <animated-number
                      class="num"
                      :number="stats.totalUniqueQuestions"
                    />
                    <p class="statName">Unique Questions Answered</p>
                  </div>
                  <div class="square">
                    <animated-number class="num" :number="stats.correctAnswers"
                      >%</animated-number
                    >
                    <p class="statName">Correct Answers</p>
                  </div>
                  <div class="square">
                    <animated-number
                      class="num"
                      :number="stats.improvedCorrectAnswers"
                      >%</animated-number
                    >
                    <p class="statName">Improved Correct Questions</p>
                  </div>
                  <div class="square">
                    <animated-number
                      :number="
                        (stats.totalUniqueQuestions * 100) /
                          stats.totalAvailableQuestions
                      "
                      class="num"
                      >%</animated-number
                    >
                    <p class="statName">Questions Seen</p>
                  </div>
                </div>
                <div v-else>
                  <p class="description" style="color: inherit">
                    No stats to show
                  </p>
                </div>
              </v-col>
            </v-container>
          </v-card>
        </v-col>
        <v-col :cols="5">
          <v-card class="dashCard flexCard">
            <v-card-title class="justify-center">Tournaments</v-card-title>
            <v-data-table
              :headers="headers"
              :items="tournaments"
              :hide-default-footer="true"
              :mobile-breakpoint="0"
              class="fill-height"
            >
              <template v-slot:item.score="{ item }">
                <v-chip>
                  {{ score(item) }}
                </v-chip>
              </template>
            </v-data-table>
          </v-card>
        </v-col>
        <v-col :cols="2">
          <v-card class="dashCard flexCard">
            <v-card-title class="justify-center">Submissions</v-card-title>
            <div class="dashInfo" v-if="info !== null">
              <div class="square">
                <animated-number class="num" :number="info.numSubmissions" />
                <p class="statName">Submissions</p>
              </div>
              <div class="square">
                <animated-number
                  class="num"
                  :number="info.numApprovedSubmissions"
                />
                <p class="statName">Approved Submissions</p>
              </div>
            </div>
            <div v-else>
              <p class="description" style="color: inherit">
                No submission stats to show
              </p>
            </div>
          </v-card>
        </v-col>
        <v-col :cols="2">
          <v-card class="dashCard flexCard">
            <v-card-title class="justify-center" style="display: block;"
              >Discussions</v-card-title
            >
            <div class="dashInfo" v-if="info !== null">
              <div class="square">
                <animated-number class="num" :number="info.numDiscussions" />
                <p class="statName">Discussions</p>
              </div>
              <div class="square">
                <animated-number
                  class="num"
                  :number="info.numPublicDiscussions"
                />
                <p class="statName">Public Discussions</p>
              </div>
            </div>
            <div v-else>
              <p class="description">
                No discussions stats to show
              </p>
            </div>
          </v-card>
        </v-col>
      </v-row>
    </v-container>
  </v-container>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import Dashboard from '@/models/management/Dashboard';
import RemoteServices from '@/services/RemoteServices';
import StudentStats from '@/models/statement/StudentStats';
import Tournament from '@/models/user/Tournament';
import AnimatedNumber from '@/components/AnimatedNumber.vue';
import SolvedQuiz from '@/models/statement/SolvedQuiz';

@Component({
  components: { AnimatedNumber }
})
export default class DashboardView extends Vue {
  info: Dashboard | null = null;
  stats: StudentStats | null = null;
  tournaments: Tournament[] = [];
  quizzes: SolvedQuiz[] = [];

  headers: object = [
    { text: 'Tournament Number', value: 'id', align: 'center' },
    { text: 'Start Time', value: 'startTime', align: 'center' },
    { text: 'End Time', value: 'endTime', align: 'center' },
    { text: 'Score', value: 'score', align: 'center' }
  ];

  async created() {
    await this.$store.dispatch('loading');
    try {
      this.info = await RemoteServices.getDashboardInfo();
      this.stats = await RemoteServices.getUserStats();
      this.quizzes = await RemoteServices.getSolvedQuizzes();
      if (this.info.joinedTournaments)
        this.tournaments = this.info.joinedTournaments;
    } catch (error) {
      await this.$store.dispatch('error', error);
    }

    await this.$store.dispatch('clearLoading');
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
    return `${correct}/${quiz.statementQuiz.questions.length}`;
  }

  score(tournament: Tournament) {
    let score = '';
    console.log(this.quizzes.length);
    console.log(tournament.quizId);
    console.log(tournament.id)
    this.quizzes.map(quiz => {
      console.log('quiz: ' + quiz.statementQuiz.id);
      if (quiz.statementQuiz.id == tournament.quizId) {
        console.log('ola');
        score = this.calculateScore(quiz);
      }
    });

    if (score == '') return 'not solved';
    return score;
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
    border: 2px solid #333333;
    border-radius: 5px;
    display: flex;
    color: #1976d2;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    @include background-opacity(#7f7f7f, 0.75);

    .num {
      display: block;
      font-size: 30pt;
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
