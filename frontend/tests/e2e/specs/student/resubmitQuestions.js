describe('Student walkthrough', () => {
    beforeEach(() => {
        cy.demoStudentLogin()
    })

    afterEach(() => {
        cy.contains('Logout').click()
    })

    it('login edits and resubmits a rejected question', () => {
        // add submission
        cy.addSubmission('Test Question');
        // add rejected review
        cy.reviewSubmission('Test Question');

        // resubmit
        //cy.resubmitQuestion('New Test Question','How many years will this pandemic last?','1','2','3','300');



        //go back to submissions
        cy.openSubmissions();

        //view question


        //delete both submissions
        cy.deleteSubmission('Test Question');
        //cy.deleteSubmission('New Test Question');
    });

});