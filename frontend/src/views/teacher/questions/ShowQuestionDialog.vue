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
        <v-btn dark color="blue darken-1" @click="$emit('dialog')">close</v-btn>
      </v-card-actions>
      <reply-component :discussions="discussions" />
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { Component, Vue, Prop, Model, Watch } from 'vue-property-decorator';
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
  discussions: Discussion[] = [];

  async created() {
    await this.getDiscussions();
  }

  @Watch('question')
  async getDiscussions() {
    try {
      [this.discussions] = await Promise.all([
        RemoteServices.getDiscussionsByQuestion(this.question.id!)
      ]);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }
}
</script>
