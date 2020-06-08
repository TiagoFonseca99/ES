describe('Teacher walkthrough', () => {
  beforeEach(() => {
    cy.demoTeacherLogin();
    cy.addDiscussionsInfo(676);
    cy.openTeacherQuestions();
  });

  afterEach(() => {
    cy.contains('Logout').click();
    cy.removeAllReplies();
    cy.removeDiscussionInfo(676);
  });

  it('login submits a reply and deletes', () => {
    cy.filterDiscussions();
    cy.filterDiscussions();
    cy.selectFirstDiscussion();
    cy.wait(1000);
    cy.submitReply('Reply Message');
    cy.openReplies();
    cy.wait(1000);
    cy.removeReply();
  });

  it('login submits an empty reply, good reply and edit', () => {
    cy.filterDiscussions();
    cy.filterDiscussions();
    cy.selectFirstDiscussion();
    cy.log('missing reply message');
    cy.submitEmptyReply();
    cy.closeErrorMessage();
    cy.submitReply('Reply Message');
    cy.openReplies();
    cy.wait(1000);
    cy.editReply('Not relevant');
    cy.wait(1000);
  });

  it('login changes discussion availability', () => {
    cy.filterDiscussions();
    cy.selectFirstDiscussion();
    cy.clickAvailability();
    cy.wait(1000);
  });
});
