describe('Student walkthrough', () => {
  beforeEach(() => {
    cy.demoStudentLogin();
    cy.contains('Tournaments')
      .should('be.visible')
      .click();
    cy.contains('All')
      .should('be.visible')
      .click();
    cy.wait(100);
  });

  afterEach(() => {
    cy.contains('Logout').click();
  });

  it('login creates a tournament', () => {
    cy.createTournament('3');
  });

  it('login sees open tournaments', () => {
    cy.get('[data-cy="changeButton"]')
      .should('be.visible')
      .click();
  });

  it('login joins tournament', () => {
    cy.joinTournament('-1');
  });

  it('login creates a tournament and joins', () => {
    cy.createTournament('3');
    cy.wait(100);
    cy.joinTournament('0');
  });

  it('login joins tournament already in', () => {
    cy.log('try to join again');
    cy.joinTournament('-1');

    cy.closeErrorMessage();
    cy.log('close dialog');
  });

  it('login edits tournament', () => {
    cy.contains('Tournaments')
      .should('be.visible')
      .click();
    cy.contains('My Tournaments')
      .should('be.visible')
      .click();
    cy.wait(100);
    cy.editTournament('-1');
  });

  it('login cancel tournament', () => {
    cy.contains('Tournaments')
      .should('be.visible')
      .click();
    cy.contains('My Tournaments')
      .should('be.visible')
      .click();
    cy.wait(100);
    cy.cancelTournament('-1');
  });
});
