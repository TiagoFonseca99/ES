<template>
  <transition class="cookie-consent-transition">
    <div v-if="show" class="cookie-consent">
      <slot name="message">
        <span class="cookie-consent-message"
          >{{ message }}
          <slot name="link">
            <a :href="targetLink" target="_blank" class="cookie-consent-link">{{
              linkLabel
            }}</a>
          </slot>
        </span>
      </slot>
      <section @click="hide()">
        <slot name="dismiss">
          <v-btn class="primary cookie-consent-dismiss" data-cy="cookies">
            {{ buttonLabel }}
          </v-btn>
        </slot>
      </section>
    </div>
  </transition>
</template>

<script lang="ts">
import { Component, Emit, Prop, Vue } from 'vue-property-decorator';

@Component
export default class CookieConsent extends Vue {
  @Prop(String) readonly message!: string;
  @Prop(String) readonly targetLink!: string;
  @Prop(String) readonly linkLabel!: string;
  @Prop(String) readonly buttonLabel!: string;
  @Prop(Boolean) readonly show!: boolean;

  @Emit('hide')
  hide() {}
}
</script>

<style lang="scss" scoped>
$cookieconsent-padding: 1rem 0 !default;
$cookieconsent-space: 0.5rem 1rem !default;
$cookieconsent-position: fixed !default;
$cookieconsent-z-index: 9999 !default;
$cookieconsent-width: 100% !default;

$cookieconsent-compliance-padding: 0.5rem 2rem !default;
$cookieconsent-compliance-border: 2px solid currentColor !default;

.cookie-consent {
  position: $cookieconsent-position;
  z-index: $cookieconsent-z-index;
  width: $cookieconsent-width;
  padding: $cookieconsent-padding;
  left: 0;
  right: 0;
  bottom: 0;

  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;

  background-color: rgba(206, 212, 220, 0.7);

  &-message,
  &-dismiss {
    margin: $cookieconsent-space;
  }

  &-dismiss {
    cursor: pointer;
    padding: $cookieconsent-compliance-padding;
    border: $cookieconsent-compliance-border;
  }

  &-link {
    text-decoration: none;
  }

  &-link:hover {
    text-decoration: underline;
  }
}

.cookie-consent-transition {
  &-leave-active {
    transition: transform 0.75s;
    transition-timing-function: cubic-bezier(0.75, 0, 0, 1);
  }
  &-leave-to {
    transform: translateY(100%);
  }
}
</style>
