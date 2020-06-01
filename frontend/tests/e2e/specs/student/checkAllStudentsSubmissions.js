describe('Student walkthrough', () => {
    beforeEach(() => {
        cy.demoStudentLogin()
        cy.addSubmission('Test Question1', 'SUBMITTED', 676, false);
        cy.addSubmission('Test Question2', 'SUBMITTED', 678, true);
        cy.addSubmission('Test Question3', 'SUBMITTED', 679, true);
    })

    afterEach(() => {
        cy.contains('Logout').click()
    })

    it('login checks all submissions for course execution', () => {
        cy.checkAllStudentsSubmission('Test Question1', 'Test Question2', 'Test Question3');

        cy.openSubmissions();

        cy.deleteSubmission('Test Question1');
        cy.exec(
            'PASSWORD= psql -d tutordb -U dserafim1999 -h localhost -c "WITH q AS (SELECT id FROM questions WHERE id IN (SELECT max(id) FROM questions)), opt AS (DELETE FROM options WHERE question_id IN (SELECT * FROM q)), sub AS (DELETE FROM submissions WHERE question_id IN (SELECT * FROM q)) DELETE FROM questions WHERE id IN (SELECT * FROM q);" '
        );
        cy.exec(
            'PASSWORD= psql -d tutordb -U dserafim1999 -h localhost -c "WITH q AS (SELECT id FROM questions WHERE id IN (SELECT max(id) FROM questions)), opt AS (DELETE FROM options WHERE question_id IN (SELECT * FROM q)), sub AS (DELETE FROM submissions WHERE question_id IN (SELECT * FROM q)) DELETE FROM questions WHERE id IN (SELECT * FROM q);" '
        );
    });
});