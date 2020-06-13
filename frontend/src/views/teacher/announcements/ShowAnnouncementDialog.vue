<template>
  <v-dialog
    v-model="dialog"
    @input="$emit('dialog', false)"
    @keydown.esc="$emit('dialog', false)"
    max-width="75%"
  >
    <v-card>
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
        <v-spacer />
        <v-btn data-cy="close" dark color="primary" @click="$emit('dialog')"
          >close</v-btn
        >
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { Component, Vue, Prop, Model } from 'vue-property-decorator';
import Announcement from '../../../models/management/Announcement';
import ShowAnnouncement from '@/views/teacher/announcements/ShowAnnouncement.vue';

@Component({
  components: {
    'show-announcement': ShowAnnouncement
  }
})
export default class ShowAnnouncementDialog extends Vue {
  @Prop({ type: Announcement, required: true })
  readonly announcement!: Announcement;
  @Model('dialog', Boolean) dialog!: boolean;
}
</script>
