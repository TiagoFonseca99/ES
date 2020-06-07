<template>
  <v-card class="table container">
    <v-data-table
      :headers="headers"
      :items="items"
      :search="search"
      multi-sort
      show-expand
      single-expand
      :expanded.sync="expanded"
      :mobile-breakpoint="0"
      :items-per-page="15"
      :footer-props="{ itemsPerPageOptions: [15, 30, 50, 100] }"
      item-key="questionId"
    >
      <template v-slot:top>
        <v-card-title>
          <v-text-field
            v-model="search"
            append-icon="search"
            label="Search"
            class="mx-2"
          />
          <v-spacer />
          <v-btn color="primary" dark @click="toggleAnswers">{{
            filterLabel
          }}</v-btn>
        </v-card-title>
      </template>
      <template v-slot:item.action="{ item }">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon
              large
              class="mr-2"
              v-on="on"
              @click="
                setDiscussion(item);
                editDiscussion();
              "
              >edit</v-icon
            >
            <v-icon
              large
              class="mr-2"
              v-on="on"
              @click="
                setDiscussion(item);
                deleteDiscussion();
              "
              color="red"
              >delete</v-icon
            >
          </template>
          <span>Delete Discussion</span>
        </v-tooltip>
      </template>
      <template v-slot:item.content="{ item }">
        <td class="justify-center">
          {{
            item.content.length > 70
              ? item.content.substring(0, 70) + '...'
              : item.content
          }}
        </td>
      </template>
      <template v-slot:item.available="{ item }">
        <td class="text-center">
          <v-icon v-if="item.available" style="color: #1ea62b"
            >fas fa-check</v-icon
          >
          <v-icon v-else style="color: #fc0b03">fas fa-times</v-icon>
        </td>
      </template>
      <template v-slot:expanded-item="{ headers, item }">
        <td :colspan="headers.length" class="text-left">
          <div class="container" style="margin: 10px; width: 90%">
            <b>On {{ item.date }}:</b>
            <span v-html="convertToMarkdown(item.content)" />
            <div v-if="item.replies !== []">
              <div
                class="reply"
                v-for="(reply, index) in item.replies"
                :key="reply.id"
              >
                <b v-if="$store.getters.getUser.id !== reply.userId"
                  >{{ reply.userName }} on {{ reply.date }}:
                </b>
                <div v-else>
                  <b>You on {{ reply.date }}:</b>
                  <v-icon
                    class="mr-2"
                    style="float: right"
                    @click="
                      setDiscussion(item);
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
                      setDiscussion(item);
                      setReply(reply, index);
                      editReply();
                    "
                    >edit</v-icon
                  >
                </div>
                <span v-html="convertToMarkdown(reply.message)" />
              </div>
            </div>
            <v-textarea
              clearable
              outlined
              auto-grow
              v-on:focus="setDiscussion(item)"
              @input="setReplyMessage"
              rows="2"
              label="Message"
              class="text"
              data-cy="reply"
              :id="'reply' + item.questionId"
            ></v-textarea>
            <v-card-actions>
              <v-spacer />
              <v-btn
                color="primary"
                data-cy="submitReply"
                @click="
                  setDiscussion(item);
                  submitReply();
                  clearTextarea('#reply' + item.questionId);
                "
                >Submit</v-btn
              >
            </v-card-actions>
          </div>
        </td>
      </template>
    </v-data-table>
    <edit-discussion-dialog
      :discussion="currentDiscussion"
      :dialog="discussionEdit"
      v-on:dialog="closeDialog"
      v-on:save-discussion="onSaveDiscussion"
    />
    <edit-reply-dialog
      :reply="reply"
      :dialog="replyEdit"
      v-on:dialog="closeDialog"
      v-on:save-reply="onSaveReply"
    />
  </v-card>
</template>

<script lang="ts">
import { Component, Vue, Watch } from 'vue-property-decorator';
import { convertMarkDown } from '@/services/ConvertMarkdownService';
import Discussion from '@/models/management/Discussion';
import RemoteServices from '@/services/RemoteServices';
import Reply from '@/models/management/Reply';
import EditDiscussionDialog from '@/views/student/discussion/EditDiscussionDialog.vue';
import EditReplyDialog from '@/views/student/discussion/EditReplyDialog.vue';

enum FilterState {
  REPLY = 'See all discussions',
  ALL = 'See discussions with reply'
}

@Component({
  components: {
    'edit-discussion-dialog': EditDiscussionDialog,
    'edit-reply-dialog': EditReplyDialog
  }
})
export default class DiscussionView extends Vue {
  discussions: Discussion[] = [];
  search: String = '';
  filterLabel: FilterState = FilterState.ALL;
  items: Discussion[] = [];
  expanded = [];
  currentDiscussion?: Discussion;
  replyMessages: Map<number, string> = new Map();
  reply?: Reply;
  replyInd!: number;
  discussionEdit: Boolean = false;
  replyEdit: Boolean = false;

  headers: object = [
    { text: '', value: 'data-table-expand' },
    { text: 'Actions', value: 'action', align: 'center' },
    {
      text: 'Question Title',
      value: 'question.title',
      align: 'center'
    },
    {
      text: 'Discussion Content',
      value: 'content',
      align: 'center'
    },
    { text: 'Public', value: 'available', align: 'center' }
  ];

  async created() {
    await this.$store.dispatch('loading');
    try {
      [this.discussions] = await Promise.all([
        RemoteServices.getDiscussions(
          this.$store.getters.getUser.id,
          this.$store.getters.getCurrentCourse.courseId
        )
      ]);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }

    this.customFilter();
    await this.$store.dispatch('clearLoading');
  }

  @Watch('filterLabel')
  customFilter() {
    if (this.filterLabel == FilterState.REPLY) {
      this.items = this.discussions.filter(discussion => {
        return discussion.replies! && discussion.replies!.length !== 0;
      });
    } else {
      this.items = this.discussions;
    }
  }

  toggleAnswers() {
    if (this.filterLabel == FilterState.REPLY) {
      this.filterLabel = FilterState.ALL;
    } else {
      this.filterLabel = FilterState.REPLY;
    }
  }

  setReplyMessage(message: string) {
    this.replyMessages.set(this.currentDiscussion!.questionId, message);
  }

  setDiscussion(discussion: Discussion) {
    this.currentDiscussion = discussion;
  }

  async submitReply() {
    try {
      if (
        this.replyMessages.get(this.currentDiscussion!.questionId) === undefined
      ) {
        this.replyMessages.set(this.currentDiscussion!.questionId, '');
      }
      const reply = await RemoteServices.createReply(
        this.replyMessages.get(this.currentDiscussion!.questionId)!,
        this.currentDiscussion!
      );
      if (this.currentDiscussion!.replies === null) {
        this.currentDiscussion!.replies = [];
      }
      this.currentDiscussion!.replies.push(reply);
      this.replyMessages.set(this.currentDiscussion!.questionId, '');
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

  async onSaveReply(edited: Reply) {
    this.reply = edited;
    this.closeDialog(false);

    for (let i = 0; i < this.discussions.length; i++) {
      if (
        this.discussions[i].questionId == this.currentDiscussion!.questionId
      ) {
        this.discussions[i].replies![this.replyInd] = edited;
        break;
      }
    }
  }

  async deleteReply() {
    try {
      await RemoteServices.deleteReply(this.reply!.id);
      this.currentDiscussion!.replies = this.currentDiscussion!.replies!.filter(
        obj => obj !== this.reply
      );
      this.customFilter();
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

  async onSaveDiscussion(edited: Discussion) {
    this.currentDiscussion = edited;
    this.closeDialog(false);

    for (let i = 0; i < this.discussions.length; i++) {
      if (this.discussions[i].questionId == edited.questionId) {
        this.discussions.splice(i, 1);
        break;
      }
    }

    this.discussions.unshift(edited);
  }

  async deleteDiscussion() {
    try {
      await RemoteServices.deleteDiscussion(
        this.currentDiscussion!.userId,
        this.currentDiscussion!.questionId
      );
      this.discussions = this.discussions.filter(
        obj => obj !== this.currentDiscussion
      );
      this.customFilter();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }

  convertToMarkdown(text: string) {
    return convertMarkDown(text, null);
  }
}
</script>

<style lang="scss" scoped>
.reply {
  padding-left: 30px;
}
</style>
