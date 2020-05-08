<template>
  <v-container
    fluid
    style="height: 100%; position: relative; display: flex; flex-direction: column"
  >
    <h2>Course Execution Dashboard</h2>
    <v-container fluid style="position: relative; max-height: 100%; flex: 1;">
      <v-row style="width: 100%; height: 100%">
        <v-col>
          <v-card class="dashCard flexCard">
            <v-card-title class="justify-center">Information</v-card-title>
            <div class="dashInfo" v-if="course">
              <div class="square taller">
                <b class="infoName">Course Name</b>
                <span style="color: black">{{ course.name }}</span>
              </div>
              <div class="square taller">
                <b class="infoName">Course Acronym</b>
                <span style="color: black">{{ course.acronym }}</span>
              </div>
              <div class="square taller">
                <b class="infoName">Academic Term</b>
                <span style="color: black">{{ course.academicTerm }}</span>
              </div>
              <div class="square taller">
                <b class="infoName">Course Type</b>
                <span style="color: black">{{
                  course.courseExecutionType
                }}</span>
              </div>
              <div class="square taller">
                <b class="infoName">Status</b>
                <span style="color: black"> {{ course.status }}</span>
              </div>
            </div>
            <div v-else>
              <p class="description" style="color: inherit">
                No course execution info to show
              </p>
            </div>
          </v-card>
        </v-col>
        <v-col :cols="5">
          <v-card class="dashCard flexCard">
            <v-card-title class="justify-center">Tournaments</v-card-title>
            <v-data-table
              :headers="headers"
              :items="tournaments"
              :sort-by="['id']"
              :hide-default-footer="true"
              :mobile-breakpoint="0"
              class="fill-height"
            >
              <template v-slot:item.startTime="{ item }">
                <v-chip small color="primary">
                  {{ item.startTime }}
                </v-chip>
              </template>
              <template v-slot:item.endTime="{ item }">
                <v-chip small color="primary">
                  {{ item.endTime }}
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
                <p class="statName">Total Submissions</p>
              </div>
              <div class="square">
                <animated-number
                  class="num"
                  :number="info.numApprovedSubmissions"
                />
                <p class="statName">Total Approved Submissions</p>
              </div>
              <div class="square">
                <animated-number
                  class="num"
                  :number="info.numRejectedSubmissions"
                />
                <p class="statName">Total Rejected Submissions</p>
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
                <p class="statName">Total Discussions</p>
              </div>
              <div class="square">
                <animated-number
                  class="num"
                  :number="info.numPublicDiscussions"
                />
                <p class="statName">Total Public Discussions</p>
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
import AnimatedNumber from '@/components/AnimatedNumber.vue';
import Course from '@/models/user/Course';
import Tournament from '@/models/user/Tournament';

@Component({
  components: { AnimatedNumber }
})
export default class DashboardView extends Vue {
  info: Dashboard | null = null;
  course: Course | null = null;
  tournaments: Tournament[] = [];

  headers: object = [
    { text: 'Tournament Number', value: 'id', align: 'center' },
    { text: 'Start Time', value: 'startTime', align: 'center' },
    { text: 'End Time', value: 'endTime', align: 'center' }
  ];

  async created() {
    await this.$store.dispatch('loading');
    try {
      this.info = await RemoteServices.getCourseDashboardInfo(
        this.$store.getters.getCurrentCourse.courseExecutionId
      );
      if (this.info.joinedTournaments) {
        console.log(this.info.joinedTournaments);
        this.tournaments = this.info.joinedTournaments.sort();
      }
      this.course = this.$store.getters.getCurrentCourse;
    } catch (error) {
      await this.$store.dispatch('error', error);
    }

    await this.$store.dispatch('clearLoading');
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

  .taller {
    height: 15%;
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
      font-size: 12pt;
    }

    .infoName {
      display: block;
      font-size: 20pt;
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
