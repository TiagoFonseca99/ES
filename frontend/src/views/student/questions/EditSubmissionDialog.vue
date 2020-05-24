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
                v-bind:data-cy="anonymous"
        />
        <v-spacer />
        <v-btn
          color="blue darken-1"
          @click="$emit('dialog', false)"
          data-cy="cancelButton"
          >Cancel</v-btn
        >
        <v-btn
          color="blue darken-1"
          @click="submitQuestion"
          data-cy="submitButton"
        >
          {{ oldQuestionId === null ? 'Submit' : 'Resubmit' }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { Component, Model, Prop, Vue, Watch } from 'vue-property-decorator';
import Question from '@/models/management/Question';
import RemoteServices from '@/services/RemoteServices';
import Submission from '@/models/management/Submission';

@Component
export default class EditSubmissionDialog extends Vue {
  @Model('dialog', Boolean) dialog!: boolean;
  @Prop({ type: Question, required: true }) readonly question!: Question;
  @Prop({ type: Submission, required: true }) readonly submission!: Submission;

  editQuestion!: Question;
  oldQuestionId: number | null = null;
  currentSubmission!: Submission;

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

  async submitQuestion() {
    if (
      this.editQuestion &&
      (!this.editQuestion.title || !this.editQuestion.content)
    ) {
      await this.$store.dispatch(
        'error',
        'Question must have title and content'
      );
      return;
    }

    try {
      this.currentSubmission.questionDto = this.editQuestion;
      this.currentSubmission.courseId = this.$store.getters.getCurrentCourse.courseId;
      const result =
        this.oldQuestionId != null
          ? await RemoteServices.resubmitQuestion(
              this.currentSubmission,
              this.oldQuestionId
            )
          : await RemoteServices.submitQuestion(this.currentSubmission);

      this.$emit('submit-question', result);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }
}
</script>
