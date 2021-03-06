<template>
  <v-form>
    <v-autocomplete
      v-model="questionTopics"
      :items="topics"
      multiple
      return-object
      item-text="name"
      item-value="name"
      @change="saveTopics"
    >
      <template v-slot:selection="data">
        <v-chip
          v-bind="data.attrs"
          :input-value="data.selected"
          close
          @click="data.select"
          @click:close="removeTopic(data.item)"
        >
          {{ data.item.name }}
        </v-chip>
      </template>
      <template v-slot:item="data">
        <v-list-item-content>
          <v-list-item-title v-html="data.item.name" />
        </v-list-item-content>
      </template>
    </v-autocomplete>
  </v-form>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator';
import Topic from '@/models/management/Topic';
import RemoteServices from '@/services/RemoteServices';
import Submission from '@/models/management/Submission';

@Component
export default class EditQuestionTopics extends Vue {
  @Prop({ type: Submission, required: true }) readonly submission!: Submission;
  @Prop({ type: Array, required: true }) readonly topics!: Topic[];

  questionTopics: Topic[] = JSON.parse(
    JSON.stringify(this.submission.questionDto.topics)
  );

  async saveTopics() {
    if (this.submission.questionDto.id) {
      try {
        await RemoteServices.updateQuestionTopics(
          this.submission.questionDto.id,
          this.questionTopics
        );
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
    }

    this.$emit(
      'submission-changed-topics',
      this.submission.questionDto.id,
      this.questionTopics
    );
  }

  removeTopic(topic: Topic) {
    this.questionTopics = this.questionTopics.filter(
      element => element.id != topic.id
    );
    this.saveTopics();
  }
}
</script>
