import User from '@/models/user/User';
import Topic from '@/models/management/Topic';
import { ISOtoString } from '@/services/ConvertDateService';

export default class Tournament {
  id!: number | undefined;
  startTime!: string | undefined;
  endTime!: string | undefined;
  numberOfQuestions!: number | undefined;
  state!: string | undefined;
  courseAcronym!: string | undefined;
  enrolled!: boolean | undefined;
  topics!: String[];
  participants!: User[];
  quizId!: number | undefined;
  privateTournament!: boolean;
  password!: string;

  constructor(jsonObj?: Tournament, user?: User) {
    if (jsonObj) {
      this.id = jsonObj.id;
      if (jsonObj.startTime) {
        this.startTime = ISOtoString(jsonObj.startTime);
      }
      if (jsonObj.endTime) {
        this.endTime = ISOtoString(jsonObj.endTime);
      }
      this.numberOfQuestions = jsonObj.numberOfQuestions;
      this.state = jsonObj.state;
      this.courseAcronym = jsonObj.courseAcronym;
      this.topics = [];

      if (jsonObj.topics) {
        // @ts-ignore
        jsonObj.topics.forEach((topic: Topic) => {
          if (!this.topics.includes(topic.name)) {
            this.topics.push(topic.name);
          }
        });
      }

      const p: User[] = jsonObj.participants;
      this.enrolled = false;
      this.quizId = jsonObj.quizId;
      this.privateTournament = jsonObj.privateTournament;
      this.password = jsonObj.password;
      if (user) {
        p.forEach(pUser => {
          if (user.id == pUser.id) {
            this.enrolled = true;
            return;
          }
        });
        for (let i = 0; i < p!.length; i++) {
          if (user.id == p![i].id) {
            this.enrolled = true;
            return;
          }
        }
      }
    }
  }
}
