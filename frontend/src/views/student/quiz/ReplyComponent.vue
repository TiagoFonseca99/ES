<template>
  <div class="reply-container" v-if="discussions[0] !== undefined">
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
              style="flex: 1; position: relative; max-width: 100%;"
            >
              <b v-if="$store.getters.getUser.id !== discussion.userId"
                ><span
                  class="primary--text"
                  @click="
                    setUsername(discussion.userUsername);
                    openDashboard();
                  "
                  >{{ discussion.userName }}</span
                >
                on {{ discussion.date }}:</b
              >
              <div v-else>
                <b>You on {{ discussion.date }}:</b>
                <v-icon
                  large
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
                <v-icon
                  large
                  class="mr-2"
                  style="float: right"
                  @click="
                    setDiscussion(discussion, index);
                    editDiscussion();
                  "
                  data-cy="editDiscussion"
                  >edit</v-icon
                >
              </div>
              <span v-html="convertMarkDown(discussion.content)" />
            </div>
          </div>
          <v-expansion-panels
            v-if="discussion.replies !== null"
            :popout="true"
            style="margin-bottom: 20px"
          >
            <v-expansion-panel>
              <v-expansion-panel-header>View replies </v-expansion-panel-header>
              <v-expansion-panel-content>
                <div
                  v-for="(reply, index) in discussion.replies"
                  :key="reply.id"
                  class="text-left reply"
                >
                  <b v-if="$store.getters.getUser.id !== reply.userId"
                    ><span
                      v-if="reply.userRole === 'STUDENT'"
                      class="primary--text"
                      @click="
                        setUsername(reply.userUsername);
                        openDashboard();
                      "
                      >{{ reply.userName }}</span
                    >
                    <span v-else>{{ reply.userName }}</span>
                    on {{ reply.date }}:
                  </b>
                  <div v-else>
                    <b>You on {{ reply.date }}:</b>
                    <v-icon
                      class="mr-2"
                      style="float: right"
                      @click="
                        setDiscussion(discussion, index);
                        setReply(reply, index);
                        deleteReply();
                      "
                      color="red"
                      >delete</v-icon
                    >
                    <v-icon
                      class="mr-2"
                      style="float: right"
                      @click="
                        setDiscussion(discussion, index);
                        setReply(reply, index);
                        editReply();
                      "
                      >edit</v-icon
                    >
                  </div>
                  <span v-html="convertMarkDown(reply.message)" />
                </div>
                <div class="reply-message" v-if="discussion.userId === userId">
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
              v-if="discussion.userId === userId"
              v-on:focus="setDiscussion(discussion, index)"
              @input="setReplyMessage"
              rows="2"
              label="Message"
              class="text"
              data-cy="ReplyMessage"
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
                v-if="discussion.userId === userId"
                >Submit</v-btn
              >
            </v-card-actions>
          </div>
        </li>
      </ul>
    </div>
    <edit-discussion-dialog
      :discussion="discussion"
      :dialog="discussionEdit"
      v-on:save-discussion="onSaveDiscussion"
      v-on:dialog="closeDialog"
    />
    <edit-reply-dialog
      :reply="reply"
      :dialog="replyEdit"
      v-on:save-reply="onSaveReply"
      v-on:dialog="closeDialog"
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
import { Component, Vue, Prop } from 'vue-property-decorator';
import { convertMarkDown } from '@/services/ConvertMarkdownService';
import Discussion from '@/models/management/Discussion';
import RemoteServices from '@/services/RemoteServices';
import Reply from '@/models/management/Reply';
import EditDiscussionDialog from '@/views/student/discussion/EditDiscussionDialog.vue';
import EditReplyDialog from '@/views/student/discussion/EditReplyDialog.vue';
import ShowDashboardDialog from '@/views/student/dashboard/DashboardDialogView.vue';

@Component({
  components: {
    'edit-discussion-dialog': EditDiscussionDialog,
    'edit-reply-dialog': EditReplyDialog,
    'show-dashboard-dialog': ShowDashboardDialog
  }
})
export default class ReplyComponent extends Vue {
  @Prop() readonly discussions!: Discussion[];
  discussion: Discussion = this.discussions[0];
  discussionInd: number = 0;
  replyMessages: Map<number, string> = new Map();
  userId: number = this.$store.getters.getUser.id;
  reply!: Reply;
  replyInd!: number;
  discussionEdit: Boolean = false;
  replyEdit: Boolean = false;
  currentUsername: string | null = null;
  dashboard: Boolean = false;

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
      this.$emit('submit', true);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }

    for (let i = 0; i < this.discussions.length; i++) {
      if (this.discussions[i].replies === []) {
        return false;
      }
    }

    return true;
  }

  setReplyMessage(message: string) {
    this.replyMessages.set(this.discussion.userId!, message);
  }

  setDiscussion(discussion: Discussion, index: number) {
    this.discussion = discussion;
    this.discussionInd = index;
  }

  setReply(reply: Reply, index: number) {
    this.reply = reply;
    this.replyInd = index;
  }

  onSaveReply(reply: Reply) {
    this.reply = reply;
    this.discussions[this.discussionInd].replies![this.replyInd] = reply;
    this.closeDialog(false);
  }

  editReply() {
    this.replyEdit = true;
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

  closeDialog(dialog: Boolean) {
    if (this.discussionEdit) {
      this.discussionEdit = dialog;
    } else {
      this.replyEdit = dialog;
    }
  }

  onSaveDiscussion(discussion: Discussion) {
    this.discussion = discussion;
    this.discussions[this.discussionInd] = discussion;
    this.closeDialog(false);
  }

  editDiscussion() {
    this.discussionEdit = true;
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

  clearTextarea(name: string) {
    let textArea: HTMLTextAreaElement;
    let val = document.querySelector(name)!;
    textArea = val as HTMLTextAreaElement;
    textArea.value = '';
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
  margin: auto;
  border-radius: 0;

  ul {
    list-style-type: none;
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
