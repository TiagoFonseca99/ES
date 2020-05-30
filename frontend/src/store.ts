import Vue from 'vue';
import Vuex from 'vuex';
import RemoteServices from '@/services/RemoteServices';
import AuthDto from '@/models/user/AuthDto';
import Course from '@/models/user/Course';
import User from '@/models/user/User';
import * as storage from '@/storage';
import * as session from '@/session';

interface State {
  token: string;
  logged: boolean;
  session: boolean;
  user: User | null;
  currentCourse: Course | null;
  error: boolean;
  errorMessage: string;
  loading: boolean;
}

const state: State = {
  token: '',
  logged: false,
  session: true,
  user: null,
  currentCourse: null,
  error: false,
  errorMessage: '',
  loading: false
};

Vue.use(Vuex);
Vue.config.devtools = true;

export default new Vuex.Store({
  state: state,
  mutations: {
    login(state, authResponse: AuthDto) {
      state.token = authResponse.token;
      state.user = authResponse.user;
      state.logged = true;

      let sessionVal = storage.getLocal(session.SESSION_TOKEN);
      if (sessionVal == 'true' || sessionVal == 'false') {
        state.session = sessionVal == 'true';
      }

      storage.removeAll(session.LOGIN_TOKEN);
      storage.persist(session.SESSION_TOKEN, String(state.session), false);
      storage.persist(session.LOGIN_TOKEN, authResponse.token, state.session);
    },
    logout(state) {
      state.token = '';
      state.user = null;
      state.currentCourse = null;
      state.logged = false;
      storage.removeAll(session.LOGIN_TOKEN);
      storage.removeAll(session.COURSE_TOKEN);
      storage.persist(session.SESSION_TOKEN, String(state.session), false);
    },
    token(state, token) {
      state.token = token;
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
      storage.persist(
        session.COURSE_TOKEN,
        JSON.stringify(currentCourse),
        state.session
      );
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
      commit('login', authResponse);
    },
    async demoStudentLogin({ commit }) {
      const authResponse = await RemoteServices.demoStudentLogin();
      commit('login', authResponse);
      commit(
        'currentCourse',
        (Object.values(authResponse.user.courses)[0] as Course[])[0]
      );
    },
    async demoTeacherLogin({ commit }) {
      const authResponse = await RemoteServices.demoTeacherLogin();
      commit('login', authResponse);
      commit(
        'currentCourse',
        (Object.values(authResponse.user.courses)[0] as Course[])[0]
      );
    },
    async demoAdminLogin({ commit }) {
      const authResponse = await RemoteServices.demoAdminLogin();
      commit('login', authResponse);
    },
    logout({ commit }) {
      return new Promise(resolve => {
        commit('logout');
        resolve();
      });
    },
    currentCourse({ commit }, currentCourse) {
      commit('currentCourse', currentCourse);
    }
  },
  getters: {
    isLoggedIn(state): boolean {
      return state.logged;
    },
    isAdmin(state): boolean {
      return (
        !!state.token &&
        state.user !== null &&
        (state.user.role == 'ADMIN' || state.user.role == 'DEMO_ADMIN')
      );
    },
    isTeacher(state): boolean {
      return (
        !!state.token && state.user !== null && state.user.role == 'TEACHER'
      );
    },
    isStudent(state): boolean {
      return (
        !!state.token && state.user !== null && state.user.role == 'STUDENT'
      );
    },
    getToken(state): string {
      return state.token;
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
    }
  }
});
