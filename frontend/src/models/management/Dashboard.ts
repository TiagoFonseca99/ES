import Tournament from '@/models/user/Tournament';

export default class Dashboard {
  name!: string;
  username!: string;
  numDiscussions!: number | null;
  numPublicDiscussions!: number | null;
  numSubmissions!: number | null;
  numApprovedSubmissions!: number | null;
  joinedTournaments!: Tournament[] | null;

  constructor(jsonObj?: Dashboard) {
    if (jsonObj) {
      this.name = jsonObj.name;
      this.username = jsonObj.username;
      this.numDiscussions = jsonObj.numDiscussions;
      this.numPublicDiscussions = jsonObj.numPublicDiscussions;
      this.numSubmissions = jsonObj.numSubmissions;
      this.numApprovedSubmissions = jsonObj.numApprovedSubmissions;

      if (jsonObj.joinedTournaments !== null) {
        this.joinedTournaments = [];
        for (let i = 0; i < jsonObj.joinedTournaments.length; i++) {
          this.joinedTournaments.push(
            new Tournament(jsonObj.joinedTournaments[i])
          );
        }
      }
    }
  }
}
