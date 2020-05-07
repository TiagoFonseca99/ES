describe('Teacher walkthrough', () => {
    beforeEach(() => {
        cy.demoTeacherLogin();
        cy.log('student submits a question');
        cy.exec('PGPASSWORD= psql -d tutordb -U tomas -h localhost -c "WITH quest AS (insert into questions (creation_date, content, title, status, course_id, key) VALUES (current_timestamp, \'OLA?\', \'OIOI\', \'SUBMITTED\', 2, 200) returning id) insert into submissions (question_id, user_id) VALUES ((SELECT id from quest), 676);" ');

    })

    afterEach(() => {
        cy.contains('Logout').click()
    })

    it('login approves a submission', () => {

        cy.ApproveSubmissions('OIOI','Excelente Pergunta');
        cy.log('delete student submited question');
        cy.wait(1000);
        cy.exec('PGPASSWORD= psql -d tutordb -U tomas -h localhost -c "With rev as (delete from reviews where user_id = 677 returning submission_id) delete from submissions where id = (select submission_id from rev); delete from questions where title = \'OIOI\'"');

    });

    it('login rejects a submission', () => {

        cy.RejectSubmissions('OIOI','Porque me apeteceu');
        cy.log('delete student submited question');
        cy.wait(1000);
        cy.exec('PGPASSWORD= psql -d tutordb -U tomas -h localhost -c "With rev as (delete from reviews where user_id = 677 returning submission_id) delete from submissions where id = (select submission_id from rev); delete from questions where title = \'OIOI\'"');

    });

    it('login reviews submisssion without justification', () => {
        cy.log('missing information');
        cy.RejectSubmissions('OIOI',' ');

        cy.closeErrorMessage();

        cy.log('close dialog')
        cy.get('[data-cy="cancelButton"]').click();

        cy.log('delete student submited question');
        cy.exec('PGPASSWORD= psql -d tutordb -U tomas -h localhost -c "delete from submissions where user_id=676; delete from questions where title = \'OIOI\'"');

    });
});