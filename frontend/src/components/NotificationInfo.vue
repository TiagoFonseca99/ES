<template>
  <span class="notification" v-bind:class="{ unopened: unopened, all: all }">
    <v-icon x-large style="padding: 0 30px 0 0;">{{
      notificationIcon(notification.type)
    }}</v-icon>
    <span style="text-align: left">
      <h3>{{ notification.title }}</h3>
      {{ notification.content }}
      <v-spacer />
      <v-chip x-small style="align-content: end">{{
        convertDate(notification.creationDate)
      }}</v-chip>
    </span>
  </span>
</template>

<script lang="ts">
import { Component, Vue, Prop } from 'vue-property-decorator';
import { ISOtoString } from '@/services/ConvertDateService';
import Notification from '@/models/user/Notification';

@Component
export default class NotificationInfo extends Vue {
  @Prop({ type: Notification, required: true })
  readonly notification!: Notification;
  @Prop({ type: Boolean, required: true })
  readonly unopened!: boolean;
  @Prop({ type: Boolean, required: false })
  readonly all!: boolean;

  convertDate(date: string) {
    return ISOtoString(date);
  }

  notificationIcon(type: string) {
    switch (type) {
      case 'TOURNAMENT':
        return 'fa-trophy';
      case 'ANNOUNCEMENT':
        return 'fa-bullhorn';
    }
  }
}
</script>
<style lang="scss" scoped>
.notification {
  display: flex;
  padding: 10px;
  opacity: 50%;
  width: 100%;
}
.all {
  opacity: 100%;
}
.unopened {
  background-color: whitesmoke;
  opacity: 100%;
}
</style>
