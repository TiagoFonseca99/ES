<template>
  <div>
    <h2 v-if="this.announcements.length === 0">
      Welcome! Announcements will appear here
    </h2>
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
  display: flex;
  height: 100%;
  max-width: 1000px;
  margin-left: auto;
  margin-right: auto;
  padding-left: 10px;
  padding-right: 10px;

  h2 {
    font-size: 26px;
    margin: 20px 0;
    text-align: center;
    small {
      font-size: 0.5em;
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
}
</style>
