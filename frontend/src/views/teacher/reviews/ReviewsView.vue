<template>
  <div>
    <h2>Student Submissions</h2>
    <v-card class="table">
      <v-data-table
        :headers="headers"
        :search="searchSubmissions"
        :items="submissions.filter(s => s.questionDto.status === 'SUBMITTED')"
        :mobile-breakpoint="0"
        :items-per-page="4"
        multi-sort
      >
        <template>
          <p>Title</p>
        </template>
        <template v-slot:top>
          <v-card-title>
            <v-text-field
              v-model="searchSubmissions"
              append-icon="search"
              label="Search"
              data-cy="Search"
              class="mx-2"
            />
          </v-card-title>
        </template>
        <template v-slot:item.questionDto.content="{ item }">
          <p
            v-html="
              convertMarkDown(item.questionDto.content, item.questionDto.image)
            "
            @click="showQuestionDialog(item.questionDto)"
        /></template>
        <template v-slot:item.questionDto.status="{ item }">
          <v-chip color="pink" small>
            <span>{{ item.questionDto.status }}</span>
          </v-chip>
        </template>
        <template v-slot:item.questionDto.creationDate="{ item }">
          <v-chip small>
            <span>{{ item.questionDto.creationDate }}</span>
          </v-chip>
        </template>
        <template v-slot:item.questionDto.image="{ item }">
          <v-file-input
            show-size
            dense
            small-chips
            @change="handleFileUpload($event, item.questionDto)"
            accept="image/*"
          />
        </template>
        <template v-slot:item.action="{ item }">
          <v-btn
            color="primary"
            small
            @click="createReview(item)"
            data-cy="createReview"
          >
            Create
          </v-btn>
          <v-tooltip bottom>
            <template v-slot:activator="{ on }">
              <v-icon
                small
                class="mr-2"
                v-on="on"
                @click="deleteSubmission(item)"
                color="red"
                data-cy="deleteSubmission"
                >delete</v-icon
              >
            </template>
            <span>Delete Question</span>
          </v-tooltip>
        </template>
      </v-data-table>
      <edit-reviews
        v-if="currentSubmission"
        v-model="editReview"
        :submission="currentSubmission"
        v-on:create-review="onSaveReview"
      />
    </v-card>
    <h2>Reviews</h2>
    <v-card class="table">
      <v-data-table
        :headers="headers2"
        :search="searchReviews"
        :items="reviews"
        :mobile-breakpoint="0"
        :items-per-page="5"
        multi-sort
      >
        <template v-slot:top>
          <v-card-title>
            <v-text-field
              v-model="searchReviews"
              append-icon="search"
              label="Search"
              class="mx-2"
            />
          </v-card-title>
        </template>
        <template v-slot:item.questionDto.title="{ item }">
          <span> {{ getSubmission(item).title }}</span>
        </template>
        <template v-slot:item.questionDto.content="{ item }">
          <p
            v-html="
              convertMarkDown(
                getSubmission(item).content,
                getSubmission(item).image
              )
            "
            @click="showQuestionDialog(getSubmission(item))"
          >
            <span> {{ getSubmission(item).content }}</span>
          </p>
        </template>
        <template v-slot:item.questionDto.creationDate="{ item }">
          <v-chip small>
            <span> {{ getSubmission(item).creationDate }}</span>
          </v-chip>
        </template>
        <template v-slot:item.status="{ item }">
          <v-chip :color="changeColor(item.status)" small>
            <span>{{ item.status }}</span>
          </v-chip>
        </template>
        <template v-slot:item.justication="{ item }">
          <v-chip small>
            <span>{{ item.justification }}</span>
          </v-chip>
        </template>
      </v-data-table>
    </v-card>
    <show-question-dialog
      v-if="currentQuestion"
      v-model="questionDialog"
      :question="currentQuestion"
      v-on:close-show-question-dialog="onCloseShowQuestionDialog"
    />
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import { convertMarkDown } from '@/services/ConvertMarkdownService';
import Submission from '@/models/management/Submission';
import RemoteServices from '@/services/RemoteServices';
import Question from '@/models/management/Question';
import Image from '@/models/management/Image';
import EditReview from '@/views/teacher/reviews/EditReview.vue';
import Review from '@/models/management/Review';
import ShowQuestionDialog from '@/views/teacher/questions/ShowQuestionDialog.vue';

@Component({
  components: {
    'show-question-dialog': ShowQuestionDialog,
    'edit-reviews': EditReview
  }
})
export default class ReviewsView extends Vue {
  submissions: Submission[] = [];
  currentSubmission: Submission | null = null;
  editReview: boolean = false;
  searchSubmissions: string = '';
  currentQuestion: Question | null = null;
  questionDialog: boolean = false;

  headers: object = [
    { text: 'Title', value: 'questionDto.title', align: 'center' },
    { text: 'Question', value: 'questionDto.content', align: 'left' },
    {
      text: 'Creation Date',
      value: 'questionDto.creationDate',
      align: 'center'
    },
    { text: 'Status', value: 'questionDto.status', align: 'center' },
    { text: 'Image', value: 'questionDto.image', align: 'center' },
    { text: 'Review', value: 'action', align: 'center' }
  ];
  searchReviews: string = '';
  reviews: Review[] = [];
  headers2: object = [
    { text: 'Title', value: 'questionDto.title', align: 'center' },
    { text: 'Question', value: 'questionDto.content', align: 'left' },
    {
      text: 'Creation Date',
      value: 'questionDto.creationDate',
      align: 'center'
    },
    { text: 'Status', value: 'status', align: 'center' },
    { text: 'Justification', value: 'justification', align: 'center' }
  ];

  async created() {
    await this.$store.dispatch('loading');
    try {
      [this.submissions] = await Promise.all([
        RemoteServices.getSubsToTeacher()
      ]);
      this.submissions.sort((a, b) => this.sortNewestSubmissionFirst(a, b));
      [this.reviews] = await Promise.all([
        RemoteServices.getReviewsToTeacher()
      ]);
      this.reviews.sort((a, b) => (a.creationDate < b.creationDate ? 1 : -1));
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  async handleFileUpload(event: File, question: Question) {
    if (question.id) {
      try {
        const imageURL = await RemoteServices.uploadImage(event, question.id);
        question.image = new Image();
        question.image.url = imageURL;
        confirm('Image ' + imageURL + ' was uploaded!');
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
    }
  }

  changeColor(status: string) {
    if (status == 'REJECTED') return 'red';
    if (status == 'APPROVED') return 'green';
  }

  createReview(submission: Submission) {
    this.currentSubmission = submission;
    this.editReview = true;
  }

  async onSaveReview() {
    this.editReview = false;
    this.currentSubmission = null;
    this.reviews = [];
    try {
      [this.submissions] = await Promise.all([
        RemoteServices.getSubsToTeacher()
      ]);
      [this.reviews] = await Promise.all([
        RemoteServices.getReviewsToTeacher()
      ]);

      this.reviews.sort((a, b) => (a.creationDate < b.creationDate ? 1 : -1));
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  sortNewestSubmissionFirst(a: Submission, b: Submission) {
    if (a.questionDto.creationDate && b.questionDto.creationDate)
      return a.questionDto.creationDate < b.questionDto.creationDate ? 1 : -1;
    else return 0;
  }
  showQuestionDialog(question: Question) {
    this.currentQuestion = question;
    this.questionDialog = true;
  }

  onCloseShowQuestionDialog() {
    this.questionDialog = false;
  }

  convertMarkDown(text: string, image: Image | null = null): string {
    return convertMarkDown(text, image);
  }

  getSubmission(review: Review) {
    let submission = this.submissions.find(
      (submission: Submission) => submission.id == review.submissionId
    );

    if (submission) {
      return submission.questionDto;
    }
  }

  async deleteSubmission(toDeletesubmission: Submission) {
    if (
      toDeletesubmission.id &&
      confirm('Are you sure you want to delete this question?')
    ) {
      try {
        let questionId = toDeletesubmission.questionDto.id;
        if (questionId != null) await RemoteServices.deleteQuestion(questionId);
        this.submissions = this.submissions.filter(
          submission => submission.id != toDeletesubmission.id
        );
        this.reviews = this.reviews.filter(
          review => review.submissionId != toDeletesubmission.id
        );
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
    }
  }
}
</script>

<style lang="scss" scoped></style>
