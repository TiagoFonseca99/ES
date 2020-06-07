import Question from '@/models/management/Question';

export default class Submission {
  id!: number;
  courseId: number | null = null;
  courseExecutionId!: number;
  questionDto!: Question;
  studentId: number | null = null;
  username: string | null = null;
  argument: string | null = null;
  anonymous!: boolean;

  constructor(jsonObj?: Submission) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.courseId = jsonObj.courseId;
      this.courseExecutionId = jsonObj.courseExecutionId;
      this.questionDto = new Question(jsonObj.questionDto);
      this.studentId = jsonObj.studentId;
      this.username = jsonObj.username;
      this.anonymous = jsonObj.anonymous;
      this.argument = jsonObj.argument;
    }
  }
}
