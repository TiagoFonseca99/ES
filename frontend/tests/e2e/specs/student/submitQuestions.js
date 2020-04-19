describe('Student walkthrough', () => {
  beforeEach(() => {
    cy.demoStudentLogin()
  })

  afterEach(() => {
    cy.contains('Logout').click()
  })

  it('login submits a question', () => {
    cy.submitQuestion('Test Question','How many years will this pandemic last?','1','2','3','300');
  });

  it('login submits an invalid question', () => {
    cy.log('missing information')
    cy.submitInvalidQuestion('Invalid Question','How many years will this pandemic last?');

    cy.closeErrorMessage()

    cy.log('close dialog')
    cy.get('[data-cy="cancelButton"]').click()

  });
});
