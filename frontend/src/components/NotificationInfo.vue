<template>
  <span
    class="notification"
    v-bind:class="{ unopened: unopened, all: all }"
    @click="routeToPage(notification.type)"
  >
    <v-icon large class="icon">{{
      notificationIcon(notification.type)
    }}</v-icon>
    <span class="text">
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
      case 'SUBMISSION':
      case 'QUESTION':
        return 'fa-question-circle';
      case 'REVIEW':
        return 'fa-edit';
      case 'DISCUSSION':
        return 'fa-comment-alt';
      default:
        return 'fa-exclamation-circle';
    }
  }

  routeToPage(type: string) {
    let path = '/';
    switch (type) {
      case 'TOURNAMENT':
        path = '/student/open';
        break;
      case 'ANNOUNCEMENT':
        path = '/';
        break;
      case 'SUBMISSION':
        path = '/management/reviews';
        break;
      case 'QUESTION':
        path = '/student/submissions';
        break;
      case 'REVIEW':
        path = '/student/reviews';
        break;
      case 'DISCUSSION':
        if (this.$store.getters.isStudent) {
          path = '/student/discussions';
        } else {
          path = '/management/questions';
        }
    }
    window.location.href = path;
  }
}
</script>
<style lang="scss" scoped>
.notification {
  display: grid;
  grid-template-areas: 'icon text text text text';
  padding: 10px;
  opacity: 50%;
  cursor: pointer;
}
.icon {
  padding: 0px 30px 0 0;
  grid-area: icon;
}

.text {
  text-align: left;
  grid-area: text;
}

.all {
  opacity: 100%;
}
.unopened {
  background-color: whitesmoke;
  opacity: 100%;
}
</style>
