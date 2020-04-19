describe('Student walkthrough', () => {
  beforeEach(() => {
    cy.demoStudentLogin()
  })

  afterEach(() => {
    cy.contains('Logout').click()
  })

  it('login submits a question', () => {
    cy.submitQuestion('Test Question','How many years will this pandemic last?','1','2','3','300');

    cy.viewQuestion('Test Question');
    cy.deleteSubmission('Test Question');
  });

  it('login submits an invalid question', () => {
    cy.log('missing information')
    cy.submitInvalidQuestion('Invalid Question','How many years will this pandemic last?');

    cy.closeErrorMessage()

    cy.log('close dialog')
    cy.get('[data-cy="cancelButton"]').click()

    });

  it('login submits a question and checks review status', () => {
    cy.submitQuestion('Test Question','How many years will this pandemic last?','1','2','3','300');

    cy.log('teacher reviews submission')
    cy.exec('PGPASSWORD= psql -d tutordb -U dserafim1999 -h localhost -c "with sub as (select s.id from submissions s join questions q on s.question_id=q.id where q.title=\'Test Question\') insert into reviews(creation_date,justification,status,student_id,submission_id,user_id) values (current_timestamp,\'Excelente pergunta\', \'APPROVED\', 676, (select * from sub), 677);" ')

    cy.getSubmissionStatus('Test Question', 'APPROVED','Excelente Pergunta');

    cy.log('student deletes submission');
    cy.openSubmissions();
    cy.deleteSubmission('Test Question');
  });
});