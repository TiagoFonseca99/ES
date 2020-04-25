<template>
  <div class="container">
    <h2>Submission Reviews</h2>
    <ul>
      <li class="list-header">
        <div class="col">Question Title</div>
        <div class="col">Creation Date</div>
        <div class="col">Review Date</div>
        <div class="col">Status</div>
        <div class="col">Justification</div>
      </li>
      <li class="list-row" v-for="review in this.reviews" :key="review.id">
        <div class="col" @click="showQuestionDialog(getSubmission(review))">
          {{ getSubmission(review).title }}
        </div>
        <div class="col" @click="showQuestionDialog(getSubmission(review))">
          <v-chip small>
            <span> {{ getSubmission(review).creationDate }} </span>
          </v-chip>
        </div>
        <div class="col" @click="showQuestionDialog(getSubmission(review))">
          <v-chip small>
            <span>{{ review.creationDate }}</span>
          </v-chip>
        </div>
        <div class="col" @click="showQuestionDialog(getSubmission(review))">
          <v-chip :color="getStatusColor(review.status)" small>
            <span>{{ getReviewStatus(review) }}</span>
          </v-chip>
        </div>
        <div class="col">
          <v-btn
            small
            color="primary"
            data-cy="view"
            dark
            @click="showReviewDialog(review)"
            >View</v-btn
          >
        </div>
      </li>
      <li class="list-header" >
        <div class="col" style="color: white"><v-icon class="mr-2" color="white">mouse</v-icon>Left-click on question's title to view it.</div>
      </li>
    </ul>
    <show-review-dialog
      v-if="currentReview"
      :dialog="reviewDialog"
      :review="currentReview"
      v-on:close-show-review-dialog="onCloseShowReviewDialog"
    />
    <show-question-dialog
      v-if="currentQuestion"
      :dialog="questionDialog"
      :question="currentQuestion"
      v-on:close-show-question-dialog="onCloseShowQuestionDialog"
    />
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import { convertMarkDown } from '@/services/ConvertMarkdownService';
import RemoteServices from '@/services/RemoteServices';
import Review from '@/models/management/Review';
import Submission from '@/models/management/Submission';
import Image from '@/models/management/Image';
import ShowReviewDialog from '@/views/student/questions/ShowReviewDialog.vue';
import ShowQuestionDialog from '@/views/teacher/questions/ShowQuestionDialog.vue';
import Question from '@/models/management/Question';

@Component({
  components: {
    'show-question-dialog': ShowQuestionDialog,
    'show-review-dialog': ShowReviewDialog
  }
})
export default class ReviewView extends Vue {
  reviews: Review[] = [];
  submissions: Submission[] = [];
  currentReview: Review | null = null;
  reviewDialog: boolean = false;
  currentQuestion: Question | null = null;
  questionDialog: boolean = false;

  async created() {
    await this.$store.dispatch('loading');
    try {
      [this.reviews, this.submissions] = await Promise.all([
        RemoteServices.getSubmissionReviews(),
        RemoteServices.getSubmissions()
      ]);
      this.reviews.sort((a, b) => (a.creationDate < b.creationDate ? 1 : -1));
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  getSubmission(review: Review) {
    let submission = this.submissions.find(
      (submission: Submission) => submission.id == review.submissionId
    );

    if (submission) {
      return submission.questionDto;
    }
  }

  getReviewStatus(review: Review) {
    if (!review.status) {
      review.status = 'IN_REVIEW';
    }

    return review.status;
  }

  getStatusColor(status: string) {
    if (status === 'REJECTED') return 'red';
    else if (status === 'APPROVED') return 'green';
    else return 'orange';
  }

  showReviewDialog(review: Review) {
    this.currentReview = review;
    this.reviewDialog = false;
    this.reviewDialog = true;
  }

  onCloseShowReviewDialog() {
    this.reviewDialog = false;
  }

  showQuestionDialog(question: Question) {
    this.currentQuestion = question;
    this.questionDialog = true;
  }

  onCloseShowQuestionDialog() {
    this.questionDialog = false;
  }
}
</script>

<style lang="scss" scoped>
.container {
  max-width: 1000px;
  margin-left: auto;
  margin-right: auto;
  padding-left: 10px;
  padding-right: 10px;

  h2 {
    font-size: 26px;
    margin: 20px 0;
    text-align: center;
    small {
      font-size: 0.5em;
    }
  }

  ul {
    overflow: hidden;
    padding: 0 5px;

    li {
      border-radius: 3px;
      padding: 15px 10px;
      display: flex;
      justify-content: space-between;
      margin-bottom: 10px;
    }

    .list-header {
      background-color: #1976d2;
      color: white;
      font-size: 14px;
      text-transform: uppercase;
      letter-spacing: 0.03em;
      text-align: center;
    }

    .col {
      flex-basis: 25% !important;
      margin: auto; /* Important */
      text-align: center;
    }

    .list-row {
      background-color: #ffffff;
      box-shadow: 0 0 9px 0 rgba(0, 0, 0, 0.1);
      display: flex;
    }
  }
}
</style>
