describe('Teacher walkthrough', () => {

    beforeEach(() => {
      cy.demoTeacherLogin()
      cy.openTeacherQuestions()
  });

    afterEach(() => {
        cy.contains('Logout').click()
    })

    it('login submits a reply', () => {
        cy.exec('PGPASSWORD= psql -d tutordb -U daniel -h localhost -c "INSERT INTO discussions (question_id, user_id, content) VALUES ((SELECT id FROM questions WHERE id NOT IN (SELECT question_id FROM discussions WHERE user_id = 676) LIMIT 1), 676, \'content\');"');
        cy.submitReply('Reply Message');

    });

    it('login submits an empty reply', () => {
        cy.exec('PGPASSWORD= psql -d tutordb -U daniel -h localhost -c "INSERT INTO discussions (question_id, user_id, content) VALUES ((SELECT id FROM questions WHERE id NOT IN (SELECT question_id FROM discussions WHERE user_id = 676) LIMIT 1), 676, \'content\');"' );
        cy.log('missing reply message')
        cy.submitEmptyReply();

        cy.closeErrorMessage();

    });

    it('login changes discussion availability', () => {
        cy.exec('PGPASSWORD= psql -d tutordb -U daniel -h localhost -c "INSERT INTO discussions (question_id, user_id, content) VALUES ((SELECT id FROM questions WHERE id NOT IN (SELECT question_id FROM discussions WHERE user_id = 676) LIMIT 1), 676, \'content\');"' );
        cy.clickAvailability();
    });

})