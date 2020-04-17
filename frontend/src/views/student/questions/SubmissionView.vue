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
            color="primary"
            dark
            @click="submitQuestion"
            data-cy="submitQuestion"
            >Submit Question</v-btn
          >
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
        <v-chip :color="getStatusColor(item.questionDto.status)" small>
          <span>{{ item.questionDto.status }}</span>
        </v-chip>
      </template>
      <template v-slot:item.questionDto.creationDate="{ item }">
        <v-chip small>
          <span> {{ item.questionDto.creationDate }}</span>
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
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon
              small
              class="mr-2"
              v-on="on"
              @click="showQuestionDialog(item.questionDto)"
              >visibility</v-icon
            >
          </template>
          <span>Show Question</span>
        </v-tooltip>
      </template>
    </v-data-table>
    <edit-submission-dialog
      v-if="currentQuestion"
      v-model="editSubmissionDialog"
      :question="currentQuestion"
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
import Image from '@/models/management/Image';
import ShowQuestionDialog from '@/views/teacher/questions/ShowQuestionDialog.vue';
import EditSubmissionDialog from '@/views/student/questions/EditSubmissionDialog.vue';
@Component({
  components: {
    'show-question-dialog': ShowQuestionDialog,
    'edit-submission-dialog': EditSubmissionDialog
  }
})
export default class SubmissionView extends Vue {
  submissions: Submission[] = [];
  currentQuestion: Question | null = null;
  editSubmissionDialog: boolean = false;
  questionDialog: boolean = false;
  search: string = '';

  headers: object = [
    { text: 'Title', value: 'questionDto.title', align: 'center' },
    { text: 'Question', value: 'questionDto.content', align: 'left' },
    { text: 'Status', value: 'questionDto.status', align: 'center' },
    {
      text: 'Creation Date',
      value: 'questionDto.creationDate',
      align: 'center'
    },
    {
      text: 'Image',
      value: 'questionDto.image',
      align: 'center',
      sortable: false
    },
    {
      text: 'Actions',
      value: 'action',
      align: 'center',
      sortable: false
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
      [this.submissions] = await Promise.all([RemoteServices.getSubmissions()]);
      this.submissions.sort((a, b) => this.sortNewestFirst(a, b));
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
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

  showQuestionDialog(question: Question) {
    this.currentQuestion = question;
    this.questionDialog = true;
  }

  onCloseShowQuestionDialog() {
    this.questionDialog = false;
  }

  submitQuestion() {
    this.currentQuestion = new Question();
    this.editSubmissionDialog = true;
  }

  getStatusColor(status: string) {
    if (status === 'AVAILABLE') return 'green';
    else return 'pink';
  }

  async onSaveQuestion(submission: Submission) {
    this.submissions = this.submissions.filter(s => s.id !== submission.id);
    this.submissions.unshift(submission);
    this.editSubmissionDialog = false;
    this.currentQuestion = null;
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
