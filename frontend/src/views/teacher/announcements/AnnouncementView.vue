<template>
  <v-card class="table">
    <v-data-table
      :headers="headers"
      :custom-filter="customFilter"
      :items="announcements"
      :search="search"
      multi-sort
      :mobile-breakpoint="0"
      :items-per-page="15"
      :footer-props="{ itemsPerPageOptions: [15, 30, 50, 100] }"
    >
      <template v-slot:top>
        <v-card-title>
          <v-text-field
            v-model="search"
            append-icon="search"
            label="Search"
            data-cy="Search"
            class="mx-2"
          />

          <v-spacer />
          <v-btn
            color="primary"
            dark
            @click="newAnnouncement"
            data-cy="newAnnouncement"
            >New Announcement</v-btn
          >
        </v-card-title>
      </template>
      <template v-slot:item.title="{ item }">
        <p
          v-html="convertMarkDown(item.title)"
          @click="showAnnouncementDialog(item)"
      /></template>

      <template v-slot:item.creationDate="{ item }">
        <v-chip small>
          <span> {{ item.creationDate }}</span>
        </v-chip>
      </template>

      <template v-slot:item.action="{ item }">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon
              large
              class="mr-2"
              v-on="on"
              @click="showAnnouncementDialog(item)"
              data-cy="viewAnnouncement"
              >visibility</v-icon
            >
          </template>
          <span>Show Announcement</span>
        </v-tooltip>
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon
              large
              class="mr-2"
              v-on="on"
              @click="editAnnouncement(item)"
              data-cy="editAnnouncement"
              >edit</v-icon
            >
          </template>
          <span>Edit Announcement</span>
        </v-tooltip>
      </template>
    </v-data-table>
    <footer>
      <v-icon class="mr-2">mouse</v-icon>Left-click on announcement's title to
      view it.
    </footer>
    <show-announcement-dialog
      v-if="currentAnnouncement"
      v-model="announcementDialog"
      :announcement="currentAnnouncement"
    />
    <edit-announcement-dialog
      v-if="currentAnnouncement"
      v-model="editAnnouncementDialog"
      :announcement="currentAnnouncement"
      v-on:save-announcement="onSaveAnnouncement"
    />
  </v-card>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import { convertMarkDown } from '@/services/ConvertMarkdownService';
import Announcement from '@/models/management/Announcement';
import EditAnnouncementDialog from '@/views/teacher/announcements/EditAnnouncementDialog.vue';
import ShowAnnouncementDialog from '@/views/teacher/announcements/ShowAnnouncementDialog.vue';
import Image from '@/models/management/Image';
import Submission from '@/models/management/Submission';

@Component({
  components: {
    'edit-announcement-dialog': EditAnnouncementDialog,
    'show-announcement-dialog': ShowAnnouncementDialog
  }
})
export default class AnnouncementView extends Vue {
  announcements: Announcement[] = [];
  currentAnnouncement: Announcement | null = null;
  editAnnouncementDialog: boolean = false;
  announcementDialog: boolean = false;
  search: string = '';

  headers: object = [
    {
      text: 'Actions',
      value: 'action',
      align: 'left',
      width: '15%',
      sortable: false
    },
    { text: 'Title', value: 'title', align: 'center' },
    {
      text: 'Creation Date',
      value: 'creationDate',
      align: 'right',
      width: '15%'
    }
  ];
  async created() {
    await this.$store.dispatch('loading');
    try {
      [this.announcements] = await Promise.all([
        RemoteServices.getAnnouncements()
      ]);
      this.announcements.sort((a, b) => this.sortNewestFirst(a, b));
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  customFilter(value: string, search: string, announcement: Announcement) {
    // noinspection SuspiciousTypeOfGuard,SuspiciousTypeOfGuard
    return (
      search != null &&
      JSON.stringify(announcement)
        .toLowerCase()
        .indexOf(search.toLowerCase()) !== -1
    );
  }

  convertMarkDown(text: string, image: Image | null = null): string {
    return convertMarkDown(text, image);
  }

  sortNewestFirst(a: Announcement, b: Announcement) {
    if (a.creationDate && b.creationDate)
      return a.creationDate < b.creationDate ? 1 : -1;
    else return 0;
  }

  showAnnouncementDialog(announcement: Announcement) {
    this.currentAnnouncement = announcement;
    this.announcementDialog = false;
    this.announcementDialog = true;
  }

  newAnnouncement() {
    this.currentAnnouncement = new Announcement();
    this.currentAnnouncement.courseExecutionId = this.$store.getters.getCurrentCourse.courseExecutionId;
    this.editAnnouncementDialog = true;
  }

  editAnnouncement(announcement: Announcement, e?: Event) {
    if (e) e.preventDefault();
    this.currentAnnouncement = announcement;
    this.editAnnouncementDialog = true;
  }

  async onSaveAnnouncement() {
    await this.$store.dispatch('loading');
    try {
      [this.announcements] = await Promise.all([
        RemoteServices.getAnnouncements()
      ]);
      this.announcements.sort((a, b) => this.sortNewestFirst(a, b));
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
    this.editAnnouncementDialog = false;
    this.currentAnnouncement = null;
  }
}
</script>

<style lang="scss" scoped>
.question-textarea {
  text-align: left;

  .CodeMirror,
  .CodeMirror-scroll {
    min-height: 200px !important;
  }
}
.option-textarea {
  text-align: left;

  .CodeMirror,
  .CodeMirror-scroll {
    min-height: 100px !important;
  }
}
</style>
