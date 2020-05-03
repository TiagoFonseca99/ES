<template>
  <v-dialog
    :value="dialog"
    @input="$emit('close-dialog')"
    @keydown.esc="$emit('close-dialog')"
    max-width="75%"
    max-height="80%"
  >
    <v-card>
      <v-card-title>
        <span class="headline">
          <b>Edit Tournament</b>
        </span>
      </v-card-title>

      <v-card-text class="text-left" v-if="editTournament">
        <v-container grid-list-md fluid>
          <v-layout column wrap>
            <v-flex xs24 sm12 md8>
              <b>Date:</b>
            </v-flex>
            <v-row>
              <v-col cols="12" sm="6">
                <VueCtkDateTimePicker
                  label="Start Time"
                  id="startTimeInput"
                  v-model="newStartTime"
                  format="YYYY-MM-DDTHH:mm:ssZ"
                >
                </VueCtkDateTimePicker>
              </v-col>
              <v-spacer></v-spacer>
              <v-col cols="12" sm="6">
                <VueCtkDateTimePicker
                  label="End Time"
                  id="endTimeInput"
                  v-model="newEndTime"
                  format="YYYY-MM-DDTHH:mm:ssZ"
                >
                </VueCtkDateTimePicker>
              </v-col>
            </v-row>
            <v-flex xs24 sm12 md8>
              <p>
                <b>Number Of Questions:</b>
                {{ oldNumberOfQuestions }}
              </p>
              <v-text-field
                min="1"
                step="1"
                type="number"
                v-model="editTournament.numberOfQuestions"
                label="Number Of Questions"
                data-cy="NumberOfQuestions"
              />
            </v-flex>
          </v-layout>
        </v-container>
      </v-card-text>
      <v-card-text class="text-center" v-if="editTournament">
        <v-row>
          <v-col cols="12" sm="6" class="light-green lighten-4">
            <v-data-table
              :headers="topicHeaders"
              :custom-filter="topicFilter"
              :items="currentTopics"
              :search="JSON.stringify(currentTopicsSearch)"
              :mobile-breakpoint="0"
              :items-per-page="5"
              :footer-props="{ itemsPerPageOptions: [5, 10, 15] }"
            >
              <template v-slot:top>
                <h2>Currently selected</h2>
                <v-autocomplete
                  v-model="currentTopicsSearch"
                  label="Search"
                  :items="allTopics"
                  :filter="topicSearch"
                  :search-input.sync="currentTopicsSearchText"
                  @change="currentTopicsSearchText = ''"
                  item-text="name"
                  return-object
                  chips
                  small-chips
                  clearable
                  deletable-chips
                  multiple
                  dense
                  class="mx-4"
                >
                </v-autocomplete>
              </template>
              <template v-slot:item.topicsCreate="{ item }">
                {{ item.name }}
              </template>
              <template v-slot:item.action="{ item }">
                <v-tooltip bottom>
                  <template v-slot:activator="{ on }">
                    <v-icon
                      small
                      class="mr-2"
                      v-on="on"
                      @click="removeTopic(item)"
                      data-cy="removeTopic"
                    >
                      remove</v-icon
                    >
                  </template>
                  <span>Remove from Tournament</span>
                </v-tooltip>
              </template>
            </v-data-table>
          </v-col>
          <v-col cols="12" sm="6" class="red lighten-4">
            <v-data-table
              :headers="topicHeaders"
              :custom-filter="topicFilter"
              :items="availableTopics"
              :search="JSON.stringify(allTopicsSearch)"
              :mobile-breakpoint="0"
              :items-per-page="5"
              :footer-props="{ itemsPerPageOptions: [5, 10, 15] }"
              data-cy="Topics"
            >
              <template v-slot:top>
                <h2>Available topics</h2>
                <v-autocomplete
                  v-model="allTopicsSearch"
                  label="Search"
                  :items="allTopics"
                  :filter="topicSearch"
                  :search-input.sync="allTopicsSearchText"
                  @change="allTopicsSearchText = ''"
                  item-text="name"
                  return-object
                  chips
                  small-chips
                  clearable
                  deletable-chips
                  multiple
                  dense
                  class="mx-4"
                >
                </v-autocomplete>
              </template>
              <template v-slot:item.topicsCreate="{ item }">
                {{ item.name }}
              </template>
              <template v-slot:item.action="{ item }">
                <v-tooltip bottom>
                  <template v-slot:activator="{ on }">
                    <v-icon
                      small
                      class="mr-2"
                      v-on="on"
                      @click="addTopic(item)"
                      data-cy="addTopic"
                    >
                      add</v-icon
                    >
                  </template>
                  <span>Add to Tournament</span>
                </v-tooltip>
              </template>
            </v-data-table>
          </v-col>
        </v-row>
      </v-card-text>

      <v-card-actions>
        <v-spacer />
        <v-btn
          color="blue darken-1"
          @click="cancelTournament"
          data-cy="cancelButton"
          >Cancel</v-btn
        >
        <v-btn
          color="blue darken-1"
          @click="saveTournament"
          data-cy="saveButton"
          >Save</v-btn
        >
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { Component, Model, Prop, Vue } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import Tournament from '@/models/user/Tournament';
import Topic from '@/models/management/Topic';
import VueCtkDateTimePicker from 'vue-ctk-date-time-picker';
import 'vue-ctk-date-time-picker/dist/vue-ctk-date-time-picker.css';

Vue.component('VueCtkDateTimePicker', VueCtkDateTimePicker);

@Component
export default class EditTournamentDialog extends Vue {
  @Model('dialog', Boolean) dialog!: boolean;
  @Prop({ type: Tournament, required: true }) readonly tournament!: Tournament;

  editTournament!: Tournament;
  currentTopicsSearch: string = '';
  currentTopicsSearchText: string = '';
  allTopicsSearch: string = '';
  allTopicsSearchText: string = '';

  allTopics: Topic[] = [];

  currentTopics: Topic[] = [];
  availableTopics: Topic[] = [];

  oldStartTime: string = '';
  oldEndTime: string = '';
  oldNumberOfQuestions: number = -1;
  oldTopics: String[] = [];

  newStartTime: string = '';
  newEndTime: string = '';
  topicsToAdd: Topic[] = [];
  topicsToRemove: Topic[] = [];

  topicHeaders: object = [
    {
      text: 'Topics',
      value: 'topicsCreate',
      align: 'left',
      sortable: false
    },
    {
      text: 'Actions',
      value: 'action',
      align: 'center',
      width: '150px',
      sortable: false
    }
  ];

  async created() {
    this.editTournament = this.tournament;
    if (this.editTournament.startTime) {
      this.oldStartTime = this.newStartTime = this.editTournament.startTime;
    }
    if (this.editTournament.endTime) {
      this.oldEndTime = this.newEndTime = this.editTournament.endTime;
    }
    if (this.editTournament.numberOfQuestions) {
      this.oldNumberOfQuestions = this.editTournament.numberOfQuestions;
    }
    this.oldTopics = this.editTournament.topics;
    await this.$store.dispatch('loading');
    try {
      [this.allTopics] = await Promise.all([RemoteServices.getTopics()]);
      this.availableTopics = this.allTopics;
      this.editTournament.topics.forEach(topicName => {
        this.availableTopics.forEach(topic => {
          if (topic.name.valueOf() == topicName.valueOf()) {
            this.addTopic(topic);
          }
        });
      });
      this.topicsToAdd = [];
      this.topicsToRemove = [];
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  async cancelTournament() {
    this.editTournament.startTime = this.oldStartTime;
    this.editTournament.endTime = this.oldEndTime;
    this.editTournament.numberOfQuestions = this.oldNumberOfQuestions;
    this.editTournament.topics = this.oldTopics;

    console.log('topics antigos: ' + this.editTournament.topics);
    this.topicsToAdd.map(topic => console.log('Topic to add: ' + topic.name));
    this.topicsToRemove.map(topic =>
      console.log('Topic to remove: ' + topic.name)
    );
    this.$emit('close-dialog');
  }

  async saveTournament() {
    this.editTournament.startTime = this.newStartTime;
    this.editTournament.endTime = this.newEndTime;

    if (
      this.editTournament &&
      (!this.editTournament.startTime ||
        !this.editTournament.endTime ||
        !this.editTournament.numberOfQuestions ||
        this.currentTopics.length == 0)
    ) {
      await this.$store.dispatch(
        'error',
        'Tournament must have Start Time, End Time, Number Of Questions and Topics'
      );
      return;
    }

    if (this.editTournament && this.editTournament.id != null) {
      const enrolled = this.editTournament.enrolled;
      const topics = this.editTournament.topics;
      this.editTournament.enrolled = undefined;
      this.editTournament.topics = [];
      this.editTournament.state = 'NOT_CANCELED';

      let topicsToAddID = this.topicsToAdd.map(topic => {
        return topic.id;
      });

      let topicsToRemoveID = this.topicsToRemove.map(topic => {
        return topic.id;
      });

      try {
        const result = await RemoteServices.editTournament(
          this.oldStartTime,
          this.oldEndTime,
          this.oldNumberOfQuestions,
          topicsToAddID,
          topicsToRemoveID,
          this.editTournament
        );
        console.log('Resultado: ' + result.topics);
        this.$emit('edit-tournament', result);
      } catch (error) {
        await this.$store.dispatch('error', error);
        this.editTournament.enrolled = enrolled;
        this.editTournament.topics = topics;
      }
      this.editTournament.enrolled = enrolled;
      this.editTournament.topics = this.currentTopics.map(topic => {
        return topic.name;
      });
    }
  }

  topicFilter(value: string, search: string, topic: Topic) {
    let searchTopics = JSON.parse(search);

    if (searchTopics !== '') {
      return searchTopics
        .map((searchTopic: Topic) => searchTopic.name)
        .every((t: string) => topic.name.includes(t));
    }
    return true;
  }

  topicSearch(topic: Topic, search: string) {
    return (
      search != null &&
      topic.name.toLowerCase().indexOf(search.toLowerCase()) !== -1
    );
  }

  removeTopic(topic: Topic) {
    if (this.oldTopics.includes(topic.name)) {
      this.topicsToRemove.push(topic);
    }

    if (this.topicsToAdd.includes(topic)) {
      this.topicsToAdd = this.topicsToAdd.filter(t => t.id != topic.id);
    }

    this.availableTopics.push(topic);
    this.availableTopics.sort((a, b) => {
      let result = a.name.localeCompare(b.name);
      return result === 0 ? 0 : result > 0 ? 1 : -1;
    });
    this.currentTopics = this.currentTopics.filter(t => t.id != topic.id);
  }

  addTopic(topic: Topic) {
    if (!this.oldTopics.includes(topic.name)) {
      this.topicsToAdd.push(topic);
    }

    if (this.topicsToRemove.includes(topic)) {
      this.topicsToRemove = this.topicsToRemove.filter(t => t.id != topic.id);
    }

    this.currentTopics.push(topic);
    this.currentTopics.sort((a, b) => {
      let result = a.name.localeCompare(b.name);
      return result === 0 ? 0 : result > 0 ? 1 : -1;
    });
    this.availableTopics = this.availableTopics.filter(t => t.id != topic.id);
  }
}
</script>
