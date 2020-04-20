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
      <template v-slot:item.content="{ item }">
        <td class="justify-center">
          {{
            item.content.length > 70
              ? item.content.substring(0, 70) + '...'
              : item.content
          }}
        </td>
      </template>
      <template v-slot:item.replyDto="{ item }">
        <td>
          <v-icon v-if="item.replyDto !== undefined" style="color: #1ea62b"
            >fas fa-check</v-icon
          >
          <v-icon v-else style="color: #fc0b03">fas fa-times</v-icon>
        </td>
      </template>
      <template v-slot:expanded-item="{ headers, item }">
        <td :colspan="headers.length" class="text-left">
          <div class="container" style="margin: 10px; width: 90%">
            <p v-html="convertToMarkdown(item.content)" />
            <p v-if="item.replyDto !== undefined">
              <b>Reply by user with id {{ item.replyDto.teacherId }}: </b
              >{{ convertToMarkdown(item.replyDto.message) }}
            </p>
          </div>
        </td>
      </template>
    </v-data-table>
  </v-card>
</template>

<script lang="ts">
import { Component, Vue, Watch } from 'vue-property-decorator';
import { convertMarkDown } from '@/services/ConvertMarkdownService';
import Discussion from '@/models/management/Discussion';
import RemoteServices from '@/services/RemoteServices';

enum FilterState {
  REPLY = 'See all discussions',
  ALL = 'See discussions with reply'
}

@Component
export default class DiscussionView extends Vue {
  discussions: Discussion[] = [];
  search: String = '';
  filterLabel: FilterState = FilterState.ALL;
  items: Discussion[] = [];
  expanded = [];

  headers: object = [
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
    { text: 'Reply?', value: 'replyDto', align: 'center' },
    { text: '', value: 'data-table-expand' }
  ];

  async created() {
    await this.$store.dispatch('loading');
    try {
      [this.discussions] = await Promise.all([
        RemoteServices.getDiscussions(this.$store.getters.getUser.id)
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
        return discussion.replyDto!;
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

  convertToMarkdown(text: string) {
    return convertMarkDown(text, null);
  }
}
</script>

<style lang="scss" scoped></style>
