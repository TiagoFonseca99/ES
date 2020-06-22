<template>
  <v-dialog
    v-model="dialog"
    @input="closeReviewDialog"
    @keydown.esc="closeReviewDialog"
    max-width="75%"
  >
    <v-card>
      <v-card-title>
        <span class="headline">Review</span>
      </v-card-title>

      <v-card-text class="text-left">
        <show-review :review="review" />
      </v-card-text>

      <v-card-actions>
        <v-spacer />
        <v-btn
          data-cy="resubmit"
          dark
          color="primary"
          v-if="review.status === 'REJECTED'"
          @click="resubmitQuestion"
          >edit & resubmit</v-btn
        >
        <v-btn data-cy="close" dark color="primary" @click="closeReviewDialog"
          >close</v-btn
        >
      </v-card-actions>
      <edit-submission-dialog
        v-if="currentQuestion"
        v-model="editSubmissionDialog"
        :question="currentQuestion"
        :submission="currentSubmission"
        v-on:submit-question="onSaveQuestion"
      />
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { Component, Vue, Prop, Watch, Model } from 'vue-property-decorator';
import Review from '@/models/management/Review';
import ShowReview from '@/views/student/questions/ShowReview.vue';
import Question from '@/models/management/Question';
import EditSubmissionDialog from '@/views/student/questions/EditSubmissionDialog.vue';
import Submission from '@/models/management/Submission';

@Component({
  components: {
    'show-review': ShowReview,
    'edit-submission-dialog': EditSubmissionDialog
  }
})
export default class ShowReviewDialog extends Vue {
  @Prop({ type: Review, required: true }) readonly review!: Review;
  @Prop({ type: Submission, required: true }) readonly submission!: Submission;
  @Prop({ type: Question, required: true }) readonly question!: Question;
  @Model('dialog', Boolean) dialog!: boolean;

  currentQuestion: Question | null = null;
  currentSubmission: Submission | null = null;
  editSubmissionDialog: boolean = false;

  closeReviewDialog() {
    this.$emit('close-show-review-dialog');
  }

  @Watch('editSubmissionDialog')
  closeError() {
    if (!this.editSubmissionDialog) {
      this.currentQuestion = null;
    }
  }

  resubmitQuestion() {
    this.currentQuestion = this.question;
    this.currentSubmission = this.submission;
    this.currentSubmission.courseExecutionId = this.$store.getters.getCurrentCourse.courseExecutionId;
    this.editSubmissionDialog = true;
  }

  async onSaveQuestion() {
    this.editSubmissionDialog = false;
    this.closeReviewDialog();
  }
}
</script>
