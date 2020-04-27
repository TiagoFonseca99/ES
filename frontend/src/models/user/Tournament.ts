import User from '@/models/user/User';
import Topic from '@/models/management/Topic';

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

  constructor(jsonObj?: Tournament, user?: User) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.startTime = jsonObj.startTime;
      this.endTime = jsonObj.endTime;
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
