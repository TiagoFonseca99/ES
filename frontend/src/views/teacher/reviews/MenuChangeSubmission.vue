<template>
  <v-dialog
    :value="dialog"
    @input="$emit('dialog', false)"
    @keydown.esc="$emit('dialog', false)"
    max-width="40%"
    max-height="80%"
  >
    <v-card>
      <v-card-title>
        <span class="headline">{{
          'Do you want to change the submission?'
        }}</span>
      </v-card-title>

      <v-card-actions>
        <v-spacer />
        <v-btn
          color="blue darken-1"
          data-cy="YesButton"
          @click="changeSubmission"
          >Yes</v-btn
        >
        <v-spacer />
        <v-btn color="blue darken-1" data-cy="NoButton" @click="acceptReview"
          >No</v-btn
        >
        <v-spacer />
      </v-card-actions>
      <change-submission
        v-if="editQuestion"
        v-model="ChangeSubmissions"
        :question="editQuestion"
        :submission="editSubmission"
        v-on:change-sub="onSaveChange"
      />
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { Component, Model, Prop, Vue } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import Review from '@/models/management/Review';
import Submission from '@/models/management/Submission';
import Question from '@/models/management/Question';
import ChangeSubmission from '@/views/teacher/reviews/ChangeSubmission.vue';

@Component({
  components: {
    'change-submission': ChangeSubmission
  }
})
export default class MenuChangeSubmission extends Vue {
  @Prop({ type: Review, required: true }) review!: Review;
  @Prop({ type: Submission, required: true }) submission!: Submission;
  @Model('dialog', Boolean) dialog!: boolean;

  currentReview!: Review;
  editSubmission!: Submission;
  editQuestion!: Question;
  ChangeSubmissions: boolean = false;

  async created() {
    this.currentReview = this.review;
    this.editSubmission = this.submission;
    this.editQuestion = this.editSubmission.questionDto;
  }

  async acceptReview() {
    try {
      console.log(this.currentReview);
      const result = await RemoteServices.createReview(this.currentReview);
      this.$emit('no-changes', result);
      this.ChangeSubmissions = false;
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }

  changeSubmission() {
    this.ChangeSubmissions = true;
  }

  async onSaveChange() {
    await RemoteServices.createReview(this.currentReview);
    this.$emit('no-changes');
    this.ChangeSubmissions = false;
  }
}
</script>
