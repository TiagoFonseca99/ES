describe('Student walkthrough', () => {
    beforeEach(() => {
        cy.demoStudentLogin()
        cy.addSubmission('Test Question', 'DEPRECATED');
        cy.reviewSubmission('Test Question');
    })

    afterEach(() => {
        cy.contains('Logout').click()
    })

    it('login edits and resubmits a question that was rejected', () => {
        cy.seeRejectedQuestionAndResubmit('Test Question');

        cy.resubmitQuestion('New Test Question','How many years will this pandemic last?','1','2','3','300');

        cy.openSubmissions();
        cy.viewQuestion('New Test Question','How many years will this pandemic last?','1','2','3','300');

        cy.deleteSubmission('Test Question');
        cy.deleteSubmission('New Test Question');
    });

    it('login edits and resubmits a question that was rejected with an argument', () => {
        cy.seeRejectedQuestionAndResubmit('Test Question');

        cy.resubmitQuestionArgument('New Test Question','How many years will this pandemic last?','1','2','3','300','Argument');

        cy.openSubmissions();
        cy.viewQuestion('New Test Question','How many years will this pandemic last?','1','2','3','300');

        cy.deleteSubmission('Test Question');
        cy.deleteSubmission('New Test Question');
    });


    it('login edits and resubmits an invalid question', () => {
        cy.seeRejectedQuestionAndResubmit('Test Question');

        cy.resubmitInvalidQuestion('New Test Question');

        cy.closeErrorMessage('Question must have title and content');

        cy.openSubmissions();
        cy.deleteSubmission('Test Question');
    });
});