<template>
  <div class="container">
    <show-announcements id="announcements" v-if="isStudentLoggedIn">
    </show-announcements>
    <h1
      v-if="!isLoggedIn || !isStudentLoggedIn"
      id="home-title"
      class="display-2 font-weight-thin mb-3"
    >
      {{ appName }}
    </h1>

    <v-btn v-if="!isLoggedIn" :href="fenixUrl" depressed color="primary">
      Log in with Fenix <v-icon>fas fa-sign-in-alt</v-icon>
    </v-btn>
    <div class="demo-buttons" v-if="!isLoggedIn">
      <v-btn
        depressed
        small
        color="primary"
        @click="demoStudent"
        data-cy="demoStudentLoginButton"
      >
        <i class="fa fa-graduation-cap" />Demo as student
      </v-btn>
      <v-btn
        depressed
        small
        color="primary"
        @click="demoTeacher"
        data-cy="demoTeacherLoginButton"
      >
        <i class="fa fa-graduation-cap" />Demo as teacher
      </v-btn>
      <v-btn
        depressed
        small
        color="primary"
        @click="demoAdmin"
        data-cy="demoAdminLoginButton"
      >
        <i class="fa fa-user-cog" />Demo as administrator
      </v-btn>
    </div>
    <div v-if="!isLoggedIn" class="white remember" style="cursor: pointer">
      <v-switch
        inset
        class="primary--text"
        v-model="remember"
        label="Remember me"
      />
    </div>
    <v-footer class="footer" v-if="!isStudentLoggedIn">
      <img
        :src="require('../assets/img/ist_optimized.png')"
        class="logo"
        alt="Técnico Logo"
      />
      <div>
        <v-btn
          depressed
          small
          color="secondary"
          href="https://github.com/socialsoftware/quizzes-tutor"
          target="_blank"
        >
          <i class="fab fa-github" /> View code
        </v-btn>
      </div>
      <div>
        <v-btn
          depressed
          small
          color="secondary"
          href="https://github.com/socialsoftware/quizzes-tutor/issues"
          target="_blank"
        >
          <i class="fab fa-github" /> Bug report
        </v-btn>
      </div>
      <img
        :src="require('../assets/img/impress_optimized.png')"
        class="logo"
        alt="IMPRESS Logo"
      />
    </v-footer>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Watch } from 'vue-property-decorator';
import Store from '@/store';
import ShowAnnouncementsView from '@/views/ShowAnnouncementsView.vue';
import * as storage from '@/storage';
import * as session from '@/session';

@Component({ components: { 'show-announcements': ShowAnnouncementsView } })
export default class HomeView extends Vue {
  appName: string = process.env.VUE_APP_NAME;
  fenixUrl: string = process.env.VUE_APP_FENIX_URL;
  remember: boolean = false;
  readonly EXPIRY = 1; // 1 day

  created() {
    this.remember = !(storage.getCookie(session.SESSION_TOKEN) === 'true');
  }

  get isLoggedIn() {
    return Store.getters.isLoggedIn;
  }

  get isStudentLoggedIn() {
    return this.isLoggedIn && Store.getters.isStudent;
  }

  async demoStudent() {
    await this.$store.dispatch('loading');
    try {
      await this.$store.dispatch('demoStudentLogin');
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  async demoTeacher() {
    await this.$store.dispatch('loading');
    try {
      await this.$store.dispatch('demoTeacherLogin');
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  async demoAdmin() {
    await this.$store.dispatch('loading');
    try {
      await this.$store.dispatch('demoAdminLogin');
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  @Watch('remember')
  changeSession() {
    console.log('SESSION: ' + (this.remember ? '1 DAY' : 'CLOSE'));
  }
}
</script>

<style lang="scss" scoped>
.container {
  height: 100%;
  display: flex;
  flex-direction: column;
  flex-wrap: nowrap;
  justify-content: center;
  align-items: center;

  #home-title {
    box-sizing: border-box;
    color: rgb(255, 255, 255);
    min-height: auto;
    min-width: auto;
    text-align: center;
    text-decoration: none solid rgb(255, 255, 255);
    text-rendering: optimizelegibility;
    text-size-adjust: 100%;
    column-rule-color: rgb(255, 255, 255);
    perspective-origin: 229.922px 34px;
    transform-origin: 229.922px 34px;
    caret-color: rgb(255, 255, 255);
    background: rgba(0, 0, 0, 0.75) none no-repeat scroll 0 0 / auto padding-box
      border-box;
    border: 0 none rgb(255, 255, 255);
    font: normal normal 100 normal 45px / 48px Roboto, sans-serif !important;
    margin-bottom: 70px !important;
    outline: rgb(255, 255, 255) none 0;
    padding: 10px 20px;
  }
  
  #announcements {
    height: 90%;
    width: 125%;
  }

  .remember {
    height: auto;
    width: auto;
    padding: 0 15px;
    border-radius: 8px;
  }

  .demo-buttons {
    margin-top: 40px;
    padding-bottom: 30px;

    button {
      margin: 10px;
    }
  }

  .footer {
    background-color: rgba(0, 0, 0, 0) !important;
    display: flex; /* or inline-flex */
    flex-direction: row;
    flex-wrap: wrap;
    justify-content: space-between;
    align-items: center;
    width: 100%;
    max-height: 100px;
    position: absolute;
    bottom: 0;
    overflow: hidden;

    .logo {
      flex-shrink: 1;
      width: 20%;
      max-width: 200px;
      min-width: 100px;
      padding: 2%;
    }
  }
}
</style>
