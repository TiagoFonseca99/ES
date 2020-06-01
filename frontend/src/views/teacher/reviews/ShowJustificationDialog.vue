<template>
  <v-dialog v-model="dialog" @keydown.esc="closeReviewDialog" max-width="75%">
    <v-card>
      <v-card-title>
        <span class="headline">Justification</span>
      </v-card-title>

      <v-card-text class="text-left">
        <show-review :review="review" />
      </v-card-text>

      <v-card-actions>
        <v-spacer />
        <v-btn
          data-cy="close"
          dark
          color="blue darken-1"
          @click="closeReviewDialog"
          >close</v-btn
        >
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { Component, Vue, Prop, Watch, Model } from 'vue-property-decorator';
import Review from '@/models/management/Review';
import ShowReview from '@/views/teacher/reviews/ShowJustification.vue';

@Component({
  components: {
    'show-review': ShowReview
  }
})
export default class ShowReviewDialog extends Vue {
  @Prop({ type: Review, required: true }) readonly review!: Review;
  @Model('dialog', Boolean) dialog!: boolean;

  closeReviewDialog() {
    this.$emit('close-show-review-dialog');
  }
}
</script>
