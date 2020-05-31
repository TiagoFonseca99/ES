describe('Teacher walkthrough', () => {
    beforeEach(() => {
        cy.demoTeacherLogin();
        cy.log('student submits a question');
        cy.exec('PGPASSWORD= psql -d tutordb -U dserafim1999 -h localhost -c "WITH quest AS (insert into questions (creation_date, content, title, status, course_id, key) VALUES (current_timestamp, \'OLA?\', \'OIOI\', \'SUBMITTED\', 2, 200) returning id) insert into submissions (question_id, user_id) VALUES ((SELECT id from quest), 676);" ');

    })

    afterEach(() => {
        cy.contains('Logout').click()
    })

    it('login approves a submission', () => {

        cy.approveSubmissions('OIOI','Excelente Pergunta');
        cy.log('delete student submited question');
        cy.wait(1000);
        cy.exec('PGPASSWORD= psql -d tutordb -U dserafim1999 -h localhost -c "WITH rev AS (DELETE FROM reviews WHERE id IN (SELECT max(id) FROM reviews) RETURNING submission_id), sub AS (DELETE FROM submissions WHERE id IN (SELECT * FROM rev) RETURNING question_id) DELETE FROM questions WHERE id IN (SELECT * FROM sub);"');

    });

    it('login rejects a submission', () => {

        cy.rejectSubmissions('OIOI','Porque me apeteceu');
        cy.log('delete student submited question');
        cy.wait(1000);
        cy.exec('PGPASSWORD= psql -d tutordb -U dserafim1999 -h localhost -c "WITH rev AS (DELETE FROM reviews WHERE id IN (SELECT max(id) FROM reviews) RETURNING submission_id), sub AS (DELETE FROM submissions WHERE id IN (SELECT * FROM rev) RETURNING question_id) DELETE FROM questions WHERE id IN (SELECT * FROM sub);"');

    });

    it('login reviews submisssion without justification', () => {
        cy.log('missing information');
        cy.rejectSubmissions('OIOI',' ');

        cy.closeErrorMessage();

        cy.log('close dialog')
        cy.get('[data-cy="cancelButton"]').click();

        cy.log('delete student submited question');
        cy.exec('PGPASSWORD= psql -d tutordb -U dserafim1999 -h localhost -c "WITH sub AS (DELETE FROM submissions WHERE id IN (SELECT max(id) FROM submissions) RETURNING question_id) DELETE FROM questions WHERE id IN (SELECT * FROM sub);"');

    });
});