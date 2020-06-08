<template>
  <v-dialog
    :value="dialog"
    @input="$emit('dialog', false)"
    @keydown.esc="$emit('dialog', false)"
    max-width="75%"
    max-height="80%"
  >
    <v-card>
      <v-card-title><span class="headline">Edit Reply</span></v-card-title>
      <v-card-text>
        <v-textarea
          outline
          rows="10"
          v-model="editReply.message"
          label="Content"
          data-cy="editContent"
        ></v-textarea>
      </v-card-text>
      <v-card-actions>
        <v-spacer />
        <v-btn color="primary" @click="$emit('dialog', false)">Cancel</v-btn>
        <v-btn color="primary" @click="saveReply" data-cy="submitEdit"
          >Save</v-btn
        >
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { Component, Model, Prop, Vue, Watch } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import Reply from '@/models/management/Reply';

@Component
export default class EditReplyDialog extends Vue {
  @Model('dialog', Boolean) dialog!: boolean;
  @Prop(Reply) readonly reply!: Reply;

  editReply!: Reply;

  created() {
    this.updateReply();
  }

  @Watch('reply', { immediate: true, deep: true })
  updateReply() {
    this.editReply = new Reply(this.reply);
  }

  async saveReply() {
    if (
      this.editReply &&
      (!this.editReply.message || this.editReply.message.trim().length === 0)
    ) {
      await this.$store.dispatch('error', 'Reply must have content');
      return;
    }

    try {
      const result = await RemoteServices.editReply(this.editReply);
      this.$emit('save-reply', result);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }
}
</script>

<style lang="scss" scoped></style>
