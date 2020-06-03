import Question from '@/models/management/Question';
import Reply from '@/models/management/Reply';
import { ISOtoString } from '@/services/ConvertDateService';

export default class Discussion {
  userId!: number;
  questionId!: number;
  userName!: string;
  content!: string;
  question!: Question;
  replies!: Reply[] | null;
  date!: string | null;
  available!: boolean;

  constructor(jsonObj?: Discussion) {
    if (jsonObj) {
      this.userId = jsonObj.userId;
      this.questionId = jsonObj.questionId;
      this.userName = jsonObj.userName;
      this.content = jsonObj.content;
      this.question = new Question(jsonObj.question);
      this.date = ISOtoString(jsonObj.date);
      this.available = jsonObj.available;
      if (jsonObj.replies !== null) {
        this.replies = jsonObj.replies.map((reply: any) => {
          return new Reply(reply);
        });
      } else {
        this.replies = null;
      }
    }
  }
}
