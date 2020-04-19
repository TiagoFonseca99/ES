describe('Teacher walkthrough', () => {
    beforeEach(() => {
        cy.demoTeacherLogin();
        cy.log('student submits a question');
        cy.exec('PGPASSWORD=your_pass psql -d tutordb -U your_user -h localhost -c "WITH quest AS (insert into questions (creation_date, content, title, status, course_id, key) VALUES (current_timestamp, \'OLA?\', \'OIOI\', \'SUBMITTED\', 2, 200) returning id) insert into submissions (question_id, user_id) VALUES ((SELECT id from quest), 676);" ');

    })

    afterEach(() => {
        cy.contains('Logout').click()
    })

    it('login, approves a submission', () => {

        cy.ApproveSubmissions('OIOI','Excelente Pergunta');

        cy.exec('PGPASSWORD=your_pass psql -d tutordb -U your_user -h localhost -c "With rev as (delete from reviews where user_id = 677 returning submission_id), subs as (delete from submissions where id = (select id from rev) returning question_id) delete from questions where id = (select question_id from subs);"');

    });

    it('login, rejects a submission', () => {

        cy.RejectSubmissions('OIOI','Porque me apeteceu');
        cy.TeacherDeleteSubmission('OIOI');
        
    });

    it('login, reviews submisssion without justification', () => {
        cy.log('missing information');
        cy.ApproveSubmissions('OIOI',' ');

        cy.closeErrorMessage();

        cy.log('close dialog')
        cy.get('[data-cy="cancelButton"]').click();

        cy.TeacherDeleteSubmission('OIOI');

    });
});