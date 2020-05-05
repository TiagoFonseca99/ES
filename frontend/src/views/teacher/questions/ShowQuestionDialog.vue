<template>
  <v-dialog
    :value="dialog"
    @input="$emit('dialog', false)"
    @keydown.esc="$emit('dialog', false)"
    max-width="75%"
  >
    <v-card>
      <v-card-title>
        <span class="headline">{{ question.title }}</span>
      </v-card-title>

      <v-card-text class="text-left">
        <show-question :question="question" />
      </v-card-text>

      <v-card-actions>
        <v-spacer />
        <v-btn
          data-cy="close"
          dark
          color="blue darken-1"
          @click="$emit('dialog')"
          >close</v-btn
        >
      </v-card-actions>
      <reply-component
        v-if="this.$store.getters.isTeacher && discussion"
        :discussions="discussions"
        v-on:submit="submittedDiscussion"
      />
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import {
  Component,
  Vue,
  Prop,
  Model,
  Watch,
  Emit
} from 'vue-property-decorator';
import Question from '@/models/management/Question';
import ShowQuestion from '@/views/teacher/questions/ShowQuestion.vue';
import ReplyComponent from '@/views/teacher/questions/ReplyComponent.vue';
import Discussion from '@/models/management/Discussion';
import RemoteServices from '@/services/RemoteServices';

@Component({
  components: {
    'show-question': ShowQuestion,
    'reply-component': ReplyComponent
  }
})
export default class ShowQuestionDialog extends Vue {
  @Model('dialog', Boolean) dialog!: boolean;
  @Prop({ type: Question, required: true }) readonly question!: Question;
  @Prop({ type: Boolean, required: true }) readonly discussion!: Boolean;
  discussions: Discussion[] = [];

  async created() {
    if (this.$store.getters.isTeacher) {
      await this.getDiscussions();
    }
  }

  @Watch('question')
  async getDiscussions() {
    if (this.$store.getters.isTeacher && this.discussion) {
      try {
        [this.discussions] = await Promise.all([
          RemoteServices.getDiscussionsByQuestion(this.question.id!)
        ]);
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
    }
  }

  @Emit('submittedDiscussion')
  submittedDiscussion(all: boolean) {
    return all;
  }
}
</script>
