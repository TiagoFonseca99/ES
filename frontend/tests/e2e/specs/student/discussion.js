describe('Student discussion walkthrough', () => {
  beforeEach(() => {
    cy.demoStudentLogin();
    cy.removeAllReplies();
    cy.removeAllDiscussions();
  });

  afterEach(() => {
    cy.contains('Logout').click();
  });

  it('login answer quiz create discussion and delete', () => {
    cy.answerQuiz(0);
    cy.writeDiscussion('I don\' know what this question means');
    cy.log('Teacher replies to discussion');
    cy.replyTeacherDiscussion('I think the question is pretty straightforward');
    cy.viewMyDiscussions();
    cy.openDiscussion(0);
    cy.replyDiscussion('I still don\'t understand');
    cy.wait(1000);
    cy.removeDiscussion();
    cy.removeAnswers();
  });

  it('login answer quiz create empty discussion, good discussion and edit', () => {
    cy.answerQuiz(0);
    cy.writeDiscussion(' ');
    cy.closeErrorMessage();
    cy.writeDiscussion('asjdhsajhdgsadhjsagdhjsagdhjagsdhja');
    cy.wait(1000);
    cy.editDiscussion('I don\'t know what is this');
    cy.wait(1000);
    cy.removeAnswers();
  });

  it('login, answer quiz create discussion view discussions return to quiz to view solved', () => {
    cy.answerQuiz(0);
    cy.writeDiscussion('I don\'t know what this question means');
    cy.viewMyDiscussions();
    cy.log('Another user creates a public discussion');
    cy.addDiscussionSameQuestion(true, 'I dont know what this means', 677);
    cy.openSolvedQuiz(0);
    cy.wait(1000);
    cy.removeAnswers();
  });
});
