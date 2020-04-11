import Question from '@/models/management/Question';

export default class Submission {
  id: number | null = null;
  courseId: number | null = null;
  questionDto!: Question;
  studentId: number | null = null;

  constructor(jsonObj?: Submission) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.courseId = jsonObj.courseId;
      this.questionDto = new Question(jsonObj.questionDto);
      this.studentId = jsonObj.studentId;
    }
  }
}
