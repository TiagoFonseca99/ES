import Vue from 'vue';
import Vuex from 'vuex';
import RemoteServices from '@/services/RemoteServices';
import Course from '@/models/user/Course';
import User from '@/models/user/User';
import * as storage from '@/storage';
import * as session from '@/session';

interface State {
  logged: boolean;
  session: boolean;
  user: User | null;
  currentCourse: Course | null;
  error: boolean;
  errorMessage: string;
  loading: boolean;
  inQuiz: boolean;
}

const state: State = {
  logged: false,
  session: true,
  user: null,
  currentCourse: null,
  error: false,
  errorMessage: '',
  loading: false,
  inQuiz: false
};

Vue.use(Vuex);
Vue.config.devtools = true;

export default new Vuex.Store({
  state: state,
  mutations: {
    async login(state, user: User) {
      state.user = user;
      state.logged = true;

      let sessionVal = storage.getCookie(session.SESSION_TOKEN);
      if (sessionVal == 'true' || sessionVal == 'false') {
        state.session = sessionVal == 'true';
      }

      storage.createCookie(session.SESSION_TOKEN, String(state.session));
      storage.persist(session.LOGGED_TOKEN, 'true');
      await session.checkCourse(state.user);
    },
    async logout(state) {
      state.user = null;
      state.currentCourse = null;
      state.logged = false;
      await session.logout();
    },
    updateUser(state, user: User) {
      state.user = user;
    },
    toggleInQuiz(state, inQuiz: boolean) {
      state.inQuiz = inQuiz;
    },
    session(state, session) {
      state.session = session;
    },
    error(state, errorMessage: string) {
      state.error = true;
      state.errorMessage = errorMessage;
    },
    clearError(state) {
      state.error = false;
      state.errorMessage = '';
    },
    loading(state) {
      state.loading = true;
    },
    clearLoading(state) {
      state.loading = false;
    },
    currentCourse(state, currentCourse: Course) {
      storage.persist(session.COURSE_TOKEN, JSON.stringify(currentCourse));
      state.currentCourse = currentCourse;
    }
  },
  actions: {
    error({ commit }, errorMessage) {
      commit('error', errorMessage);
    },
    clearError({ commit }) {
      commit('clearError');
    },
    loading({ commit }) {
      commit('loading');
    },
    clearLoading({ commit }) {
      commit('clearLoading');
    },
    async fenixLogin({ commit }, code) {
      const authResponse = await RemoteServices.fenixLogin(code);
      await commit('login', authResponse);
    },
    async demoStudentLogin({ commit }) {
      const user = await RemoteServices.demoStudentLogin();
      await commit('login', user);
    },
    async demoTeacherLogin({ commit }) {
      const user = await RemoteServices.demoTeacherLogin();
      await commit('login', user);
    },
    async demoAdminLogin({ commit }) {
      const user = await RemoteServices.demoAdminLogin();
      await commit('login', user);
    },
    logout({ commit }) {
      return new Promise(async resolve => {
        await commit('logout');
        resolve();
      });
    },
    currentCourse({ commit }, currentCourse) {
      commit('currentCourse', currentCourse);
    },
    updateUser({ commit }, user) {
      commit('updateUser', user);
    },
    toggleInQuiz({ commit }, inQuiz) {
      commit('toggleInQuiz', inQuiz);
    }
  },
  getters: {
    isLoggedIn(state): boolean {
      return state.logged;
    },
    isAdmin(state): boolean {
      return (
        state.user !== null &&
        (state.user.role == 'ADMIN' || state.user.role == 'DEMO_ADMIN')
      );
    },
    isTeacher(state): boolean {
      return state.user !== null && state.user.role == 'TEACHER';
    },
    isStudent(state): boolean {
      return state.user !== null && state.user.role == 'STUDENT';
    },
    getUser(state): User | null {
      return state.user;
    },
    getCurrentCourse(state): Course | null {
      return state.currentCourse;
    },
    getError(state): boolean {
      return state.error;
    },
    getErrorMessage(state): string {
      return state.errorMessage;
    },
    getLoading(state): boolean {
      return state.loading;
    },
    outOfQuiz(state): boolean {
      return !state.inQuiz;
    }
  }
});
