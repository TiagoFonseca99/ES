<template>
  <div class="reply-container" v-if="discussions[0] !== undefined">
    <v-card>
      <v-card-title class="justify-left headline comp-title">
        Discussions
      </v-card-title>
      <div class="discussion">
        <ul>
          <li v-for="discussion in discussions" :key="discussion.content">
            <v-switch
              v-model="discussion.available"
              class="ma-4"
              label="Public"
              v-on:click="setDiscussion(discussion); setAvailability()"
            />
            <div class="text-left">
              <b>{{ discussion.userName }} on {{ discussion.date }}:</b>
              <span v-html="convertMarkDown(discussion.content)" />
            </div>
            <v-expansion-panels
              v-if="discussion.replies !== null"
              :popout="true"
            >
              <v-expansion-panel>
                <v-expansion-panel-header
                  >View replies</v-expansion-panel-header
                >
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
          </li>
        </ul>
      </div>
    </v-card>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Prop, Emit, Watch } from 'vue-property-decorator';
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
  
  private delay(ms: number)
  {
    return new Promise(resolve => setTimeout(resolve, ms));
  }

  async setAvailability() {
    try {
      this.discussion = await RemoteServices.setAvailability(this.discussion, !this.discussion.available);
      await this.delay(1000);
    }
    catch (error) {
      await this.$store.dispatch('error', error);
    }
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
    padding: 15px 15px 0 30px;
    border-bottom: #1e88e5 solid 2px;
  }
}
</style>
