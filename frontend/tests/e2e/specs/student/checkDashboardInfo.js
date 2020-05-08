describe('Student walkthrough', () => {
  beforeEach(() => {
    cy.demoStudentLogin();
    //cy.addSubmissionInfo();
    // add discussions/replies
    cy.addTournamentsInfo();
  });
  afterEach(() => {
    // remove discussions/replies
    //cy.removeSubmissionInfo();
    cy.contains('Logout').click();
  });

  it('login checks dashboard info', () => {
    cy.openDashboard();
    cy.checkUserInfo('Demo Student', 'Demo-Student');

    cy.checkTournamentsInfo();
    //cy.checkSubmissionsInfo(3, 1, 2);
    // check discussions info
  });
});
