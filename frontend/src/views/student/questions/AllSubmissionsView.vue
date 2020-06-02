<template>
    <v-card class="table">
        <v-data-table
                :headers="headers"
                :custom-filter="customFilter"
                :items="items"
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
                      class="mx-2"
                    />
                    <v-spacer />
                    <v-btn color="primary" data-cy="Exclude" dark @click="toggleAnswers">{{ filterLabel }}</v-btn>
                    <v-spacer />
                    <v-btn-toggle class="button-group">
                       <v-btn color="primary" data-cy="SeeAll" @click="filterSubmissions('all')">{{"See All"}}</v-btn>
                       <v-btn color="primary" data-cy="SeeApproved" @click="filterSubmissions('accepted')">{{"See Accepted"}}</v-btn>
                       <v-btn color="primary" data-cy="SeeRejected" @click="filterSubmissions('rejected')">{{"See Rejected"}}</v-btn>
                    </v-btn-toggle>
                </v-card-title>
            </template>

            <template v-slot:item.questionDto.title="{ item }">
                <p
                        v-html="
            convertMarkDown(item.questionDto.title, item.questionDto.image)
          "
                        @click="showQuestionDialog(item.questionDto)"
                />
            </template>

            <template v-slot:item.questionDto.status="{ item }">
                <v-chip :color="getStatusColor(item.questionDto.status)" small>
                    <span>{{ item.questionDto.status }}</span>
                </v-chip>
            </template>

            <template v-slot:item.username="{ item }">
                <v-chip color="primary" small @click="openStudentDashboardDialog(item)">
                    <span v-if="item.anonymous"> {{ 'ANONYMOUS' }} </span>
                    <span v-else> {{ item.username }} </span>
                </v-chip>
            </template>

            <template v-slot:item.questionDto.creationDate="{ item }">
                <v-chip small>
                    <span> {{ item.questionDto.creationDate }}</span>
                </v-chip>
            </template>

            <template v-slot:item.questionDto.image="{ item }">
                <v-file-input
                        show-size
                        dense
                        small-chips
                        @change="handleFileUpload($event, item.questionDto)"
                        accept="image/*"
                />
            </template>

            <template v-slot:item.action="{ item }">
                <v-tooltip bottom>
                    <template v-slot:activator="{ on }">
                        <v-icon
                                large
                                class="mr-2"
                                v-on="on"
                                @click="showQuestionDialog(item.questionDto)"
                                data-cy="viewQuestion"
                        >visibility</v-icon
                        >
                    </template>
                    <span>Show Question</span>
                </v-tooltip>
            </template>
        </v-data-table>
        <footer>
            <v-icon class="mr-2">mouse</v-icon>Left-click on question's title to view
            it. <v-icon class="mr-2">mouse</v-icon>Left-click on user's username to
            view dashboard preview.
        </footer>
        <show-question-dialog
                v-if="currentQuestion"
                v-model="questionDialog"
                :question="currentQuestion"
                v-on:close-show-question-dialog="onCloseShowQuestionDialog"
        />
        <show-dashboard-dialog
                v-if="currentUsername"
                v-model="dashboardDialog"
                :username="currentUsername"
                v-on:close-show-dashboard-dialog="onCloseShowDashboardDialog"
        />
    </v-card>
</template>

<script lang="ts">
import { Component, Vue, Watch } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import { convertMarkDown } from '@/services/ConvertMarkdownService';
import Question from '@/models/management/Question';
import Submission from '@/models/management/Submission';
import Image from '@/models/management/Image';
import ShowQuestionDialog from '@/views/student/questions/ShowQuestionDialog.vue';

enum FilterState {
    INCLUDE = 'Include my submissions',
    EXCLUDE = 'Exclude my submissions'
}

@Component({
    components: {
        'show-question-dialog': ShowQuestionDialog
    }
})
export default class AllSubmissionsView extends Vue {
    filterLabel: FilterState = FilterState.EXCLUDE;
    submissions: Submission[] = [];
    items: Submission[] = [];
    currentQuestion: Question | null = null;
    questionDialog: boolean = false;
    search: string = '';
    currentUsername: string | null = null;
    dashboardDialog: boolean = false;

    headers: object = [
        {
         text: 'Actions',
         value: 'action',
         align: 'left',
         width: '15%',
         sortable: false
        },
        { text: 'Title', value: 'questionDto.title', align: 'center' },
        { text: 'Submitted by', value: 'username', align: 'center' },
        { text: 'Status', value: 'questionDto.status', align: 'center' },
        {
         text: 'Creation Date',
         value: 'questionDto.creationDate',
         align: 'center'
        },
        {
         text: 'Image',
         value: 'questionDto.image',
         align: 'center',
         sortable: false
        }
       ];

    async created() {
        await this.$store.dispatch('loading');
        try {
           [this.submissions] = await Promise.all([RemoteServices.getStudentsSubmissions()]);
           this.submissions.sort((a, b) => this.sortNewestFirst(a, b));
           this.items = this.submissions;
        } catch (error) {
           await this.$store.dispatch('error', error);
        }
        await this.$store.dispatch('clearLoading');
    }

    sortNewestFirst(a: Submission, b: Submission) {
        if (a.questionDto.creationDate && b.questionDto.creationDate)
          return a.questionDto.creationDate < b.questionDto.creationDate ? 1 : -1;
        else return 0;
    }

     customFilter(value: string, search: string, question: Question) {
     // noinspection SuspiciousTypeOfGuard,SuspiciousTypeOfGuard
        return (
          search != null &&
          JSON.stringify(question)
          .toLowerCase()
          .indexOf(search.toLowerCase()) !== -1
        );
     }

     convertMarkDown(text: string, image: Image | null = null): string {
        return convertMarkDown(text, image);
     }

     async handleFileUpload(event: File, question: Question) {
        if (question.id) {
           try {
               const imageURL = await RemoteServices.uploadImage(event, question.id);
               question.image = new Image();
               question.image.url = imageURL;
               confirm('Image ' + imageURL + ' was uploaded!');
           } catch (error) {
               await this.$store.dispatch('error', error);
           }
        }
     }

     showQuestionDialog(question: Question) {
         this.currentQuestion = question;
         this.questionDialog = true;
     }

     onCloseShowQuestionDialog() {
        this.questionDialog = false;
     }

     getStatusColor(status: string) {
        if (status === 'AVAILABLE') return 'green';
        else if (status === 'DEPRECATED') return 'red';
        else return 'pink';
     }

     openStudentDashboardDialog(submission: Submission) {
        if (!submission.anonymous) {
            this.dashboardDialog = true;
            this.currentUsername = submission.username;
        }
     }

     onCloseShowDashboardDialog() {
        this.dashboardDialog = false;
     }

     filterSubmissions(value: String) {
        if (value == 'all') {
            this.items = this.submissions;
        } else if (value == 'accepted') {
            this.items = this.submissions.filter(submission => { return submission.questionDto.status == 'AVAILABLE'; });
        } else {
            this.items = this.submissions.filter(submission => { return submission.questionDto.status == 'DEPRECATED'; });
        }
     }
    toggleAnswers() {
        if (this.filterLabel == FilterState.INCLUDE) {
            this.filterLabel = FilterState.EXCLUDE;
            this.items = this.submissions;
        } else {
            this.filterLabel = FilterState.INCLUDE;
            this.items = this.submissions.filter(submission => { return submission.studentId !== this.$store.getters.getUser.id; });
        }
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
