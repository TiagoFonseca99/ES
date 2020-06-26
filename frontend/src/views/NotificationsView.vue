<template>
  <v-container>
    <v-list>
      <v-list-item>
        <v-btn-toggle
          dense
          borderless
          mandatory
          background-color="primary"
          color="white"
        >
          <v-btn color="primary" @click="filterNotifications('ALL')">{{
            'all'
          }}</v-btn>
          <v-btn
            color="primary"
            v-if="this.$store.getters.isStudent"
            @click="filterNotifications('TOURNAMENT')"
            >{{ 'tournaments' }}</v-btn
          >
          <v-btn color="primary" @click="filterNotifications('DISCUSSION')">{{
            'discussions'
          }}</v-btn>
          <v-btn color="primary" @click="filterNotifications('SUBMISSION')">{{
            'submissions'
          }}</v-btn>
          <v-btn
            color="primary"
            v-if="this.$store.getters.isStudent"
            @click="filterNotifications('REVIEW')"
            >{{ 'reviews' }}</v-btn
          >
        </v-btn-toggle>
      </v-list-item>
      <v-list-item
        v-if="notifications.length === 0"
        style="padding: 10px 30px 10px 0;"
      >
        <v-icon x-large style="padding: 30px 30px;">far fa-meh</v-icon>
        <span style="text-align: left">
          <h3>{{ 'No Notifications' }}</h3>
          {{
            filter === 'ALL'
              ? 'Notifications will appear here as you use QuizzesTutor!'
              : 'No notifications for selected type'
          }}
        </span>
      </v-list-item>
      <v-list-item
        v-for="notification in this.notifications"
        :key="notification.id"
      >
        <notification-info
          :notification="notification"
          :unopened="false"
          :all="true"
        />
      </v-list-item>
    </v-list>
  </v-container>
</template>

<script lang="ts">
import { Component, Prop, Vue, Watch } from 'vue-property-decorator';
import Notification from '@/models/user/Notification';
import NotificationInfo from '@/components/NotificationInfo.vue';
import RemoteServices from '@/services/RemoteServices';
import { ISOtoString } from '@/services/ConvertDateService';

@Component({ components: { 'notification-info': NotificationInfo } })
export default class NotificationsView extends Vue {
  @Prop({ type: String, required: true }) username!: string;

  allNotifications: Notification[] = [];
  notifications: Notification[] = [];
  filter: string = 'ALL';

  async created() {
    await this.getNotifications();
  }

  @Watch('username')
  async getNotifications() {
    await this.$store.dispatch('loading');
    try {
      this.allNotifications = await RemoteServices.getNotifications(
        this.username
      );
      this.notifications = this.allNotifications.sort((a, b) =>
        this.sortByDate(a, b)
      );
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  sortByDate(a: Notification, b: Notification) {
    if (a.id && b.id) return a.creationDate > b.creationDate ? -1 : 1;
    else return 0;
  }

  filterNotifications(type: string) {
    this.filter = type;
    if (type === 'ALL') {
      this.notifications = this.allNotifications;
    } else if (type === 'SUBMISSION') {
      this.notifications = this.allNotifications.filter(
        a => a.type == 'SUBMISSION' || a.type == 'QUESTION'
      );
    } else {
      this.notifications = this.allNotifications.filter(a => a.type == type);
    }
  }
}
</script>

<style lang="scss" scoped></style>
