import { ISOtoString } from '@/services/ConvertDateService';

export default class Reply {
  id!: number;
  userId!: number;
  userName!: string;
  message!: string;
  date!: string | null;

  constructor(jsonObj?: Reply) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.userId = jsonObj.userId;
      this.userName = jsonObj.userName;
      this.message = jsonObj.message;
      this.date = ISOtoString(jsonObj.date);
    }
  }
}
