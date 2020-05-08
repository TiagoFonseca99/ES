import StatementOption from '@/models/statement/StatementOption';
import Question from '@/models/management/Question';
import Image from '@/models/management/Image';
import { _ } from 'vue-underscore';
import Discussion from '@/models/management/Discussion';

export default class StatementQuestion {
  question!: Question;
  quizQuestionId!: number;
  content!: string;
  image: Image | null = null;
  hasUserDiscussion!: boolean;
  discussions: Discussion[] | null = null;
  options: StatementOption[] = [];

  constructor(jsonObj?: StatementQuestion) {
    if (jsonObj) {
      this.question = new Question(jsonObj.question);
      this.quizQuestionId = jsonObj.quizQuestionId;
      this.content = jsonObj.content;
      this.image = jsonObj.image;
      this.hasUserDiscussion = jsonObj.hasUserDiscussion;

      if (jsonObj.discussions!) {
        this.discussions = [];
        for (let i = 0; i < jsonObj.discussions!.length; i++)
          this.discussions.push(new Discussion(jsonObj.discussions![i]));
      }

      if (jsonObj.options) {
        this.options = _.shuffle(
          jsonObj.options.map(
            (option: StatementOption) => new StatementOption(option)
          )
        );
      }
    }
  }
}
