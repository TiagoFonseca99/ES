<template>
  <div class="container">
    <v-card v-if="!quizId">
      <v-text-field
        clearable
        style="display: inline-block; margin-right: 10px"
        v-model.number="tmpId"
        type="number"
        label="Code"
      />
      <v-btn class="primary" @click="onClick">Submit</v-btn>
    </v-card>
    <qrcode-stream v-if="!quizId" @decode="onDecode"></qrcode-stream>
    <v-card v-else>
      <v-card-title class="justify-center">
        Hold on and wait {{ timer() }} to start the quiz!
      </v-card-title>
    </v-card>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import { QrcodeStream } from 'vue-qrcode-reader';
import StatementQuiz from '@/models/statement/StatementQuiz';
import StatementManager from '@/models/statement/StatementManager';
import { milisecondsToHHMMSS } from '@/services/ConvertDateService';

@Component({
  components: {
    'qrcode-stream': QrcodeStream
  }
})
export default class ScanView extends Vue {
  quizId: number | null = null;
  quiz: StatementQuiz | null = null;
  tmpId: number | null = null;

  async onDecode(decodedString: String) {
    this.quizId = Number(decodedString);
    await this.getQuizByQRCode();
  }

  async getQuizByQRCode() {
    if (this.quizId && this.$router.currentRoute.name === 'scan') {
      try {
        this.quiz = await RemoteServices.getQuizByQRCode(this.quizId);

        if (this.quiz.timeToAvailability === 0) {
          let statementManager: StatementManager = StatementManager.getInstance;
          statementManager.statementQuiz = this.quiz;
          await this.$router.push({ name: 'solve-quiz' });
        }
      } catch (error) {
        await this.$store.dispatch('error', error);
        this.quizId = null;
      }
    }
  }

  async onClick() {
    this.quizId = this.tmpId;
    await this.getQuizByQRCode();
  }

  timer() {
    if (this.quiz?.timeToAvailability === 0) {
      this.getQuizByQRCode();
    }

    return milisecondsToHHMMSS(this.quiz?.timeToAvailability);
  }
}
</script>

<style lang="scss" scoped></style>
