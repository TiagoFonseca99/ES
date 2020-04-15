import Question from '@/models/management/Question';
import Reply from '@/models/management/Reply';

export default class Discussion {
  content!: string;
  questionId!: number;
  question!: Question;
  reply: Reply | undefined = undefined;

  constructor(jsonObj?: Discussion) {
    if (jsonObj) {
      this.content = jsonObj.content;
      this.questionId = jsonObj.questionId;
      this.question = jsonObj.question;
      this.reply = jsonObj.reply;
    }
  }
}
