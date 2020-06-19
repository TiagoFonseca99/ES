<template>
  <v-toolbar-items class="hidden-sm-and-down" hide-details>
    <v-menu open-on-click nudge-right="10" nudge-top="10" top>
      <template v-slot:activator="{ on }">
        <v-chip
          v-on="on"
          x-large
          class="footer front"
          color="primary"
          @click="setLastNotificationAccess"
        >
          <v-icon x-large style="padding: 0">
            notifications
          </v-icon>
          <v-chip v-if="unopened.length !== 0" color="error">
            <h3>{{ unopened.length }}</h3>
          </v-chip>
        </v-chip>
      </template>
      <v-list min-width="250" max-width="500">
        <v-list-item
          v-if="notifications.length === 0"
          style="padding: 10px 30px 10px 0;"
        >
          <v-icon x-large style="padding: 30px 30px;">fa-times</v-icon>
          <h3>No notifications</h3>
        </v-list-item>
        <v-list-item
          v-for="notification in this.notifications"
          :key="notification.id"
        >
          <notification-info
            :notification="notification"
            :unopened="isUnopenedNotification(notification)"
          />
        </v-list-item>
        <v-list-item>
          <v-spacer />
          <v-btn :to="sendToNotificationsView()" color="primary"
            >View all</v-btn
          >
        </v-list-item>
      </v-list>
    </v-menu>
  </v-toolbar-items>
</template>

<script lang="ts">
import { Component, Prop, Vue, Watch } from 'vue-property-decorator';
import NotificationInfo from '@/components/NotificationInfo.vue';
import RemoteServices from '@/services/RemoteServices';
import Notification from '@/models/user/Notification';
import User from '@/models/user/User';

@Component({ components: { 'notification-info': NotificationInfo } })
export default class NotificationsButton extends Vue {
  @Prop({ type: User, required: true })
  readonly user!: User;

  notifications: Notification[] = [];
  unopened: Notification[] = [];
  oldUnopened: Notification[] = [];
  updated!: User;
  old!: string;

  async created() {
    await this.getNotifications();
  }

  async getNotifications() {
    await this.$store.dispatch('loading');
    try {
      [this.notifications] = await Promise.all([
        RemoteServices.getNotifications(this.user.username)
      ]);
      this.updateUnopened();
      this.notifications = this.notifications
        .sort((a, b) => this.sortByDate(a, b))
        .slice(0, 5);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  @Watch('user.lastNotificationAccess')
  updateUnopened() {
    this.oldUnopened = this.unopened;
    this.unopened = this.notifications.filter(a =>
      this.isUnopened(a.creationDate)
    );
  }

  sortByDate(a: Notification, b: Notification) {
    if (a.id && b.id) return a.creationDate > b.creationDate ? -1 : 1;
    else return 0;
  }

  isUnopened(date: string) {
    return date >= this.$store.getters.getUser.lastNotificationAccess;
  }

  isUnopenedNotification(notification: Notification) {
    return this.oldUnopened.includes(notification);
  }

  async setLastNotificationAccess() {
    this.old = this.$store.getters.getUser.lastNotificationAccess;
    try {
      this.updated = await RemoteServices.setLastNotificationAccess();
      await this.$store.dispatch('updateUser', this.updated);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }

  sendToNotificationsView() {
    let type = this.$store.getters.isStudent ? '/student' : '/management';
    return (
      type + '/notifications?username=' + this.$store.getters.getUser.username
    );
  }
}
</script>

<style lang="scss" scoped>
.footer {
  position: absolute;
  bottom: 25px;
  left: 25px;
}

.front {
  position: absolute;
  z-index: 100;
}
</style>
