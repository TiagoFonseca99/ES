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
          {{ oldQuestionId === null ? 'New Submission' : 'Edit Submission' }}
        </span>
      </v-card-title>

      <v-card-text class="text-left" v-if="editQuestion">
        <v-container grid-list-md fluid>
          <v-layout column wrap>
            <v-flex xs24 sm12 md8>
              <v-text-field
                v-model="editQuestion.title"
                label="Title"
                data-cy="QuestionTitle"
              />
            </v-flex>
            <v-flex xs24 sm12 md12>
              <v-textarea
                outline
                rows="10"
                v-model="editQuestion.content"
                label="Content"
                data-cy="QuestionContent"
              ></v-textarea>
            </v-flex>
            <v-flex
              xs24
              sm12
              md12
              v-for="index in editQuestion.options.length"
              :key="index"
            >
              <v-switch
                v-model="editQuestion.options[index - 1].correct"
                class="ma-4"
                label="Correct"
                v-bind:data-cy="'Switch' + index"
              />
              <v-textarea
                outline
                rows="10"
                v-model="editQuestion.options[index - 1].content"
                label="Content"
                v-bind:data-cy="'Option' + index"
              ></v-textarea>
            </v-flex>
          </v-layout>
        </v-container>
      </v-card-text>

      <v-card-actions>
        <v-switch
          v-model="currentSubmission.anonymous"
          class="ma-4"
          label="Anonymous"
        />
        <v-spacer />
        <v-btn
          color="blue darken-1"
          @click="$emit('dialog', false)"
          data-cy="cancelButton"
          >Cancel</v-btn
        >
        <v-btn
          @click="menuArgument"
          color="blue darken-1"
          data-cy="submitButton"
        >
          {{ oldQuestionId === null ? 'Submit' : 'Resubmit' }}
        </v-btn>
      </v-card-actions>
      <menu-argument
        v-if="currentSubmission"
        v-model="MenuArgument"
        :submission="currentSubmission"
        :number="oldQuestionId"
        v-on:no-changes="onSaveSubmission"
        />
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { Component, Model, Prop, Vue, Watch } from 'vue-property-decorator';
import Question from '@/models/management/Question';
import Submission from '@/models/management/Submission';
import MenuArgument from '@/views/student/questions/MenuArgument.vue';

@Component({
  components: {
    'menu-argument': MenuArgument
  }
})
export default class EditSubmissionDialog extends Vue {
  @Model('dialog', Boolean) dialog!: boolean;
  @Prop({ type: Question, required: true }) readonly question!: Question;
  @Prop({ type: Submission, required: true }) readonly submission!: Submission;

  editQuestion!: Question;
  oldQuestionId: number | null = null;
  currentSubmission!: Submission;
  MenuArgument: boolean = false;

  created() {
    this.updateSubmission();
  }

  @Watch('question', { immediate: true, deep: true })
  updateSubmission() {
    this.editQuestion = new Question(this.question);
    if (this.editQuestion.id != null) {
      this.oldQuestionId = this.editQuestion.id;
      this.editQuestion.id = null;
    }
    this.currentSubmission = new Submission(this.submission);
  }

  async menuArgument() {
    if (this.editQuestion && (!this.editQuestion.title || !this.editQuestion.content)) {
      await this.$store.dispatch('error',
              'Question must have title and content'
      );
      return;
    }
    this.currentSubmission.questionDto = this.editQuestion;
    this.currentSubmission.courseId = this.$store.getters.getCurrentCourse.courseId;
    this.MenuArgument = true;
  }

  async onSaveSubmission() {
    this.MenuArgument = false;
    this.$emit('submit-question');
    await this.$store.dispatch('clearLoading');
  }
}
</script>
