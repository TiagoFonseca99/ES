<template>
  <div class="reply-container" v-if="discussions[0] !== undefined">
    <v-card>
      <v-card-title class="justify-left headline comp-title">
        Discussions
      </v-card-title>
      <div class="discussion">
        <ul>
          <li
            v-for="discussion in discussions"
            :key="discussion.content"
            @focus="setDiscussion(discussion)"
          >
            <div
              style="display: flex; justify-content: space-between; position: relative"
            >
              <div
                class="text-left"
                style="flex: 1; position: relative; max-width: 100%"
              >
                <b>{{ discussion.userName }} on {{ discussion.date }}:</b>
                <span v-html="convertMarkDown(discussion.content)" />
              </div>
            </div>
            <v-expansion-panels
              v-if="
                discussion.replies !== null && discussion.replies.length !== 0
              "
              :popout="true"
              style="margin-bottom: 20px"
            >
              <v-switch
                v-model="discussion.available"
                data-cy="changeAvailability"
                class="ma-4"
                :label="discussion.available ? 'Public' : 'Private'"
                @change="
                  setDiscussion(discussion);
                  setAvailability();
                "
                style="flex: 1; position: relative"
              />
              <v-expansion-panel>
                <v-expansion-panel-header
                  >View replies
                </v-expansion-panel-header>
                <v-expansion-panel-content>
                  <div
                    v-for="reply in discussion.replies"
                    :key="reply.id"
                    class="text-left reply"
                  >
                    <b v-if="$store.getters.getUser.id !== reply.userId"
                      >{{ reply.userName }} on {{ reply.date }}:
                    </b>
                    <b v-else>You on {{ reply.date }}:</b>
                    <v-icon
                      class="mr-2"
                      style="float: right"
                      @click="
                        setDiscussion(discussion);
                        setReply(reply);
                        deleteReply();
                      "
                      color="red"
                      >delete</v-icon
                    >
                    <span v-html="convertMarkDown(reply.message)" />
                  </div>
                  <div class="reply-message">
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
                      style="padding-top: 20px;"
                      :id="'reply' + discussion.userId"
                    ></v-textarea>
                    <v-card-actions>
                      <v-spacer />
                      <v-btn
                        color="blue darken-1"
                        data-cy="submitReply"
                        @click="
                          setDiscussion(discussion);
                          submitReply();
                          clearTextarea('#reply' + discussion.userId);
                        "
                        >Submit</v-btn
                      >
                    </v-card-actions>
                  </div>
                </v-expansion-panel-content>
              </v-expansion-panel>
            </v-expansion-panels>
            <div class="reply-message" v-else>
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
                :id="'reply' + discussion.userId"
              ></v-textarea>
              <v-card-actions>
                <v-switch
                  v-model="discussion.available"
                  data-cy="changeAvailability"
                  class="ma-4"
                  :label="discussion.available ? 'Public' : 'Private'"
                  @change="
                    setDiscussion(discussion);
                    setAvailability();
                  "
                  style="flex: 1; position: relative"
                />
                <v-spacer />
                <v-btn
                  color="blue darken-1"
                  data-cy="submitReply"
                  @click="
                    setDiscussion(discussion);
                    submitReply();
                    clearTextarea('#reply' + discussion.userId);
                  "
                  >Submit</v-btn
                >
              </v-card-actions>
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
import Reply from '@/models/management/Reply';

@Component
export default class ReplyComponent extends Vue {
  @Prop() readonly discussions!: Discussion[];
  discussion: Discussion = this.discussions[0];
  reply: Reply | undefined;
  replyMessages: Map<number, string> = new Map();

  @Emit('submit')
  async submitReply() {
    try {
      if (this.replyMessages.get(this.discussion.userId!) === undefined) {
        this.replyMessages.set(this.discussion.userId!, '');
      }
      const reply = await RemoteServices.createReply(
        this.replyMessages.get(this.discussion.userId!)!,
        this.discussion!
      );

      if (this.discussion.replies === null) {
        this.discussion.replies = [];
      }

      this.discussion.replies.push(reply);

      this.replyMessages.set(this.discussion.userId!, '');
    } catch (error) {
      await this.$store.dispatch('error', error);

      return false;
    }

    for (let i = 0; i < this.discussions.length; i++) {
      if (this.discussions[i].replies === []) {
        return false;
      }
    }

    return true;
  }

  async setAvailability() {
    try {
      this.discussion = await RemoteServices.setAvailability(
        this.discussion,
        this.discussion.available
      );
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }

  setReplyMessage(message: string) {
    this.replyMessages.set(this.discussion.userId!, message);
  }

  setDiscussion(discussion: Discussion) {
    this.discussion = discussion;
  }

  clearTextarea(name: string) {
    let textArea: HTMLTextAreaElement;
    let val = document.querySelector(name)!;
    textArea = val as HTMLTextAreaElement;
    textArea.value = '';
  }

  setReply(reply: Reply) {
    this.reply = reply;
  }

  async deleteReply() {
    try {
      await RemoteServices.deleteReply(this.reply!.id);
      this.discussion.replies = this.discussion.replies!.filter(
        obj => obj !== this.reply
      );
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
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
  margin: -50px auto 0;
  border-radius: 0;

  ul {
    list-style-type: none;
  }

  .comp-title {
    padding: 5px !important;
  }

  .reply-message {
    width: 95%;
    margin: 5px auto 20px;

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
    padding: 15px 15px 0 30px;
    border-bottom: #1e88e5 solid 2px;
  }
}
</style>
