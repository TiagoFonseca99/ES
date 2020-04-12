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
          {{ 'Submit Question' }}
        </span>
      </v-card-title>

      <v-card-text class="text-left" v-if="editQuestion">
        <v-container grid-list-md fluid>
          <v-layout column wrap>
            <v-flex xs24 sm12 md8>
              <v-text-field v-model="editQuestion.title" label="Title" />
            </v-flex>
            <v-flex xs24 sm12 md12>
              <v-textarea
                outline
                rows="10"
                v-model="editQuestion.content"
                label="Content"
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
              />
              <v-textarea
                outline
                rows="10"
                v-model="editQuestion.options[index - 1].content"
                label="Content"
              ></v-textarea>
            </v-flex>
          </v-layout>
        </v-container>
      </v-card-text>

      <v-card-actions>
        <v-spacer />
        <v-btn color="blue darken-1" @click="$emit('dialog', false)"
          >Cancel</v-btn
        >
        <v-btn color="blue darken-1" @click="submitQuestion">Submit</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { Component, Model, Prop, Vue } from 'vue-property-decorator';
import Question from '@/models/management/Question';
import RemoteServices from '@/services/RemoteServices';
import Submission from '@/models/management/Submission';

@Component
export default class EditSubmissionDialog extends Vue {
  @Model('dialog', Boolean) dialog!: boolean;
  @Prop({ type: Question, required: true }) readonly question!: Question;

  editQuestion!: Question;
  currentSubmission!: Submission;

  created() {
    this.editQuestion = new Question(this.question);
    this.editQuestion.status = 'SUBMITTED';
    this.currentSubmission = new Submission();
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
      const result = await RemoteServices.submitQuestion(
        this.currentSubmission
      );
      this.$emit('submit-question', result);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }
}
</script>
