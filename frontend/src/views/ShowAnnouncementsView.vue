<template>
  <div v-bind:class="{ container: this.announcements.length === 0 }">
    <h1
      v-if="this.announcements.length === 0 && this.$store.getters.getLoading"
      id="home-title"
      class="display-2 font-weight-thin mb-3"
    >
      {{ appName }}
    </h1>
    <ul>
      <v-card
        class="announcement"
        v-for="announcement in this.announcements"
        :key="announcement.id"
      >
        <v-card-title>
          <span class="headline">{{ announcement.title }}</span>
        </v-card-title>

        <v-card-text class="text-left">
          <show-announcement :announcement="announcement" />
        </v-card-text>
        <v-card-actions>
          <v-chip small>
            <span>{{ announcement.creationDate }}</span>
          </v-chip>
          <span id="edited" v-if="announcement.edited">
            {{ '  (edited)' }}</span
          >
          <v-spacer />
          <v-chip small color="primary" v-if="announcement.username">
            <span>{{ announcement.username }}</span>
          </v-chip>
        </v-card-actions>
      </v-card>
    </ul>
    <div class="buttons" v-if="this.announcements.length !== 0">
      <v-btn-toggle
        dense
        borderless
        mandatory
        background-color="primary"
        color="white"
      >
        <v-btn color="primary" @click="changeAnnouncementsNumber(5)">{{
          5
        }}</v-btn>
        <v-btn color="primary" @click="changeAnnouncementsNumber(10)">{{
          10
        }}</v-btn>
        <v-btn color="primary" @click="changeAnnouncementsNumber(15)">{{
          15
        }}</v-btn>
        <v-btn color="primary" @click="changeAnnouncementsNumber()">{{
          'See all'
        }}</v-btn>
      </v-btn-toggle>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import Announcement from '../models/management/Announcement';
import ShowAnnouncement from '@/views/teacher/announcements/ShowAnnouncement.vue';

@Component({ components: { 'show-announcement': ShowAnnouncement } })
export default class ReviewView extends Vue {
  appName: string = process.env.VUE_APP_NAME;
  allAnnouncements: Announcement[] = [];
  announcements: Announcement[] = [];

  async created() {
    await this.$store.dispatch('loading');
    try {
      [this.allAnnouncements] = await Promise.all([
        RemoteServices.getCourseExecutionAnnouncements()
      ]);
      this.allAnnouncements.sort((a, b) => this.sortNewestFirst(a, b));
      this.changeAnnouncementsNumber(5);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  sortNewestFirst(a: Announcement, b: Announcement) {
    if (a.creationDate && b.creationDate)
      return a.creationDate < b.creationDate ? 1 : -1;
    else return 0;
  }

  rangedAnnouncements(start: number, end: number) {
    return this.allAnnouncements.slice(start, end);
  }

  changeAnnouncementsNumber(amount: number | null = null) {
    if (amount === null) this.announcements = this.allAnnouncements;
    else this.announcements = this.rangedAnnouncements(0, amount);
  }
}
</script>

<style lang="scss" scoped>
.container {
  height: 100%;
  display: flex;
  flex-direction: column;
  flex-wrap: nowrap;
  justify-content: center;
  align-items: center;

  #home-title {
    box-sizing: border-box;
    color: rgb(255, 255, 255);
    min-height: auto;
    min-width: auto;
    text-align: center;
    text-decoration: none solid rgb(255, 255, 255);
    text-rendering: optimizelegibility;
    text-size-adjust: 100%;
    column-rule-color: rgb(255, 255, 255);
    perspective-origin: 229.922px 34px;
    transform-origin: 229.922px 34px;
    caret-color: rgb(255, 255, 255);
    background: rgba(0, 0, 0, 0.75) none no-repeat scroll 0 0 / auto padding-box
      border-box;
    border: 0 none rgb(255, 255, 255);
    font: normal normal 100 normal 45px / 48px Roboto, sans-serif !important;
    margin-bottom: 70px !important;
    outline: rgb(255, 255, 255) none 0;
    padding: 10px 20px;
  }
}

.announcement {
  border-radius: 3px;
  padding: 15px 10px;
  justify-content: space-between;
  margin-bottom: 20px;
}

.buttons {
  margin-bottom: 20px;
}

#edited {
  color: grey;
  font-size: 15px;
  padding-left: 2px;
}
</style>
