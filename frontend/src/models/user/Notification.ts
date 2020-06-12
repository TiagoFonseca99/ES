import { ISOtoString } from '@/services/ConvertDateService';

export default class Notification {
  id: number | null = null;
  title: string | null = '';
  content: string | null = '';
  creationDate!: string | null;

  constructor(jsonObj?: Notification) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.title = jsonObj.title;
      this.content = jsonObj.content;
      this.creationDate = ISOtoString(jsonObj.creationDate);
    }
  }
}
