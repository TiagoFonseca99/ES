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
          {{ 'Create Review' }}
        </span>
      </v-card-title>

      <v-card-text class="text-left" v-if="currentReview">
        <v-container grid-list-md fluid>
          <v-layout column wrap>
            <v-flex xs24 sm12 md12>
              <v-textarea
                outline
                rows="10"
                v-model="currentReview.justification"
                data-cy="Justification"
                label="Justification"
              ></v-textarea>
            </v-flex>
          </v-layout>
        </v-container>
      </v-card-text>

      <v-card-actions>
        <v-btn
          color="blue darken-1"
          data-cy="cancelButton"
          @click="$emit('dialog', false)"
          >Cancel</v-btn
        >
        <v-spacer />
        <v-btn
          color="blue darken-1"
          data-cy="Reject"
          @click="createReview('REJECTED')"
          >Reject</v-btn
        >
        <v-btn
          color="blue darken-1"
          data-cy="Approve"
          @click="createReview('APPROVED')"
          >Approve</v-btn
        >
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>
<script lang="ts">
import { Component, Model, Prop, Vue } from 'vue-property-decorator';
import Review from '@/models/management/Review';
import RemoteServices from '@/services/RemoteServices';
import Submission from '@/models/management/Submission';

@Component
export default class EditReview extends Vue {
  @Prop({ type: Submission, required: true }) submission!: Submission;
  @Model('dialog', Boolean) dialog!: boolean;

  editSubmission!: Submission;
  currentReview!: Review;

  created() {
    this.editSubmission = this.submission;
    this.currentReview = new Review();
  }

  async createReview(status: string) {
    if (this.currentReview && !this.currentReview.justification) {
      await this.$store.dispatch('error', 'Review must have justification');
      return;
    }
    try {
      this.currentReview.submissionId = this.editSubmission.id;
      this.currentReview.studentId = this.editSubmission.studentId;
      this.currentReview.status = status;
      const result = await RemoteServices.createReview(this.currentReview);
      this.$emit('create-review', result);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }
}
</script>
