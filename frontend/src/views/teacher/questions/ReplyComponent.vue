<template>
  <div class="reply-container" v-if="discussions[0] !== undefined">
    <v-card>
      <v-card-title class="justify-left headline comp-title">
        Discussions
      </v-card-title>
      <div class="discussion">
        <ul>
          <li
            v-for="(discussion, index) in discussions"
            :key="discussion.content"
            @focus="setDiscussion(discussion, index)"
          >
            <div
              style="display: flex; justify-content: space-between; position: relative"
            >
              <div
                class="text-left"
                style="flex: 1; position: relative; max-width: 100%"
              >
                <b>{{ discussion.userName }} on {{ discussion.date }}:</b>
                <v-icon
                  class="mr-2"
                  style="float: right"
                  @click="
                    setDiscussion(discussion, index);
                    editDiscussion();
                  "
                  >edit</v-icon
                >
                <v-icon
                  class="mr-2"
                  style="float: right"
                  @click="
                    setDiscussion(discussion, index);
                    deleteDiscussion();
                  "
                  color="red"
                  >delete</v-icon
                >
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
                  setDiscussion(discussion, index);
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
                        setDiscussion(discussion, index);
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
                      v-on:focus="setDiscussion(discussion, index)"
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
                        color="primary"
                        data-cy="submitReply"
                        @click="
                          setDiscussion(discussion, index);
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
                v-on:focus="setDiscussion(discussion, index)"
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
                    setDiscussion(discussion, index);
                    setAvailability();
                  "
                  style="flex: 1; position: relative"
                />
                <v-spacer />
                <v-btn
                  color="primary"
                  data-cy="submitReply"
                  @click="
                    setDiscussion(discussion, index);
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
    <edit-discussion-dialog
      :dialog="edit"
      :discussion="discussion"
      v-on:dialog="setDialog"
      v-on:save-discussion="onSaveDiscussion"
    />
  </div>
</template>

<script lang="ts">
import { Component, Vue, Prop, Emit } from 'vue-property-decorator';
import { convertMarkDown } from '@/services/ConvertMarkdownService';
import Discussion from '@/models/management/Discussion';
import RemoteServices from '@/services/RemoteServices';
import Reply from '@/models/management/Reply';
import EditDiscussionDialog from '@/views/teacher/questions/EditDiscussionDialog.vue';

@Component({
  components: {
    'edit-discussion-dialog': EditDiscussionDialog
  }
})
export default class ReplyComponent extends Vue {
  @Prop() readonly discussions!: Discussion[];
  discussion!: Discussion;
  discussionInd!: number;
  reply: Reply | undefined;
  replyMessages: Map<number, string> = new Map();
  edit: Boolean = false;

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

  setDiscussion(discussion: Discussion, index: number) {
    this.discussion = discussion;
    this.discussionInd = index;
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
      this.$emit(
        'replies',
        (this.discussion.replies = this.discussion.replies!.filter(
          obj => obj !== this.reply
        ))
      );
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }

  editDiscussion() {
    this.edit = true;
  }

  setDialog(dialog: Boolean) {
    this.edit = dialog;
  }

  onSaveDiscussion(discussion: Discussion) {
    this.discussion = discussion;
    this.discussions[this.discussionInd] = discussion;
    this.setDialog(false);
  }

  async deleteDiscussion() {
    try {
      await RemoteServices.deleteDiscussion(
        this.discussion.userId,
        this.discussion.questionId
      );
      this.$emit(
        'discussions',
        this.discussions.filter(obj => obj !== this.discussion)
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
