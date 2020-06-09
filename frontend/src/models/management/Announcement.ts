export default class Announcement {
  id: number | null = null;
  courseExecutionId!: number;
  userId!: number;
  title: string | null = '';
  content: string | null = '';
  creationDate!: string | null;

  constructor(jsonObj?: Announcement) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.courseExecutionId = jsonObj.courseExecutionId;
      this.userId = jsonObj.userId;
      this.title = jsonObj.title;
      this.content = jsonObj.content;
      this.creationDate = jsonObj.creationDate;
    }
  }
}
