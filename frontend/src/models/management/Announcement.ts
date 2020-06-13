import { ISOtoString } from '@/services/ConvertDateService';

export default class Announcement {
  id: number | null = null;
  courseExecutionId!: number;
  userId!: number;
  username!: string;
  title: string | null = '';
  content: string | null = '';
  creationDate!: string | null;

  constructor(jsonObj?: Announcement) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.courseExecutionId = jsonObj.courseExecutionId;
      this.userId = jsonObj.userId;
      this.username = jsonObj.username;
      this.title = jsonObj.title;
      this.content = jsonObj.content;
      this.creationDate = ISOtoString(jsonObj.creationDate);
    }
  }
}
