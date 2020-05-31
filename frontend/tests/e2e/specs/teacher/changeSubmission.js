describe('Teacher walkthrough', () => {
    beforeEach(() => {
        cy.demoTeacherLogin();
        cy.log('student submits a question');
        cy.exec('PGPASSWORD= psql -d tutordb -U dserafim1999 -h localhost -c "WITH quest AS (INSERT INTO questions (title, content, status, course_id, key) VALUES (\'old\', \'old content\', \'SUBMITTED\', 2, 1200) RETURNING id)\nINSERT INTO submissions (question_id, user_id) VALUES ((SELECT id from quest), 676);" ');
        cy.exec('PGPASSWORD= psql -d tutordb -U dserafim1999 -h localhost -c "WITH quest AS (SELECT * FROM questions WHERE key=1200)\nINSERT INTO options(content, correct, question_id, sequence) VALUES (\'teste\', \'t\', (SELECT id FROM quest), 0);" ');
    })

    afterEach(() => {
        cy.contains('Logout').click()
    })

    it('teacher change the submission', () => {
        cy.changeSubmission('old', 'Bom trabalho!!!!', 'Cenas', 'ahhhhhhhhhhhhhh __ hacks!!!!');
        cy.get('[data-cy="submitButton"]').click();
        cy.log('delete student submitted question');
        cy.wait(1000);
        cy.exec('PGPASSWORD= psql -d tutordb -U dserafim1999 -h localhost -c "DELETE FROM reviews WHERE id IN (SELECT max(id) FROM reviews);\n"');
        cy.exec('PGPASSWORD= psql -d tutordb -U dserafim1999 -h localhost -c "WITH sub AS (SELECT question_id FROM submissions WHERE id IN (SELECT max(id) FROM submissions)) DELETE FROM options WHERE question_id IN (SELECT * FROM sub);\nWITH sub AS (DELETE FROM submissions WHERE id IN (SELECT max(id) FROM submissions) RETURNING question_id) DELETE FROM questions WHERE id IN (SELECT * FROM sub);\n"');

    })

    it('teacher change the submission with no correct options', () => {
        cy.changeSubmission('old', 'Bom trabalho!!!!', 'Cenas', 'ahhhhhhhhhhhhhh __ hacks!!!!');
        cy.get('[data-cy="Option1"]').clear();
        cy.get('[data-cy="submitButton"]').click();

        cy.closeErrorMessage();

        cy.log('close dialog')
        cy.get('[data-cy="cancelButton1"]').click();


        cy.log('delete student submitted question');
        cy.wait(1000);
        cy.exec('PGPASSWORD= psql -d tutordb -U dserafim1999 -h localhost -c "DELETE FROM reviews WHERE id IN (SELECT max(id) FROM reviews);\n"');
        cy.exec('PGPASSWORD= psql -d tutordb -U dserafim1999 -h localhost -c "WITH sub AS (SELECT question_id FROM submissions WHERE id IN (SELECT max(id) FROM submissions)) DELETE FROM options WHERE question_id IN (SELECT * FROM sub);\nWITH sub AS (DELETE FROM submissions WHERE id IN (SELECT max(id) FROM submissions) RETURNING question_id) DELETE FROM questions WHERE id IN (SELECT * FROM sub);\n"');

    })

});