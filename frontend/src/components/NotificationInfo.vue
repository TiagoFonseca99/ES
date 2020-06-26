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
    let name = 'home';
    switch (type) {
      case 'TOURNAMENT':
        name = 'open-tournament';
        break;
      case 'SUBMISSION':
        name = 'reviews-management';
        break;
      case 'QUESTION':
        name = 'submissions';
        break;
      case 'REVIEW':
        name = 'reviews';
        break;
      case 'DISCUSSION':
        if (this.$store.getters.isStudent) {
          name = 'discussions';
        } else {
          name = 'questions-management';
        }
    }
    this.$router.push({ name: name });
  }
}
</script>
<style lang="scss" scoped>
.notification {
  display: flex;
  padding: 10px;
  opacity: 50%;
  width: 100%;
  cursor: pointer;
}
.icon {
  padding: 0px 30px 0 0;
}

.text {
  text-align: left;
}

.all {
  opacity: 100%;
}
.unopened {
  background-color: whitesmoke;
  opacity: 100%;
}
</style>
