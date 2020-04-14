import Image from '@/models/management/Image';

export default class Review {
    id: number | null = null;
    teacherId: number | null = null;
    submissionId: number | null = null;
    studentId: number | null = null;
    image: Image | null = null;
    justification: string | null = '';
    status: string = '';


    constructor(jsonObj?: Review) {
        if (jsonObj) {
            this.id = jsonObj.id;
            this.teacherId = jsonObj.teacherId;
            this.submissionId = jsonObj.submissionId;
            this.studentId = jsonObj.studentId;
            this.image = jsonObj.image;
            this.justification = jsonObj.justification;
            this.status = jsonObj.status;
        }
    }
}


