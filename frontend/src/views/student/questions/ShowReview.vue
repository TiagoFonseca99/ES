<template>
  <div>
    <span v-html="convertMarkDown(review.justification, review.imageDto)" />
  </div>
</template>

<script lang="ts">
import { Component, Vue, Prop } from 'vue-property-decorator';
import { convertMarkDown } from '@/services/ConvertMarkdownService';
import Review from '@/models/management/Review';
import Image from '@/models/management/Image';

@Component
export default class ShowReview extends Vue {
  @Prop({ type: Review, required: true }) readonly review!: Review;

  convertMarkDown(text: string, image: Image | null = null): string {
    return convertMarkDown(text, image);
  }
}
</script>
