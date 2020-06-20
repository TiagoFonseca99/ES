export default class Notification {
  id: number | null = null;
  title: string | null = '';
  content: string | null = '';
  creationDate!: string;
  type!: string;

  constructor(jsonObj?: Notification) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.title = jsonObj.title;
      this.content = jsonObj.content;
      this.creationDate = jsonObj.creationDate;
      this.type = jsonObj.type;
    }
  }
}
