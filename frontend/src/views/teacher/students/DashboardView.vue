<template>
  <v-container
    fluid
    style="height: 100%; position: relative; display: flex; flex-direction: column"
    :key="componentKey"
    v-if="this.$store.getters.isLoggedIn"
  >
    <v-container fluid style="position: relative; max-height: 100%; flex: 1;">
      <v-row style="width: 100%; height: 100%">
        <v-col>
          <v-card class="dashCard">
            <v-card-title class="justify-center">User Info</v-card-title>
            <div class="text-left" style="padding-left: 25px;">
              <b class="primary--text">Name: </b>
              <span data-cy="name">{{
                info !== null ? info.name : 'Unknown user'
              }}</span
              ><br />
              <b class="primary--text">Username: </b>
              <span data-cy="username">{{
                info !== null ? info.username : 'Unknown user'
              }}</span>
            </div>
            <v-container>
              <v-col>
                <v-card-title class="justify-center">Statistics</v-card-title>
                <div
                  class="switchContainer"
                  style="display: flex; flex-direction: row; position: relative;"
                >
                  <v-switch
                    style="flex: 1"
                    v-if="
                      info !== null &&
                        info.username === this.$store.getters.getUser.username
                    "
                    :input-value="!info.userStatsPublic"
                    append-icon="lock"
                    :label="info.userStatsPublic ? 'Public' : 'Private'"
                    @change="toggleStats()"
                  />
                </div>
                <div
                  id="statsContainer"
                  v-if="
                    stats !== null &&
                      ((info !== null &&
                        info.username ===
                          this.$store.getters.getUser.username) ||
                        this.info.userStatsPublic)
                  "
                >
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
                <v-icon v-else-if="stats !== null" size="500%" color="red"
                  >lock</v-icon
                >
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
            <v-container>
              <v-switch
                style="flex: 1"
                v-if="
                  info !== null &&
                    info.username === this.$store.getters.getUser.username
                "
                :input-value="!info.tournamentStatsPublic"
                append-icon="lock"
                :label="info.tournamentStatsPublic ? 'Public' : 'Private'"
                @change="toggleTournaments()"
              />
            </v-container>
            <v-data-table
              :headers="headers"
              :items="tournaments"
              :sort-by="['id']"
              :hide-default-footer="true"
              :mobile-breakpoint="0"
              class="fill-height"
              v-if="
                this.info !== null &&
                  (info.username === this.$store.getters.getUser.username ||
                    this.info.tournamentStatsPublic)
              "
            >
              <template v-slot:item.id="{ item }">
                <v-chip
                  color="primary"
                  small
                  :to="openTournamentDashboard(item)"
                >
                  <span> {{ item.id }} </span>
                </v-chip>
              </template>
              <template v-slot:item.startTime="{ item }">
                <v-chip small>
                  {{ item.startTime }}
                </v-chip>
              </template>
              <template v-slot:item.endTime="{ item }">
                <v-chip small>
                  {{ item.endTime }}
                </v-chip>
              </template>
              <template v-slot:item.score="{ item }">
                <v-chip small :color="getPercentageColor(score(item))">
                  {{ score(item) === '' ? 'NOT SOLVED' : score(item) }}
                </v-chip>
              </template>
            </v-data-table>
            <v-icon v-else-if="info !== null" color="red" size="500%"
              >lock</v-icon
            >
          </v-card>
        </v-col>
        <v-col :cols="2">
          <v-card class="dashCard flexCard">
            <v-card-title class="justify-center">Submissions</v-card-title>
            <v-container>
              <v-switch
                style="flex: 1"
                v-if="
                  info !== null &&
                    info.username === this.$store.getters.getUser.username
                "
                :input-value="!info.submissionStatsPublic"
                append-icon="lock"
                :label="info.submissionStatsPublic ? 'Public' : 'Private'"
                @change="toggleSubmissions()"
              />
            </v-container>
            <div
              class="dashInfo"
              v-if="
                info !== null &&
                  (info.username === this.$store.getters.getUser.username ||
                    this.info.submissionStatsPublic)
              "
            >
              <div class="square" data-cy="numSubmissions">
                <animated-number class="num" :number="info.numSubmissions" />
                <p class="statName">Submissions</p>
              </div>
              <div class="square" data-cy="numApprovedSubmissions">
                <animated-number
                  class="num"
                  :number="info.numApprovedSubmissions"
                />
                <p class="statName">Approved Submissions</p>
              </div>
              <div class="square" data-cy="numRejectedSubmissions">
                <animated-number
                  class="num"
                  :number="info.numRejectedSubmissions"
                />
                <p class="statName">Rejected Submissions</p>
              </div>
            </div>
            <v-icon v-else-if="info !== null" size="500%" color="red"
              >lock</v-icon
            >
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
            <v-container>
              <v-switch
                style="flex: 1"
                v-if="
                  info !== null &&
                    info.username === this.$store.getters.getUser.username
                "
                :input-value="!info.discussionStatsPublic"
                append-icon="lock"
                :label="info.discussionStatsPublic ? 'Public' : 'Private'"
                @change="toggleDiscussions()"
              />
            </v-container>
            <div
              class="dashInfo"
              v-if="
                info !== null &&
                  (info.username === this.$store.getters.getUser.username ||
                    this.info.discussionStatsPublic)
              "
            >
              <div class="square">
                <animated-number
                  class="num"
                  :number="info.numDiscussions"
                  data-cy="numDiscussions"
                />
                <p class="statName">Discussions</p>
              </div>
              <div class="square">
                <animated-number
                  class="num"
                  :number="info.numPublicDiscussions"
                  data-cy="numPublicDiscussions"
                />
                <p class="statName">Public Discussions</p>
              </div>
            </div>
            <v-icon v-else-if="info !== null" size="500%" color="red"
              >lock</v-icon
            >
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
import { Component, Prop, Vue, Watch } from 'vue-property-decorator';
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
  @Prop({ type: String, required: true }) username!: string;

  info: Dashboard | null = null;
  stats: StudentStats | null = null;
  tournaments: Tournament[] = [];
  quizzes: SolvedQuiz[] = [];
  componentKey: number = 0;

  headers: object = [
    { text: 'Tournament Number', value: 'id', align: 'center' },
    { text: 'Start Time', value: 'startTime', align: 'center' },
    { text: 'End Time', value: 'endTime', align: 'center' },
    { text: 'Score', value: 'score', align: 'center' }
  ];

  async created() {
    await this.getDashboardInfo();
  }

  @Watch('username')
  async getDashboardInfo() {
    this.componentKey += 1;
    this.info = null;
    this.stats = null;
    await this.$store.dispatch('loading');
    try {
      this.info = await RemoteServices.getDashboardInfo(this.username);
      this.stats = await RemoteServices.getUserStats(this.username);
      this.quizzes = await RemoteServices.getSolvedQuizzes(this.username);
      if (this.info.joinedTournaments)
        this.tournaments = this.info.joinedTournaments.sort();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }

    await this.$store.dispatch('clearLoading');
  }

  async toggleDiscussions() {
    try {
      this.info = await RemoteServices.toggleDiscussionStats();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }

  async toggleSubmissions() {
    try {
      this.info = await RemoteServices.toggleSubmissionStats();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }

  async toggleTournaments() {
    try {
      this.info = await RemoteServices.toggleTournamentStats();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }

  async toggleStats() {
    try {
      this.info = await RemoteServices.toggleUserStats();
    } catch (error) {
      await this.$store.dispatch('error', error);
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
    return `${correct}/${quiz.statementQuiz.questions.length}`;
  }

  score(tournament: Tournament) {
    let score = '';
    this.quizzes.map(quiz => {
      if (quiz.statementQuiz.id == tournament.quizId) {
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

  openTournamentDashboard(tournament: Tournament) {
    if (tournament) return '/teacher/tournament?id=' + tournament.id;
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
