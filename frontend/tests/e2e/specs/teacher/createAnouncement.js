describe('Teacher walkthrough', () => {
  beforeEach(() => {
    cy.demoTeacherLogin();
  });

  afterEach(() => {
    cy.contains('Logout').click();
  });

  it('login creates an announcement', () => {
    cy.createAnnouncement(
      'Test Announcement',
      'How many years will this pandemic last? No one really knows. Maybe 1, maybe 2, but I just want to eat a Domino\'s Pizza again'
    );
    //cy.deleteAnnouncement('Test Announcement');
  });

  it('login submits an invalid question', () => {
    cy.log('missing information');
    cy.createInvalidAnnouncement('Test Announcement');

    cy.log('close dialog');
    cy.closeErrorMessage('Announcement must have title and content');

    cy.get('[data-cy="Cancel"]').click();
  });
});
