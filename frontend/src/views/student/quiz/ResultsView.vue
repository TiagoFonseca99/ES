<template>
  <div class="quiz-container" v-if="statementManager.correctAnswers.length > 0">
    <div class="question-navigation">
      <div class="navigation-buttons">
        <span
          v-for="index in +statementManager.statementQuiz.questions.length"
          v-bind:class="[
            'question-button',
            index === questionOrder + 1 ? 'current-question-button' : '',
            index === questionOrder + 1 &&
            statementManager.correctAnswers[index - 1].correctOptionId !==
              statementManager.statementQuiz.answers[index - 1].optionId
              ? 'incorrect-current'
              : '',
            statementManager.correctAnswers[index - 1].correctOptionId !==
            statementManager.statementQuiz.answers[index - 1].optionId
              ? 'incorrect'
              : ''
          ]"
          :key="index"
          @click="changeOrder(index - 1)"
        >
          {{ index }}
        </span>
      </div>
      <span
        class="left-button"
        @click="decreaseOrder"
        v-if="questionOrder !== 0"
        ><i class="fas fa-chevron-left"
      /></span>
      <span
        class="right-button"
        @click="increaseOrder"
        v-if="
          questionOrder !== statementManager.statementQuiz.questions.length - 1
        "
        ><i class="fas fa-chevron-right"
      /></span>
    </div>
    <result-component
      v-model="questionOrder"
      :answer="statementManager.statementQuiz.answers[questionOrder]"
      :correctAnswer="statementManager.correctAnswers[questionOrder]"
      :question="statementManager.statementQuiz.questions[questionOrder]"
      :questionNumber="statementManager.statementQuiz.questions.length"
      @increase-order="increaseOrder"
      @decrease-order="decreaseOrder"
    />
    <discussion-component
      :question="
        statementManager.statementQuiz.questions[questionOrder].question
      "
      :discussions="
        statementManager.statementQuiz.questions[questionOrder].discussions
      "
      :hasDiscussion="
        statementManager.statementQuiz.questions[questionOrder]
          .hasUserDiscussion
      "
      :answered="
        statementManager.statementQuiz.answers[questionOrder].optionId != null
      "
      v-on:message="updateMessage"
      v-on:discussions="updateDiscussions"
      @submit-discussion="submitDiscussion"
    />
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import StatementManager from '@/models/statement/StatementManager';
import ResultComponent from '@/views/student/quiz/ResultComponent.vue';
import DiscussionComponent from '@/views/student/quiz/DiscussionComponent.vue';
import Discussion from '@/models/management/Discussion';
import RemoteServices from '@/services/RemoteServices';

@Component({
  components: {
    'result-component': ResultComponent,
    'discussion-component': DiscussionComponent
  }
})
export default class ResultsView extends Vue {
  statementManager: StatementManager = StatementManager.getInstance;
  questionOrder: number = 0;
  discussion!: Discussion;

  async created() {
    if (this.statementManager.isEmpty()) {
      await this.$router.push({ name: 'create-quiz' });
    } else if (this.statementManager.correctAnswers.length === 0) {
      await this.$store.dispatch('loading');
      setTimeout(() => {
        this.statementManager.concludeQuiz();
      }, 2000);

      await this.$store.dispatch('clearLoading');
    }

    this.updateDiscussion();
  }

  increaseOrder(): void {
    if (
      this.questionOrder + 1 <
      +this.statementManager.statementQuiz!.questions.length
    ) {
      this.questionOrder += 1;
    }

    this.updateDiscussion();
  }

  decreaseOrder(): void {
    if (this.questionOrder > 0) {
      this.questionOrder -= 1;
    }

    this.updateDiscussion();
  }

  changeOrder(n: number): void {
    if (n >= 0 && n < +this.statementManager.statementQuiz!.questions.length) {
      this.questionOrder = n;
    }

    this.updateDiscussion();
  }

  async submitDiscussion() {
    if (this.discussion!.content === '') {
      await this.$store.dispatch('error', 'Discussion must have content');
      return;
    }

    try {
      const question = this.statementManager.statementQuiz!.questions[
        this.questionOrder
      ].question;
      this.discussion!.question = question;
      this.discussion!.questionId = question.id!;
      this.discussion!.courseId = this.$store.getters.getCurrentCourse.courseId;

      const result = await RemoteServices.createDiscussion(this.discussion!);
      this.statementManager.statementQuiz!.questions[
        this.questionOrder
      ].discussions!.push(result);
      this.statementManager.statementQuiz!.questions[
        this.questionOrder
      ].hasUserDiscussion = true;
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }

  updateDiscussion() {
    if (
      !this.statementManager.statementQuiz!.questions[this.questionOrder]
        .hasUserDiscussion
    ) {
      this.discussion = new Discussion();
    }
  }

  updateDiscussions(discussions: Discussion[]) {
    this.statementManager.statementQuiz!.questions[
      this.questionOrder
    ].discussions = discussions;
    this.statementManager.statementQuiz!.questions[
      this.questionOrder
    ].hasUserDiscussion = false;
  }

  updateMessage(message: string) {
    this.discussion!.content = message;
  }
}
</script>

<style lang="scss" scoped>
.incorrect {
  color: #cf2323 !important;
}

.incorrect-current {
  background-color: #cf2323 !important;
  color: #fff !important;
}
</style>
