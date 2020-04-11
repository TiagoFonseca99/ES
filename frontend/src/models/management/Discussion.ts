import Question from '@/models/management/Question';

export default class Discussion {
  content!: string;
  questionId!: number;
  question!: Question;

  constructor(jsonObj?: Discussion) {
    if (jsonObj) {
      this.content = jsonObj.content;
      this.questionId = jsonObj.questionId;
      this.question = jsonObj.question;
    }
  }
}
