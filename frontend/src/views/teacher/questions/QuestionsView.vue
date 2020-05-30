<template>
  <v-card class="table">
    <v-data-table
      :headers="headers"
      :custom-filter="customFilter"
      :items="items"
      :search="search"
      :sort-by="['creationDate']"
      sort-desc
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
            data-cy="searchQuestion"
          />

          <v-spacer />
          <v-btn
            color="primary"
            dark
            data-cy="filterDiscussions"
            @click="toggleFilter"
            >{{ filterLabel }}
          </v-btn>
          <v-btn color="primary" dark @click="newQuestion">New Question</v-btn>
          <v-btn color="primary" dark @click="exportCourseQuestions"
            >Export Questions</v-btn
          >
        </v-card-title>
      </template>

      <template v-slot:item.title="{ item }">
        <p
          @click="showQuestionDialog(item)"
          @contextmenu="editQuestion(item, $event)"
          data-cy="viewQuestion"
          style="cursor: pointer"
        >
          {{ item.title }}
        </p>
      </template>

      <template v-slot:item.topics="{ item }">
        <edit-question-topics
          :question="item"
          :topics="topics"
          v-on:question-changed-topics="onQuestionChangedTopics"
        />
      </template>

      <template v-slot:item.difficulty="{ item }">
        <v-chip
          v-if="item.difficulty"
          :color="getDifficultyColor(item.difficulty)"
          dark
          >{{ item.difficulty + '%' }}</v-chip
        >
      </template>

      <template v-slot:item.status="{ item }">
        <v-select
          v-model="item.status"
          :items="statusList"
          dense
          @change="setStatus(item.id, item.status)"
        >
          <template v-slot:selection="{ item }">
            <v-chip :color="getStatusColor(item)" small>
              <span>{{ item }}</span>
            </v-chip>
          </template>
        </v-select>
      </template>

      <template v-slot:item.image="{ item }">
        <v-file-input
          show-size
          dense
          small-chips
          @change="handleFileUpload($event, item)"
          accept="image/*"
        />
      </template>

      <template v-slot:item.action="{ item }">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon
              large
              class="mr-2"
              v-on="on"
              @click="showQuestionDialog(item)"
              data-cy="showQuestionDialog"
              >visibility</v-icon
            >
          </template>
          <span>Show Question</span>
        </v-tooltip>
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon
              large
              class="mr-2"
              v-on="on"
              @click="duplicateQuestion(item)"
              >cached</v-icon
            >
          </template>
          <span>Duplicate Question</span>
        </v-tooltip>
        <v-tooltip bottom v-if="item.numberOfAnswers === 0">
          <template v-slot:activator="{ on }">
            <v-icon large class="mr-2" v-on="on" @click="editQuestion(item)"
              >edit</v-icon
            >
          </template>
          <span>Edit Question</span>
        </v-tooltip>

        <v-tooltip bottom v-if="item.numberOfAnswers === 0">
          <template v-slot:activator="{ on }">
            <v-icon
              large
              class="mr-2"
              v-on="on"
              @click="deleteQuestion(item)"
              color="red"
              data-cy="deleteQuestion"
              >delete</v-icon
            >
          </template>
          <span>Delete Question</span>
        </v-tooltip>
      </template>
    </v-data-table>
    <footer>
      <v-icon class="mr-2">mouse</v-icon>Left-click on question's title to view
      it. <v-icon class="mr-2">mouse</v-icon>Right-click on question's title to
      edit it.
    </footer>
    <edit-question-dialog
      v-if="currentQuestion"
      v-model="editQuestionDialog"
      :question="currentQuestion"
      v-on:save-question="onSaveQuestion"
    />
    <show-question-dialog
      v-if="currentQuestion"
      v-model="questionDialog"
      :question="currentQuestion"
      :discussion="discussion"
      v-on:close-show-question-dialog="onCloseShowQuestionDialog"
      v-on:submittedDiscussion="updateQuestion"
    />
  </v-card>
</template>

<script lang="ts">
import { Component, Vue, Watch } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import Question from '@/models/management/Question';
import Image from '@/models/management/Image';
import Topic from '@/models/management/Topic';
import ShowQuestionDialog from '@/views/teacher/questions/ShowQuestionDialog.vue';
import EditQuestionDialog from '@/views/teacher/questions/EditQuestionDialog.vue';
import EditQuestionTopics from '@/views/teacher/questions/EditQuestionTopics.vue';

enum FilterState {
  DISCUSSION = 'See questions with unanswered discussions',
  NO_ANSWER = 'See all questions',
  ALL = 'See questions with discussion'
}

@Component({
  components: {
    'show-question-dialog': ShowQuestionDialog,
    'edit-question-dialog': EditQuestionDialog,
    'edit-question-topics': EditQuestionTopics
  }
})
export default class QuestionsView extends Vue {
  items: Question[] = [];
  questions: Question[] = [];
  topics: Topic[] = [];
  currentQuestion: Question | null = null;
  editQuestionDialog: boolean = false;
  questionDialog: boolean = false;
  search: string = '';
  statusList = ['DISABLED', 'AVAILABLE', 'REMOVED'];
  filterLabel: FilterState = FilterState.ALL;
  discussion: boolean = true;

  headers: object = [
    {
      text: 'Actions',
      value: 'action',
      align: 'left',
      width: '15%',
      sortable: false
    },
    { text: 'Title', value: 'title', align: 'left' },
    { text: 'Status', value: 'status', align: 'left', width: '150px' },
    {
      text: 'Topics',
      value: 'topics',
      align: 'center',
      sortable: false
    },
    {
      text: 'Image',
      value: 'image',
      align: 'center',
      sortable: false
    },
    { text: 'Difficulty', value: 'difficulty', align: 'center' },
    { text: 'Answers', value: 'numberOfAnswers', align: 'center' },
    {
      text: 'Nº of generated quizzes',
      value: 'numberOfGeneratedQuizzes',
      align: 'center'
    },
    {
      text: 'Nº of non generated quizzes',
      value: 'numberOfNonGeneratedQuizzes',
      align: 'center'
    },
    {
      text: 'Creation Date',
      value: 'creationDate',
      align: 'center'
    }
  ];

  @Watch('editQuestionDialog')
  closeError() {
    if (!this.editQuestionDialog) {
      this.currentQuestion = null;
    }
  }

  async created() {
    await this.$store.dispatch('loading');
    try {
      [this.topics, this.questions] = await Promise.all([
        RemoteServices.getTopics(),
        RemoteServices.getQuestions()
      ]);
      this.questions = this.questions.filter(q =>
        this.statusList.includes(q.status)
      );
    } catch (error) {
      await this.$store.dispatch('error', error);
    }

    this.discussionFilter();

    await this.$store.dispatch('clearLoading');
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

  onQuestionChangedTopics(questionId: Number, changedTopics: Topic[]) {
    let question = this.questions.find(
      (question: Question) => question.id == questionId
    );
    if (question) {
      question.topics = changedTopics;
      this.discussionFilter();
    }
  }

  getDifficultyColor(difficulty: number) {
    if (difficulty < 25) return 'red';
    else if (difficulty < 50) return 'orange';
    else if (difficulty < 75) return 'lime';
    else return 'green';
  }

  async setStatus(questionId: number, status: string) {
    try {
      await RemoteServices.setQuestionStatus(questionId, status);
      let question = this.questions.find(
        question => question.id === questionId
      );
      if (question) {
        question.status = status;
      }
    } catch (error) {
      await this.$store.dispatch('error', error);
    }

    this.discussionFilter();
  }

  getStatusColor(status: string) {
    if (status === 'REMOVED') return 'red';
    else if (status === 'DISABLED') return 'orange';
    else return 'green';
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

      this.discussionFilter();
    }
  }

  showQuestionDialog(question: Question) {
    this.currentQuestion = question;
    this.questionDialog = true;
    this.discussion = true;
  }

  onCloseShowQuestionDialog() {
    this.currentQuestion = null;
    this.questionDialog = false;
  }

  newQuestion() {
    this.currentQuestion = new Question();
    this.editQuestionDialog = true;
    this.discussion = false;
    this.discussionFilter();
  }

  editQuestion(question: Question, e?: Event) {
    if (e) e.preventDefault();
    this.currentQuestion = question;
    this.discussion = false;
    this.editQuestionDialog = true;
    this.discussionFilter();
  }

  duplicateQuestion(question: Question) {
    this.currentQuestion = new Question(question);
    this.currentQuestion.id = null;
    this.currentQuestion.options.forEach(option => {
      option.id = null;
    });
    this.currentQuestion.image = null;
    this.editQuestionDialog = true;
    this.discussion = false;
    this.discussionFilter();
  }

  async onSaveQuestion(question: Question) {
    this.questions = this.questions.filter(q => q.id !== question.id);
    this.questions.unshift(question);
    this.editQuestionDialog = false;
    this.currentQuestion = null;
    this.discussionFilter();
  }

  async exportCourseQuestions() {
    let fileName = this.$store.getters.getCurrentCourse.name + '-Questions.zip';
    try {
      let result = await RemoteServices.exportCourseQuestions();
      const url = window.URL.createObjectURL(result);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', fileName);
      document.body.appendChild(link);
      link.click();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }

  async deleteQuestion(toDeletequestion: Question) {
    if (
      toDeletequestion.id &&
      confirm('Are you sure you want to delete this question?')
    ) {
      try {
        await RemoteServices.deleteQuestion(toDeletequestion.id);
        this.questions = this.questions.filter(
          question => question.id != toDeletequestion.id
        );
        this.discussionFilter();
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
    }
  }

  @Watch('filterLabel')
  discussionFilter() {
    if (this.filterLabel == FilterState.DISCUSSION) {
      this.items = this.questions.filter(question => {
        return question.hasDiscussions;
      });
    } else if (this.filterLabel == FilterState.NO_ANSWER) {
      this.items = this.questions.filter(question => {
        return question.hasDiscussions && !question.hasAllReplies;
      });
    } else {
      this.items = this.questions;
    }
  }

  toggleFilter() {
    switch (this.filterLabel) {
      case FilterState.ALL:
        this.filterLabel = FilterState.DISCUSSION;
        break;
      case FilterState.DISCUSSION:
        this.filterLabel = FilterState.NO_ANSWER;
        break;
      case FilterState.NO_ANSWER:
      default:
        this.filterLabel = FilterState.ALL;
    }
  }

  updateQuestion(allReplies: boolean) {
    this.currentQuestion!.hasAllReplies = allReplies;
    this.discussionFilter();
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
