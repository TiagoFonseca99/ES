describe('Teacher walkthrough', () => {
    beforeEach(() => {
        cy.demoTeacherLogin();
        cy.log('student submits a question');
        cy.addSubmission('Test Question', 'SUBMITTED');
    })

    afterEach(() => {
        cy.contains('Logout').click()
    })

    it('login approves a submission', () => {

        cy.approveSubmissions('Test Question','Excelente Pergunta');

        cy.openTeacherQuestions();
        cy.checkAvailableQuestion('Test Question', 'Question?', 'teste a', 'teste b', 'teste c', 'teste d');


        cy.log('delete student submitted question');
        cy.wait(500);
        cy.deleteQuestion('Test Question');
    });
});