describe('Teacher walkthrough', () => {

    beforeEach(() => {
        cy.demoTeacherLogin1()
      })
    
    afterEach(() => {
        cy.contains('Logout').click()
    })

    it('login submits a reply', () => {
        cy.exec('PGPASSWORD=tutordb psql -d tutordb -U jpaquete14 -h localhost -c "INSERT INTO discussions (question_id, user_id, content) VALUES ((SELECT id FROM questions WHERE id NOT IN (SELECT question_id FROM discussions WHERE user_id = 676) LIMIT 1), 676, \'content\');"');
        cy.submitReply('Reply Message');

    });

    it('login submits an empty reply', () => {
        cy.exec('PGPASSWORD=tutordb psql -d tutordb -U jpaquete14 -h localhost -c "INSERT INTO discussions (question_id, user_id, content) VALUES ((SELECT id FROM questions WHERE id NOT IN (SELECT question_id FROM discussions WHERE user_id = 676) LIMIT 1), 676, \'content\');"' );
        cy.log('missing reply message')
        cy.submitEmptyReply();

        cy.closeErrorMessage();

    });

})