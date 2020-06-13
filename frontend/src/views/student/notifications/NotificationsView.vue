<template>
  <v-card class="table">
    <v-data-table
      :headers="headers"
      :items="notifications"
      :search="search"
      disable-pagination
      :hide-default-footer="true"
      :mobile-breakpoint="0"
      multi-sort
      data-cy="allNotifications"
    >
      <template v-slot:top>
        <v-card-title>
          <v-text-field
            v-model="search"
            append-icon="search"
            label="Search"
            class="mx-2"
          />
        </v-card-title>
      </template>
    </v-data-table>
  </v-card>
</template>

<script lang="ts">
  import { Component, Prop, Vue, Watch } from 'vue-property-decorator';
import Notification from '@/models/user/Notification';
import RemoteServices from '@/services/RemoteServices';

@Component({
  components: {}
})
export default class NotificationsView extends Vue {
  @Prop({ type: String, required: true }) username!: string;

  notifications: Notification[] = [];
  search: string = '';
  headers: object = [
    {
      text: 'Notification Number',
      value: 'id',
      align: 'center',
      width: '10%'
    },
    {
      text: 'Title',
      value: 'title',
      align: 'center',
      width: '10%'
    },
    {
      text: 'Content',
      value: 'content',
      align: 'center',
      width: '10%'
    },
    {
      text: 'Creation Date',
      value: 'creationDate',
      align: 'center',
      width: '10%'
    }
  ];

  async created() {
    await this.getNotifications();
  }

  @Watch('username')
  async getNotifications() {
    await this.$store.dispatch('loading');
    try {
      this.notifications = await RemoteServices.getNotifications(this.username);
      this.notifications.sort((a, b) => this.sortById(a, b));
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  sortById(a: Notification, b: Notification) {
    if (a.id && b.id) return a.id > b.id ? 1 : -1;
    else return 0;
  }
}
</script>

<style lang="scss" scoped></style>
