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
          {{ 'Give Argument' }}
        </span>
      </v-card-title>

      <v-card-text class="text-left" v-if="editSubmission">
        <v-container grid-list-md fluid>
          <v-layout column wrap>
            <v-flex xs24 sm12 md12>
              <v-textarea
                outline
                rows="10"
                v-model="editSubmission.argument"
                data-cy="Argument"
                label="Argument"
              ></v-textarea>
            </v-flex>
          </v-layout>
        </v-container>
      </v-card-text>

      <v-card-actions>
        <v-spacer />
        <v-btn color="blue darken-1" data-cy="Submit" @click="submitQuestion"
          >Submit</v-btn
        >
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>
<script lang="ts">
import { Component, Model, Prop, Vue } from 'vue-property-decorator';
import Submission from '@/models/management/Submission';
import RemoteServices from '@/services/RemoteServices';

@Component
export default class GiveArgument extends Vue {
  @Prop({ type: Submission, required: true }) submission!: Submission;
  @Prop({ type: Number, required: false }) oldQuestionId!: number;
  @Model('dialog', Boolean) dialog!: boolean;

  editSubmission!: Submission;

  async created() {
    this.editSubmission = this.submission;
  }

  async submitQuestion() {
    try {
      const result =
        this.oldQuestionId != null
          ? await RemoteServices.resubmitQuestion(
              this.editSubmission,
              this.oldQuestionId
            )
          : await RemoteServices.submitQuestion(this.editSubmission);

      this.$emit('argument-given', result);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }
}
</script>

<style lang="scss" scoped></style>
