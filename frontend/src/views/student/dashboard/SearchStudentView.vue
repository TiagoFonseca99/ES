<template>
  <v-card class="table">
    <v-data-table
      :headers="headers"
      :items="students"
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
        </v-card-title>
      </template>

      <template v-slot:item.username="{ item }">
        <v-chip color="primary" small>
          {{ item.username }}
        </v-chip>
      </template>

      <template v-slot:item.dashboard="{ item }">
        <v-btn :to="openStudentDashboard(item)" color="primary" dark>
          View
        </v-btn>
      </template>
    </v-data-table>
  </v-card>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import { Student } from '@/models/management/Student';

@Component
export default class StudentsView extends Vue {
  students: Student[] = [];
  search: string = '';
  headers: object = [
    { text: 'Name', value: 'name', align: 'center' },
    { text: 'Username', value: 'username', align: 'center' },
    { text: 'Dashboard', value: 'dashboard', align: 'center' }
  ];

  async created() {
    await this.$store.dispatch('loading');
    try {
      this.students = await RemoteServices.getCourseStudents(
        this.$store.getters.getCurrentCourse
      );
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  openStudentDashboard(student: Student) {
    return '/student/user?username=' + student.username;
  }
}
</script>

<style lang="scss" scoped />
