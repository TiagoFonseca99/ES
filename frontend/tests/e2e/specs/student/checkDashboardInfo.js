describe('Student walkthrough', () => {
    beforeEach(() => {
        cy.demoStudentLogin()
        cy.addSubmissionInfo()
        cy.addDiscussionsInfo()
        // add tournaments
    })
    afterEach(() => {

        cy.removeDiscussionInfo()
        // remove tournaments
        cy.removeSubmissionInfo()
        cy.contains('Logout').click()
    })

    it('login checks dashboard info', () => {
        cy.openDashboard();
        cy.checkUserInfo('Demo Student', 'Demo-Student');

        //check tournaments info
        cy.checkSubmissionsInfo(3,1,2);
        cy.checkDiscussionsInfo(2, 1);

    });
});


