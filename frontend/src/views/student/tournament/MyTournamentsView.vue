<template>
  <v-card class="table">
    <v-data-table
      :headers="headers"
      :items="tournaments"
      :search="search"
      disable-pagination
      :hide-default-footer="true"
      :mobile-breakpoint="0"
      :items-per-page="15"
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
      <template v-slot:item.state="{ item }">
        <v-chip :color="getStateColor(item.state)">
          {{ getStateName(item.state) }}
        </v-chip>
      </template>
      <template v-slot:item.enrolled="{ item }">
        <v-chip :color="getEnrolledColor(item.enrolled)">
          {{ getEnrolledName(item.enrolled) }}
        </v-chip>
      </template>
      <template v-slot:item.privateTournament="{ item }">
        <v-chip :color="getPrivateColor(item.privateTournament)">
          {{ getPrivateName(item.privateTournament) }}
        </v-chip>
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
        <v-tooltip bottom v-if="isNotCanceled(item)">
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
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon
              small
              class="mr-2"
              v-on="on"
              @click="removeTournament(item)"
              color="red"
              data-cy="RemoveTournament"
              >delete</v-icon
            >
          </template>
          <span>Remove Tournament</span>
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
    { text: 'Tournament Number', value: 'id', align: 'center', width: '10%' },
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
      text: 'Privacy',
      value: 'privateTournament',
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
      this.tournaments.sort((a, b) => this.sortById(a, b));
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  sortById(a: Tournament, b: Tournament) {
    if (a.id && b.id) return a.id > b.id ? 1 : -1;
    else return 0;
  }

  editTournament(tournamentToEdit: Tournament) {
    this.currentTournament = tournamentToEdit;
    this.editTournamentDialog = true;
  }

  async onEditTournament(tournament: Tournament) {
    this.currentTournament = tournament;
    try {
      this.tournaments = await RemoteServices.getUserTournaments();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    this.editTournamentDialog = false;
    this.currentTournament = null;
  }

  onCloseDialog() {
    this.editTournamentDialog = false;
    this.currentTournament = null;
  }

  getStateColor(state: string) {
    if (state === 'NOT_CANCELED') return 'green';
    else return 'red';
  }

  getStateName(state: string) {
    if (state === 'NOT_CANCELED') return 'NOT CANCELED';
    else return 'CANCELED';
  }

  getEnrolledColor(enrolled: string) {
    if (enrolled) return 'green';
    else return 'red';
  }

  getEnrolledName(enrolled: string) {
    if (enrolled) return 'YOU ARE IN';
    else return 'YOU NEED TO JOIN';
  }

  getPrivateColor(privateTournament : boolean) {
    if (privateTournament) return 'red';
    else return 'green';
  }
  getPrivateName(privateTournament : boolean) {
    if (privateTournament) return 'private';
    else return 'public';
  }

  isNotCanceled(tournamentToCancel: Tournament) {
    return tournamentToCancel.state === 'NOT_CANCELED';
  }

  async cancelTournament(tournamentToCancel: Tournament) {
    if (confirm('Are you sure you want to cancel this tournament?')) {
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
      tournamentToCancel.enrolled = enrolled;
      tournamentToCancel.topics = topics;
      tournamentToCancel.state = 'CANCELED';
    }
  }

  async removeTournament(tournamentToRemove: Tournament) {
    if (confirm('Are you sure you want to delete this tournament?')) {
      const enrolled = tournamentToRemove.enrolled;
      const topics = tournamentToRemove.topics;
      tournamentToRemove.enrolled = false;
      tournamentToRemove.topics = [];
      try {
        if (tournamentToRemove.id)
          await RemoteServices.removeTournament(tournamentToRemove.id);
        this.tournaments = await RemoteServices.getUserTournaments();
      } catch (error) {
        await this.$store.dispatch('error', error);
        tournamentToRemove.enrolled = enrolled;
        tournamentToRemove.topics = topics;
        return;
      }
    }
  }
}
</script>

<style lang="scss" scoped></style>
