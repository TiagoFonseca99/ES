<template>
  <v-card class="table">
    <v-data-table
      :headers="headers"
      :custom-filter="customFilter"
      :items="submissions"
      :search="search"
      multi-sort
      :mobile-breakpoint="0"
      :items-per-page="15"
      :footer-props="{ itemsPerPageOptions: [15, 30, 50, 100] }"
    >
      <template v-slot:top>
        <v-card-title>
          <v-text-field
            v-model="search"
            append-icon="search"
            label="Search"
            class="mx-2"
          />

          <v-spacer />
          <v-btn
            to="/student/reviews"
            color="primary"
            dark
            data-cy="submissionsStatus"
          >
            Submissions Status
          </v-btn>
          <v-btn
            color="primary"
            dark
            @click="submitQuestion"
            data-cy="submitQuestion"
            >Submit Question</v-btn
          >
        </v-card-title>
      </template>

      <template v-slot:item.questionDto.title="{ item }">
        <p
          v-html="
            convertMarkDown(item.questionDto.title, item.questionDto.image)
          "
          @click="showQuestionDialog(item.questionDto)"
          style="cursor: pointer"
      /></template>

      <template v-slot:item.questionDto.status="{ item }">
        <v-chip :color="getStatusColor(item.questionDto.status)" small>
          <span>{{ item.questionDto.status }}</span>
        </v-chip>
      </template>
      <template v-slot:item.questionDto.creationDate="{ item }">
        <v-chip small>
          <span> {{ item.questionDto.creationDate }}</span>
        </v-chip>
      </template>
      <template v-slot:item.topics="{ item }">
        <edit-submission-topics
          v-if="item.questionDto.status === 'SUBMITTED'"
          :submission="item"
          :topics="topics"
          v-on:submission-changed-topics="onSubmissionChangedTopics"
        />
        <view-submission-topics v-else :submission="item" :topics="topics" />
      </template>

      <template v-slot:item.action="{ item }">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon
              large
              class="mr-2"
              v-on="on"
              @click="showQuestionDialog(item.questionDto)"
              data-cy="viewQuestion"
              >visibility</v-icon
            >
          </template>
          <span>Show Question</span>
        </v-tooltip>
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon
              v-if="item.questionDto.status !== 'AVAILABLE'"
              large
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
    <footer>
      <v-icon class="mr-2">mouse</v-icon>Left-click on question's title to view
      it.
    </footer>
    <edit-submission-dialog
      v-if="currentQuestion && currentSubmission"
      v-model="editSubmissionDialog"
      :question="currentQuestion"
      :submission="currentSubmission"
      v-on:submit-question="onSaveQuestion"
    />
    <show-question-dialog
      v-if="currentQuestion"
      v-model="questionDialog"
      :question="currentQuestion"
      v-on:close-show-question-dialog="onCloseShowQuestionDialog"
    />
  </v-card>
</template>

<script lang="ts">
import { Component, Vue, Watch } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import { convertMarkDown } from '@/services/ConvertMarkdownService';
import Question from '@/models/management/Question';
import Submission from '@/models/management/Submission';
import Topic from '@/models/management/Topic';
import Image from '@/models/management/Image';
import EditSubmissionTopics from '@/views/student/questions/EditSubmissionTopics.vue';
import ShowQuestionDialog from '@/views/student/questions/ShowQuestionDialog.vue';
import EditSubmissionDialog from '@/views/student/questions/EditSubmissionDialog.vue';
import ViewSubmissionTopics from '@/views/student/questions/ViewSubmissionTopics.vue';

@Component({
  components: {
    'show-question-dialog': ShowQuestionDialog,
    'edit-submission-topics': EditSubmissionTopics,
    'view-submission-topics': ViewSubmissionTopics,
    'edit-submission-dialog': EditSubmissionDialog
  }
})
export default class SubmissionView extends Vue {
  submissions: Submission[] = [];
  topics: Topic[] = [];
  currentQuestion: Question | null = null;
  currentSubmission: Submission | null = null;
  editSubmissionDialog: boolean = false;
  questionDialog: boolean = false;
  search: string = '';

  headers: object = [
    {
      text: 'Actions',
      value: 'action',
      align: 'left',
      width: '15%',
      sortable: false
    },
    { text: 'Title', value: 'questionDto.title', align: 'center' },
    { text: 'Status', value: 'questionDto.status', align: 'center' },
    {
      text: 'Topics',
      value: 'topics',
      align: 'center',
      sortable: false,
      width: '20%'
    },
    {
      text: 'Creation Date',
      value: 'questionDto.creationDate',
      align: 'center'
    }
  ];

  @Watch('editSubmissionDialog')
  closeError() {
    if (!this.editSubmissionDialog) {
      this.currentQuestion = null;
    }
  }

  async created() {
    await this.$store.dispatch('loading');
    try {
      [this.submissions, this.topics] = await Promise.all([
        RemoteServices.getSubmissions(),
        RemoteServices.getTopics()
      ]);
      this.submissions.sort((a, b) => this.sortNewestFirst(a, b));
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  onSubmissionChangedTopics(questionId: Number, changedTopics: Topic[]) {
    let submission = this.submissions.find(
      (submission: Submission) => submission.questionDto.id == questionId
    );
    if (submission) {
      submission.questionDto.topics = changedTopics;
    }
  }

  sortNewestFirst(a: Submission, b: Submission) {
    if (a.questionDto.creationDate && b.questionDto.creationDate)
      return a.questionDto.creationDate < b.questionDto.creationDate ? 1 : -1;
    else return 0;
  }

  customFilter(value: string, search: string, question: Question) {
    // noinspection SuspiciousTypeOfGuard,SuspiciousTypeOfGuard
    return (
      search != null &&
      JSON.stringify(question)
        .toLowerCase()
        .indexOf(search.toLowerCase()) !== -1
    );
  }

  convertMarkDown(text: string, image: Image | null = null): string {
    return convertMarkDown(text, image);
  }

  showQuestionDialog(question: Question) {
    this.currentQuestion = question;
    this.questionDialog = true;
  }

  onCloseShowQuestionDialog() {
    this.questionDialog = false;
  }

  submitQuestion() {
    this.currentQuestion = new Question();
    this.currentQuestion.status = 'SUBMITTED';
    this.currentSubmission = new Submission();
    this.currentSubmission.courseExecutionId = this.$store.getters.getCurrentCourse.courseExecutionId;
    this.editSubmissionDialog = true;
  }

  getStatusColor(status: string) {
    if (status === 'AVAILABLE') return 'green';
    else if (status === 'DEPRECATED') return 'red';
    else return 'pink';
  }

  async onSaveQuestion() {
    await this.$store.dispatch('loading');
    try {
      [this.submissions, this.topics] = await Promise.all([
        RemoteServices.getSubmissions(),
        RemoteServices.getTopics()
      ]);
      this.submissions.sort((a, b) => this.sortNewestFirst(a, b));
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
    this.editSubmissionDialog = false;
    this.currentQuestion = null;
    this.currentSubmission = null;
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
          submission => submission.questionDto.id != questionId
        );
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.question-textarea {
  text-align: left;

  .CodeMirror,
  .CodeMirror-scroll {
    min-height: 200px !important;
  }
}
.option-textarea {
  text-align: left;

  .CodeMirror,
  .CodeMirror-scroll {
    min-height: 100px !important;
  }
}
</style>
