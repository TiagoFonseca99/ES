import Image from '@/models/management/Image';

export default class Review {
  id!: number;
  studentId: number | null = null;
  teacherId: number | null = null;
  submissionId: number | null = null;
  justification!: string;
  imageDto: Image | null = null;
  status!: string;

  constructor(jsonObj?: Review) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.studentId = jsonObj.studentId;
      this.teacherId = jsonObj.teacherId;
      this.submissionId = jsonObj.submissionId;
      this.justification = jsonObj.justification;
      this.imageDto = jsonObj.imageDto;
      this.status = jsonObj.status;
    }
  }
}
