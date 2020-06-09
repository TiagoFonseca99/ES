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
                <b
                  ><span
                    class="primary--text"
                    @click="
                      setUsername(discussion.userUsername);
                      openDashboard();
                    "
                    style="cursor: pointer"
                    >{{ discussion.userName }}</span
                  >
                  on {{ discussion.date }}:</b
                >
                <v-icon
                  class="mr-2"
                  style="float: right"
                  @click="
                    setDiscussion(discussion, index);
                    editDiscussion();
                  "
                  data-cy="editDiscussion"
                  >edit</v-icon
                >
                <v-icon
                  class="mr-2"
                  style="float: right"
                  @click="
                    setDiscussion(discussion, index);
                    deleteDiscussion();
                  "
                  data-cy="removeDiscussion"
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
              <v-expansion-panel data-cy="replies">
                <v-expansion-panel-header
                  >View replies
                </v-expansion-panel-header>
                <v-expansion-panel-content>
                  <div
                    v-for="(reply, replyIndex) in discussion.replies"
                    :key="reply.id"
                    class="text-left reply"
                  >
                    <b v-if="$store.getters.getUser.id !== reply.userId"
                      ><span
                        class="primary--text"
                        v-if="reply.userRole === 'STUDENT'"
                        @click="
                          setUsername(reply.userUsername);
                          openDashboard();
                        "
                        style="cursor: pointer"
                        >{{ reply.userName }}</span
                      >
                      <span v-else>{{ reply.userName }}</span>
                      on {{ reply.date }}:
                    </b>
                    <b v-else>You on {{ reply.date }}:</b>
                    <v-icon
                      class="mr-2"
                      style="float: right"
                      @click="
                        setDiscussion(discussion, index);
                        setReply(reply, replyIndex);
                        deleteReply();
                      "
                      data-cy="removeReply"
                      color="red"
                      >delete</v-icon
                    >
                    <v-icon
                      class="mr-2"
                      style="float: right"
                      @click="
                        setDiscussion(discussion, index);
                        setReply(reply, replyIndex);
                        editReply();
                      "
                      data-cy="editReply"
                      >edit</v-icon
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
      :dialog="discussionEdit"
      :discussion="discussion"
      v-on:dialog="closeDialog"
      v-on:save-discussion="onSaveDiscussion"
    />
    <edit-reply-dialog
      :dialog="replyEdit"
      :reply="reply"
      v-on:dialog="closeDialog"
      v-on:save-reply="onSaveReply"
    />
    <show-dashboard-dialog
      v-if="currentUsername"
      v-model="dashboard"
      :username="currentUsername"
      v-on:close-show-dashboard-dialog="onCloseDashboard"
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
import EditReplyDialog from '@/views/teacher/questions/EditReplyDialog.vue';
import ShowDashboardDialog from '@/views/teacher/students/DashboardDialogView.vue';

@Component({
  components: {
    'edit-discussion-dialog': EditDiscussionDialog,
    'edit-reply-dialog': EditReplyDialog,
    'show-dashboard-dialog': ShowDashboardDialog
  }
})
export default class ReplyComponent extends Vue {
  @Prop() readonly discussions!: Discussion[];
  discussion!: Discussion;
  discussionInd!: number;
  reply!: Reply;
  replyInd!: number;
  replyMessages: Map<number, string> = new Map();
  discussionEdit: Boolean = false;
  replyEdit: Boolean = false;
  currentUsername: String | null = null;
  dashboard: Boolean = false;

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

  setReply(reply: Reply, index: number) {
    this.reply = reply;
    this.replyInd = index;
  }

  editReply() {
    this.replyEdit = true;
  }

  onSaveReply(reply: Reply) {
    this.reply = reply;
    console.log(this.discussions[this.discussionInd]);
    this.discussions[this.discussionInd].replies![this.replyInd] = reply;
    this.closeDialog(false);
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

  closeDialog(dialog: Boolean) {
    if (this.discussionEdit) {
      this.discussionEdit = dialog;
    } else {
      this.replyEdit = dialog;
    }
  }

  editDiscussion() {
    this.discussionEdit = true;
  }

  onSaveDiscussion(discussion: Discussion) {
    this.discussion = discussion;
    this.discussions[this.discussionInd] = discussion;
    this.closeDialog(false);
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

  setUsername(username: string) {
    this.currentUsername = username;
  }

  openDashboard() {
    this.dashboard = true;
  }

  onCloseDashboard() {
    this.dashboard = false;
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
