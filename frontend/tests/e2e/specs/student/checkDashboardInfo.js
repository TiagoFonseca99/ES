describe('Student walkthrough', () => {
    beforeEach(() => {
        cy.demoStudentLogin()
        cy.addSubmissionInfo()
        cy.addDiscussionsInfo()
        cy.addTournamentsInfo();
    })
    afterEach(() => {

        cy.removeDiscussionInfo()
        cy.removeSubmissionInfo()
        cy.contains('Logout').click()
    })

    it('login checks dashboard info', () => {
        cy.openDashboard();
        cy.checkUserInfo('Demo Student', 'Demo-Student');

        cy.checkTournamentsInfo();
        cy.checkSubmissionsInfo(3,1,2);
        cy.checkDiscussionsInfo(2, 1);

    });
});
