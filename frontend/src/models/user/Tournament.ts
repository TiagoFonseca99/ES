import User from '@/models/user/User';

export default class Tournament {
  id!: number | undefined;
  startTime!: string | undefined;
  endTime!: string | undefined;
  numberOfQuestions!: number | undefined;
  state!: string | undefined;
  courseAcronym!: string | undefined;
  enrolled!: boolean | undefined;
  topics!: Array<string> | undefined;
  participants!: Array<User> | undefined;

  constructor(jsonObj?: Tournament, user?: User) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.startTime = jsonObj.startTime;
      this.endTime = jsonObj.endTime;
      this.numberOfQuestions = jsonObj.numberOfQuestions;
      this.state = jsonObj.state;
      this.courseAcronym = jsonObj.courseAcronym;
      this.topics = new Array<string>(0);

      const t = jsonObj.topics;
      // @ts-ignore
      for (var i=0; i<t?.length; i++){
        if (t) {
          this.topics?.push(' ' + t[i].name);
        }
      }
      const p = jsonObj.participants;
      if (user) {
        // @ts-ignore
        for (var i=0; i<p?.length; i++){
          if (user.id == !(p) || p[i].id) {
            this.enrolled = true;
            return;
          }
        }
        this.enrolled = false;
      }
      else {
        this.enrolled = false;
      }
    }
  }
}