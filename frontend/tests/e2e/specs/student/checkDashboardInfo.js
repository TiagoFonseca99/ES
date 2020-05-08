describe('Student walkthrough', () => {
    beforeEach(() => {
        cy.demoStudentLogin()
        cy.addSubmissionInfo(676)
        cy.addDiscussionsInfo(676)
        cy.addTournamentsInfo()
    })
    afterEach(() => {
        cy.removeDiscussionInfo(676)
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
