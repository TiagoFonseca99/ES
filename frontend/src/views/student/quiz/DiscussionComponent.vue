<template>
  <div class="discussion-container" v-if="answered || hasDiscussion">
    <v-card>
      <v-card-title class="justify-left headline comp-title">
        Discussions
      </v-card-title>
      <reply-component
        v-if="discussions != null"
        :discussions="discussions"
        v-on:discussions="onDiscussions"
      />
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
          data-cy="discussionText"
        ></v-textarea>
        <v-card-actions>
          <v-spacer />
          <v-btn
            color="primary"
            data-cy="createDiscussion"
            @click="submitDiscussion"
            >Submit</v-btn
          >
        </v-card-actions>
      </div>
    </v-card>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Prop, Emit, Watch } from 'vue-property-decorator';
import { convertMarkDown } from '@/services/ConvertMarkdownService';
import Question from '@/models/management/Question';
import Discussion from '@/models/management/Discussion';
import ReplyComponent from '@/views/student/quiz/ReplyComponent.vue';

@Component({
  components: { 'reply-component': ReplyComponent }
})
export default class DiscussionComponent extends Vue {
  @Prop(Boolean) readonly hasDiscussion!: Boolean;
  @Prop(Question) readonly question?: Question;
  @Prop() readonly discussions?: Discussion[];
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

  @Emit('discussions')
  onDiscussions(discussions: Discussion[]) {
    return discussions;
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
