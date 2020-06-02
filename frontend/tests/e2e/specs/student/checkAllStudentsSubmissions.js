describe('Student walkthrough', () => {
    beforeEach(() => {
        cy.demoStudentLogin()
        cy.addSubmission('Test Question1', 'DEPRECATED', 676, false);
        cy.addSubmission('Test Question2', 'AVAILABLE', 678, true);
        cy.addSubmission('Test Question3', 'DEPRECATED', 679, true);
    })

    afterEach(() => {
        cy.exec(
            'PGPASSWORD=fartodisto psql -d tutordb -U tomas -h localhost -c "WITH q AS (SELECT id FROM questions WHERE id IN (SELECT max(id) FROM questions)), opt AS (DELETE FROM options WHERE question_id IN (SELECT * FROM q)), sub AS (DELETE FROM submissions WHERE question_id IN (SELECT * FROM q)) DELETE FROM questions WHERE id IN (SELECT * FROM q);" '
        );
        cy.exec(
            'PGPASSWORD=fartodisto psql -d tutordb -U tomas -h localhost -c "WITH q AS (SELECT id FROM questions WHERE id IN (SELECT max(id) FROM questions)), opt AS (DELETE FROM options WHERE question_id IN (SELECT * FROM q)), sub AS (DELETE FROM submissions WHERE question_id IN (SELECT * FROM q)) DELETE FROM questions WHERE id IN (SELECT * FROM q);" '
        );
        cy.contains('Logout').click()
    })

    it('login checks all submissions for course execution', () => {
        cy.checkAllStudentsSubmission('Test Question1', 'Test Question2', 'Test Question3');

        cy.openSubmissions();

        cy.deleteSubmission('Test Question1');

    });

    it('login, sees all questions, sees questions excluding the ones submitted by himself', () => {
        cy.contains('Questions').click();
        cy.contains('All Submissions').click();
        cy.contains('All Submissions').click();

        cy.contains('Test Question1');
        cy.contains('Test Question2');
        cy.contains('Test Question3');

        cy.seesExcludingSubmissions('Test Question2', 'Test Question3');

        cy.exec(
            'PGPASSWORD=fartodisto psql -d tutordb -U tomas -h localhost -c "WITH q AS (SELECT id FROM questions WHERE id IN (SELECT max(id) FROM questions)), opt AS (DELETE FROM options WHERE question_id IN (SELECT * FROM q)), sub AS (DELETE FROM submissions WHERE question_id IN (SELECT * FROM q)) DELETE FROM questions WHERE id IN (SELECT * FROM q);" '
        );
    });
});