<template>
  <v-app id="app">
    <top-bar />
    <div class="scrollbar">
      <error-message />
      <loading />
      <router-view />
    </div>
    <cookie-consent
      :show="cookies === null"
      :message="message"
      :link-label="linkLabel"
      :button-label="buttonLabel"
      :target-link="link"
      v-on:hide="acceptCookies"
    />
  </v-app>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import axios from 'axios';
import TopBar from '@/components/TopBar.vue';
import ErrorMessage from '@/components/ErrorMessage.vue';
import Loading from '@/components/Loading.vue';
import CookieConsent from '@/components/CookieConsent.vue';
import * as storage from '@/storage';
import '@/assets/css/_global.scss';
import '@/assets/css/_scrollbar.scss';
import '@/assets/css/_question.scss';
require('typeface-roboto');

@Component({
  components: { TopBar, ErrorMessage, Loading, CookieConsent }
})
export default class App extends Vue {
  readonly COOKIES = 'cookies';
  readonly message =
    'This website uses cookies for authentication. By continuing, you agree with their usage.';
  readonly link = 'https://cookiesandyou.com';
  readonly linkLabel = 'Learn More';
  readonly buttonLabel = 'Got it!';
  cookies: String | null = storage.get(this.COOKIES);

  created() {
    axios.interceptors.response.use(undefined, err => {
      return new Promise(() => {
        if (err.status === 401 && err.config && !err.config.__isRetryRequest) {
          this.$store.dispatch('logout');
        }
        throw err;
      });
    });
  }

  acceptCookies() {
    storage.persist(this.COOKIES, 'true');
    this.cookies = 'true';
  }
}
</script>

<style scoped>
#app {
  background-image: url('assets/img/background.jpg');
  background-position: 0 0;
  background-repeat: no-repeat;
  background-size: 100% 100%;
  height: 100%;
  min-height: 100vh;
  width: 100vw;
  color: #2c3e50;
  content: ' ';
  display: flex;
  flex-direction: column;
  left: 0;
  margin: 0 !important;
  overflow: hidden;
  position: absolute;
  text-align: center;
  top: 0;
  z-index: 1;
}

/*noinspection CssUnusedSymbol*/
.application--wrap {
  min-height: initial !important;
}
</style>
