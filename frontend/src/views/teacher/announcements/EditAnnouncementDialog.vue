<template>
  <v-dialog
    :value="dialog"
    @input="$emit('dialog', false)"
    @keydown.esc="$emit('dialog', false)"
    max-width="75%"
    max-height="80%"
  >
    <v-card>
      <v-card-title>
        <span class="headline">
          {{
            editAnnouncement && editAnnouncement.id === null
              ? 'New Announcement'
              : 'Edit Announcement'
          }}
        </span>
      </v-card-title>

      <v-card-text class="text-left" v-if="editAnnouncement">
        <v-container grid-list-md fluid>
          <v-layout column wrap>
            <v-flex>
              <v-textarea
                auto-grow
                row-height="1"
                v-model="editAnnouncement.title"
                data-cy="AnnouncementTitle"
                label="Title"
              ></v-textarea>
            </v-flex>
            <v-flex xs24 sm12 md12>
              <v-textarea
                rows="5"
                v-model="editAnnouncement.content"
                data-cy="AnnouncementContent"
                label="Content"
              ></v-textarea>
            </v-flex>
          </v-layout>
        </v-container>
      </v-card-text>

      <v-card-actions>
        <v-spacer />
        <v-btn color="primary" data-cy="Cancel" @click="$emit('dialog', false)"
          >Cancel</v-btn
        >
        <v-btn color="primary" data-cy="Save" @click="createAnnouncement()"
          >Save</v-btn
        >
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>
<script lang="ts">
import { Component, Model, Prop, Vue, Watch } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import Announcement from '../../../models/management/Announcement';

@Component
export default class EditAnnouncementDialog extends Vue {
  @Prop({ type: Announcement, required: true }) announcement!: Announcement;
  @Model('dialog', Boolean) dialog!: boolean;

  editAnnouncement!: Announcement;

  created() {
    this.updateAnnouncement();
  }

  @Watch('announcement', { immediate: true, deep: true })
  updateAnnouncement() {
    this.editAnnouncement = this.announcement;
  }

  async createAnnouncement() {
    if (
      this.editAnnouncement &&
      (!this.editAnnouncement.title || !this.editAnnouncement.content)
    ) {
      await this.$store.dispatch(
        'error',
        'Announcement must have title and content'
      );
      return;
    }

    try {
      const result =
        this.editAnnouncement.id != null
          ? await RemoteServices.updateAnnouncement(this.editAnnouncement)
          : await RemoteServices.createAnnouncement(this.editAnnouncement);
      this.$emit('save-announcement', result);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }
}
</script>
