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
          {{ 'Change Question' }}
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
        <v-spacer />
        <v-btn
          color="blue darken-1"
          @click="cancelButton"
          data-cy="cancelButton1"
          >Cancel</v-btn
        >
        <v-btn
          color="blue darken-1"
          @click="changeQuestion"
          data-cy="submitButton"
          >Submit</v-btn
        >
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">

import {Component, Model, Prop, Vue} from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import Question from '@/models/management/Question';
import Submission from '@/models/management/Submission';

@Component
export default class MenuChangeSubmission extends Vue {
  @Prop({ type: Question, required: true }) question!: Question;
  @Prop({ type: Submission, required: true }) submission!: Submission;
  @Model('dialog', Boolean) dialog!: boolean;

  editQuestion!: Question;
  editSubmission!: Submission;

  async created() {
    this.editQuestion = this.question;
    this.editSubmission = this.submission;
  }

  async changeQuestion() {
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
        this.editSubmission.questionDto = this.editQuestion;
        this.editSubmission.courseId = this.$store.getters.getCurrentCourse.courseId;
        await RemoteServices.changeSubmission(this.editSubmission);
        this.$emit('change-sub');
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
    }

    cancelButton() {
      this.$emit('dialog', false);
      this.$emit('change-sub');
    }
}

</script>

<style lang="scss" scoped></style>