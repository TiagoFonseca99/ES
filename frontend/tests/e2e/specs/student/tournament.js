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
    cy.joinTournament('-1');
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

  it('login creates, joins and leaves tournament', () => {
    cy.createTournament('3');
    cy.wait(100);
    cy.joinTournament('-1');
    cy.leaveTournament('-1');
  });

  it('login leaves tournament', () => {
    cy.get('[data-cy="changeButton"]')
      .should('be.visible')
      .click();
    cy.wait(100);
    cy.leaveTournament('-1');
  });

  it('login joins and solves tournament', () => {
    cy.get('[data-cy="changeButton"]')
      .should('be.visible')
      .click();
    cy.wait(100);
    cy.joinTournament('0');
    cy.solveTournament('0');
  });
});
