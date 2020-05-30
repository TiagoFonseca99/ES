<template>
  <v-dialog
    :value="dialog"
    @input="$emit('dialog', false)"
    @keydown.esc="$emit('dialog', false)"
    max-height="80%"
    max-width="75%"
    :key="componentKey"
  >
    <v-card>
      <v-card-title class="justify-center">
        <v-spacer />
        <b style="color: #1976d2">Username: </b>
        <span data-cy="username">{{
          info !== null ? info.username : 'Unknown user'
        }}</span>
        <v-spacer />
        <b style="color: #1976d2">Name: </b>
        <span data-cy="name">{{
          info !== null ? info.name : 'Unknown user'
        }}</span>
        <v-spacer />
      </v-card-title>
      <v-col>
        <v-card class="dashCard">
          <v-container>
            <v-col>
              <div class="statsContainer" v-if="info !== null">
                <div class="square justify-center" data-cy="numTournaments">
                  <animated-number
                    class="num"
                    :number="info.joinedTournaments.length"
                    v-if="info.tournamentStatsPublic"
                  />
                  <v-icon left large v-else color="red">fa lock</v-icon>
                  <p class="statName">Tournaments</p>
                </div>
                <div class="square justify-center" data-cy="numSubmissions">
                  <animated-number
                    class="num"
                    :number="info.numSubmissions"
                    v-if="info.submissionStatsPublic"
                  />
                  <v-icon left large v-else color="red">fa lock</v-icon>
                  <p class="statName">Submissions</p>
                </div>
                <div class="square justify-center" data-cy="numDiscussions">
                  <animated-number
                    class="num"
                    :number="info.numDiscussions"
                    v-if="info.discussionStatsPublic"
                  />
                  <v-icon left large v-else color="red">fa lock</v-icon>
                  <p class="statName">Discussions</p>
                </div>
              </div>
              <div v-else>
                <p class="description" style="color: inherit">
                  No stats to show
                </p>
              </div>
            </v-col>
            <v-col>
              <div
                class="statsContainer"
                v-if="stats !== null && info.userStatsPublic"
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
              <div v-else-if="stats !== null" class="square justify-center">
                <v-icon left large color="red">fa lock</v-icon>
                <p class="statName">Stats</p>
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
      <v-card-actions>
        <v-spacer />
        <v-btn
          color="blue darken-1"
          @click="$emit('dialog', false)"
          :to="openStudentDashboard()"
          >Open Dashboard</v-btn
        >
        <v-btn color="blue darken-1" @click="$emit('dialog', false)"
          >Close</v-btn
        >
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { Component, Model, Prop, Vue, Watch } from 'vue-property-decorator';
import Dashboard from '@/models/management/Dashboard';
import StudentStats from '@/models/statement/StudentStats';
import Tournament from '@/models/user/Tournament';
import RemoteServices from '@/services/RemoteServices';
import AnimatedNumber from '@/components/AnimatedNumber.vue';
import { Student } from '@/models/management/Student';

@Component({
  components: { AnimatedNumber }
})
export default class EditSubmissionDialog extends Vue {
  @Model('dialog', Boolean) dialog!: boolean;
  @Prop({ type: String, required: true }) readonly username!: string;

  info: Dashboard | null = null;
  stats: StudentStats | null = null;
  tournaments: Tournament[] = [];
  componentKey: number = 0;

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
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  openStudentDashboard() {
    return '/student/user?username=' + this.username;
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
  @include background-opacity(#eeeeee, 0.6);

  .dashInfo {
    display: flex;
    flex-direction: column;
    justify-content: space-around;
    padding: 20px;
    align-items: stretch;
    height: 100%;
  }

  .statsContainer {
    display: grid;
    grid-template-areas: 'a a a';
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
