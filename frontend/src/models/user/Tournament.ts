import User from '@/models/user/User';

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

      const t: String[] = jsonObj.topics!;
      if (t) {
        for (let i = 0; i < t.length; i++) {
          this.topics.push(' ' + t[i]);
        }
      }
      const p: User[] = jsonObj.participants;
      if (user) {
        for (let i = 0; i < p!.length; i++) {
          if (user.id == p![i].id) {
            this.enrolled = true;
            return;
          }
        }
        this.enrolled = false;
      } else {
        this.enrolled = false;
      }
    }
  }
}
