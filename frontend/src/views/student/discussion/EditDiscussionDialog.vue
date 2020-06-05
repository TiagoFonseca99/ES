<template>
  <v-dialog
    :value="dialog"
    @input="$emit('dialog', false)"
    @keydown.esc="$emit('dialog', false)"
    max-width="75%"
    max-height="80%"
  >
    <v-card>
      <v-card-title><span class="headline">Edit Discussion</span></v-card-title>
      <v-card-text>
        <v-textarea
          outline
          rows="10"
          v-model="editDiscussion.content"
          label="Content"
        ></v-textarea>
      </v-card-text>
      <v-card-actions>
        <v-spacer />
        <v-btn color="primary" @click="$emit('dialog', false)">Cancel</v-btn>
        <v-btn color="primary" @click="saveDiscussion">Save</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { Component, Model, Prop, Vue, Watch } from 'vue-property-decorator';
import Discussion from '@/models/management/Discussion';
import RemoteServices from '@/services/RemoteServices';

@Component
export default class EditDiscussionDialog extends Vue {
  @Model('dialog', Boolean) dialog!: boolean;
  @Prop(Discussion) readonly discussion!: Discussion;

  editDiscussion!: Discussion;

  created() {
    this.updateDiscussion();
  }

  @Watch('discussion', { immediate: true, deep: true })
  updateDiscussion() {
    this.editDiscussion = new Discussion(this.discussion);
  }

  async saveDiscussion() {
    if (
      this.editDiscussion &&
      (!this.editDiscussion.content ||
        this.editDiscussion.content.trim().length === 0)
    ) {
      await this.$store.dispatch('error', 'Discussion must have content');
      return;
    }

    try {
      const result = await RemoteServices.editDiscussion(this.editDiscussion);
      this.$emit('save-discussion', result);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }
}
</script>

<style lang="scss" scoped></style>
