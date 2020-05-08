describe('Student discussion walkthrough', () => {
  beforeEach(() => {
    cy.demoStudentLogin()
    cy.removeAllReplies()
    cy.removeAllDiscussions()
  })

  afterEach(() => {
    cy.contains('Logout').click()
  })

  it('login answer quiz create discussion', () => {
    cy.answerQuiz(0)
    cy.writeDiscussion('I don\' know what this question means')
    cy.log('Teacher replies to discussion')
    cy.replyTeacherDiscussion('I think the question is pretty straightforward')
    cy.viewMyDiscussions()
    cy.openDiscussion(0)
    cy.replyDiscussion('I still don\'t understand')
  })

  it('login answer quiz create empty discussion', () => {
    cy.answerQuiz(0)
    cy.writeDiscussion(' ')
    cy.closeErrorMessage()
  })

  it('login, answer quiz create discussion view discussions return to quiz to view solved', () =>  {
    cy.answerQuiz(0)
    cy.writeDiscussion('I don\'t know what this question means')
    cy.viewMyDiscussions()
    cy.log("Another user creates a public discussion")
    cy.addDiscussionSameQuestion(true, 'I dont know what this means', 677)
    cy.openSolvedQuiz(0)
    cy.wait(1000);
  })
})