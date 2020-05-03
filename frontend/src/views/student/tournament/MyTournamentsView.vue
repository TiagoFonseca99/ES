<template>
  <v-card class="table">
    <v-data-table
      :headers="headers"
      :items="tournaments"
      :search="search"
      disable-pagination
      :hide-default-footer="true"
      :mobile-breakpoint="0"
      multi-sort
      data-cy="allTournaments"
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
        </v-card-title>
      </template>

      <template v-slot:item.action="{ item }">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon
              small
              class="mr-2"
              v-on="on"
              @click="editTournament(item)"
              data-cy="EditTournament"
              >create</v-icon
            >
          </template>
          <span>Edit Tournament</span>
        </v-tooltip>
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon
              small
              class="mr-2"
              v-on="on"
              @click="cancelTournament(item)"
              data-cy="CancelTournament"
              >cancel</v-icon
            >
          </template>
          <span>Cancel Tournament</span>
        </v-tooltip>
      </template>
    </v-data-table>

    <edit-tournament-dialog
      v-if="currentTournament"
      v-model="editTournamentDialog"
      :tournament="currentTournament"
      v-on:edit-tournament="onEditTournament"
      v-on:close-dialog="onCloseDialog"
    />
  </v-card>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import Tournament from '@/models/user/Tournament';
import RemoteServices from '@/services/RemoteServices';
import EditTournamentDialog from '@/views/student/tournament/EditTournamentView.vue';

@Component({
  components: {
    'edit-tournament-dialog': EditTournamentDialog
  }
})
export default class MyTournamentsView extends Vue {
  tournaments: Tournament[] = [];
  currentTournament: Tournament | null = null;
  editTournamentDialog: boolean = false;
  search: string = '';
  headers: object = [
    {
      text: 'Course Acronym',
      value: 'courseAcronym',
      align: 'center',
      width: '10%'
    },
    { text: 'Id', value: 'id', align: 'center', width: '10%', sort: true },
    {
      text: 'Topics',
      value: 'topics',
      align: 'center',
      width: '10%'
    },
    {
      text: 'State',
      value: 'state',
      align: 'center',
      width: '10%'
    },
    {
      text: 'Start Time',
      value: 'startTime',
      align: 'center',
      width: '10%'
    },
    {
      text: 'End Time',
      value: 'endTime',
      align: 'center',
      width: '10%'
    },
    {
      text: 'Number of Questions',
      value: 'numberOfQuestions',
      align: 'center',
      width: '10%'
    },
    {
      text: 'Enrolled',
      value: 'enrolled',
      align: 'center',
      sortable: false,
      width: '10%'
    },
    {
      text: 'Actions',
      value: 'action',
      align: 'center',
      sortable: false,
      width: '10%'
    }
  ];

  async created() {
    await this.$store.dispatch('loading');
    try {
      this.tournaments = await RemoteServices.getUserTournaments();
      if (this.tournaments) {
        console.log('Li isto: ' + this.tournaments[0].topics);
      }
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  editTournament(tournamentToEdit: Tournament) {
    this.currentTournament = tournamentToEdit;
    this.editTournamentDialog = true;
  }

  async onEditTournament(tournament: Tournament) {
    this.currentTournament = tournament;
    this.editTournamentDialog = false;
    this.currentTournament = null;
  }

  onCloseDialog() {
    this.editTournamentDialog = false;
    this.currentTournament = null;
  }

  async cancelTournament(tournamentToCancel: Tournament) {
    const enrolled = tournamentToCancel.enrolled;
    const topics = tournamentToCancel.topics;
    tournamentToCancel.enrolled = false;
    tournamentToCancel.topics = [];
    try {
      await RemoteServices.cancelTournament(tournamentToCancel);
    } catch (error) {
      await this.$store.dispatch('error', error);
      tournamentToCancel.enrolled = enrolled;
      tournamentToCancel.topics = topics;
      return;
    }
    tournamentToCancel.enrolled = true;
    tournamentToCancel.topics = topics;
  }
}
</script>

<style lang="scss" scoped></style>
