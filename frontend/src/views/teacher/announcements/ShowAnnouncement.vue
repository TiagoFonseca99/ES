<template>
  <div>
    <span v-html="convertMarkDown(announcement.content)" />
  </div>
</template>

<script lang="ts">
import { Component, Vue, Prop } from 'vue-property-decorator';
import { convertMarkDown } from '@/services/ConvertMarkdownService';
import Image from '@/models/management/Image';
import Announcement from '@/models/management/Announcement';

@Component
export default class ShowAnnouncement extends Vue {
  @Prop({ type: Announcement, required: true })
  readonly announcement!: Announcement;

  convertMarkDown(text: string, image: Image | null = null): string {
    return convertMarkDown(text);
  }
}
</script>
