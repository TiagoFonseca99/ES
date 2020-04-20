describe('Student discussion walkthrough', () => {
  beforeEach(() => {
    cy.demoStudentLogin()
  })

  afterEach(() => {
    cy.contains('Logout').click()
  })

  it('login answer quiz create discussion', () => {
    cy.answerQuiz(0)
    cy.writeDiscussion('I don\' know what this question means')
    cy.log('Teacher replies to discussion')
    cy.replyDiscussion('I think the question is pretty straightforward')
    cy.viewMyDiscussions()
    cy.openDiscussion(0)
  })

  it('login answer quiz create empty discussion', () => {
    cy.answerQuiz(0)
    cy.writeDiscussion(' ')
    cy.closeErrorMessage()
  })
})