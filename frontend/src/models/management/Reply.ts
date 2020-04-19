export default class Reply {
  id!: number;
  teacherId!: number;
  message!: string;
  date!: string | null;

  constructor(jsonObj?: Reply) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.teacherId = jsonObj.teacherId;
      this.message = jsonObj.message;
      this.date = jsonObj.date;
    }
  }
}
