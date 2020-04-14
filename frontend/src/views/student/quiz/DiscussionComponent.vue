<template>
  <div class="discussion-container" v-if="answered || hasDiscussion">
    <v-card>
      <v-card-title class="justify-left headline comp-title">
        Discussions
      </v-card-title>
      <div v-if="answered && !hasDiscussion" class="discussion-message">
        <v-textarea
          clearable
          outlined
          auto-grow
          v-model="discussionMessage"
          v-on:input="onInput"
          rows="2"
          label="Message"
          class="text"
        ></v-textarea>
        <v-card-actions>
          <v-spacer />
          <v-btn color="blue darken-1" @click="submitDiscussion">Submit</v-btn>
        </v-card-actions>
      </div>
      <div v-else class="text-left discussion">
        <span v-html="convertMarkDown(discussion.content)" />
      </div>
    </v-card>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Prop, Emit, Watch } from 'vue-property-decorator';
import { convertMarkDown } from '@/services/ConvertMarkdownService';
import Question from '@/models/management/Question';
import Discussion from '@/models/management/Discussion';

@Component
export default class DiscussionComponent extends Vue {
  @Prop(Boolean) readonly hasDiscussion!: Boolean;
  @Prop(Question) readonly question?: Question;
  @Prop(Discussion) readonly discussion?: Discussion;
  @Prop(Boolean) readonly answered!: boolean;
  discussionMessage: string = '';

  @Emit()
  submitDiscussion() {
    this.discussionMessage = '';
    return 1;
  }

  @Emit('message')
  onInput() {
    return this.discussionMessage;
  }

  @Watch('question')
  onQuestionChange() {
    this.discussionMessage = '';
  }

  convertMarkDown(text: string) {
    return convertMarkDown(text, null);
  }
}
</script>

<style lang="scss" scoped>
.discussion-container {
  box-sizing: border-box;
  color: rgb(51, 51, 51);
  max-width: 1024px;
  text-decoration: none solid;
  user-select: none;
  caret-color: rgb(51, 51, 51);
  overflow: hidden;
  margin: -100px auto 100px;
  border-radius: 0;

  .comp-title {
    padding: 5px !important;
  }

  .discussion-message {
    width: 95%;
    margin: 5px auto auto;

    .text {
      user-select: text;
      margin: 0 auto -30px;
    }
  }

  .discussion {
    width: 100%;
    left: 0;
    margin: 5px;
    padding: 15px;
    border-top: #1e88e5 solid 2px;
  }
}
</style>
