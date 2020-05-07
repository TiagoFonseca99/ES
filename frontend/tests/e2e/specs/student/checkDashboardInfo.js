describe('Student walkthrough', () => {
    beforeEach(() => {
        cy.demoStudentLogin()
        cy.addSubmissionInfo()
        // add discussions/replies
        // add tournaments
    })
    afterEach(() => {

        // remove discussions/replies
        // remove tournaments
        cy.removeSubmissionInfo()
        cy.contains('Logout').click()
    })

    it('login checks dashboard info', () => {
        cy.openDashboard();
        cy.checkUserInfo('Demo Student', 'Demo-Student');

        //check tournaments info
        cy.checkSubmissionsInfo(3,1,2);
        // check discussions info

    });
});


