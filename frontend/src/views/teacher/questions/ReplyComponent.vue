<template>
  <div class="reply-container" v-if="discussions[0] !== undefined">
    <v-card>
      <v-card-title class="justify-left headline comp-title">
        Discussions
      </v-card-title>
      <div class="discussion">
        <ul>
          <li v-for="discussion in discussions" :key="discussion.content">
            <span v-html="convertMarkDown(discussion.content)" />
            <div v-if="discussion.replyDto === undefined" class="reply-message">
              <v-textarea
                clearable
                outlined
                auto-grow
                v-on:focus="setDiscussion(discussion)"
                @input="setReplyMessage"
                rows="2"
                label="Message"
                class="text"
                data-cy="ReplyMessage"
              ></v-textarea>
              <v-card-actions>
                <v-spacer />
                <v-btn
                  color="blue darken-1"
                  data-cy="submitReply"
                  @click="
                    setDiscussion(discussion);
                    submitReply();
                  "
                  >Submit</v-btn
                >
              </v-card-actions>
            </div>
            <div v-else class="text-left reply">
              <span v-html="convertMarkDown(discussion.replyDto.message)" />
            </div>
          </li>
        </ul>
      </div>
    </v-card>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Prop, Emit } from 'vue-property-decorator';
import { convertMarkDown } from '@/services/ConvertMarkdownService';
import Discussion from '@/models/management/Discussion';
import RemoteServices from '../../../services/RemoteServices';

@Component
export default class ReplyComponent extends Vue {
  @Prop() readonly discussions!: Discussion[];
  discussion!: Discussion;
  replyMessages: Map<number, string> = new Map();

  @Emit('submit')
  async submitReply() {
    try {
      if (this.replyMessages.get(this.discussion.userId!) === undefined) {
        this.replyMessages.set(this.discussion.userId!, '');
      }
      this.discussion.replyDto = await RemoteServices.createReply(
        this.replyMessages.get(this.discussion.userId!)!,
        this.discussion!
      );
    } catch (error) {
      await this.$store.dispatch('error', error);

      return false;
    }

    for (let i = 0; i < this.discussions.length; i++) {
      if (!this.discussions[i].replyDto!) {
        return false;
      }
    }

    return true;
  }

  setReplyMessage(message: string) {
    this.replyMessages.set(this.discussion.userId!, message);
  }

  setDiscussion(discussion: Discussion) {
    this.discussion = discussion;
  }

  convertMarkDown(text: string) {
    return convertMarkDown(text, null);
  }
}
</script>

<style lang="scss" scoped>
.reply-container {
  box-sizing: border-box;
  color: rgb(51, 51, 51);
  max-width: 1024px;
  text-decoration: none solid;
  user-select: none;
  caret-color: rgb(51, 51, 51);
  overflow: hidden;
  margin: -50px auto 100px;
  border-radius: 0;

  ul {
    list-style-type: none;
  }

  .comp-title {
    padding: 5px !important;
  }

  .reply-message {
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
    padding: 25px;
    border-top: #1e88e5 solid 2px;
  }

  .reply {
    width: 100%;
    left: 0;
    margin: 5px;
    padding: 15px;
    border-top: #1e88e5 solid 2px;
  }
}
</style>
