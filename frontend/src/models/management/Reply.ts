export default class Reply {
  id!: number;
  content!: string;

  constructor(jsonObj?: Reply) {
    if(jsonObj) {
      this.id = jsonObj.id;
      this.content = jsonObj.content;
    }
  }
}