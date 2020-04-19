import Question from '@/models/management/Question';
import Reply from '@/models/management/Reply';

export default class Discussion {
  content!: string;
  questionId!: number;
  question!: Question;
  replyDto: Reply | undefined = undefined;
  userId?: number;

  constructor(jsonObj?: Discussion) {
    if (jsonObj) {
      this.content = jsonObj.content;
      this.questionId = jsonObj.questionId;
      this.question = new Question(jsonObj.question);
      if (jsonObj.replyDto!) {
        this.replyDto = new Reply(jsonObj.replyDto);
      }
      this.userId = jsonObj.userId;
    }
  }
}
