<template>
  <div id="app">
    Not Found
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';

@Component
export default class NotFoundView extends Vue {
  readonly ERROR = 'Page not found';

  created() {
    this.$store.dispatch('error', this.ERROR);
    this.$router.push({ name: 'home' });
  }
}
</script>

<style lang="scss" scoped></style>
