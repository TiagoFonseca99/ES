// ***********************************************
// This example commands.js shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
//
//
// -- This is a parent command --
// Cypress.Commands.add("login", (email, password) => { ... })
//
//
// -- This is a child command --
// Cypress.Commands.add("drag", { prevSubject: 'element'}, (subject, options) => { ... })
//
//
// -- This is a dual command --
// Cypress.Commands.add("dismiss", { prevSubject: 'optional'}, (subject, options) => { ... })
//
//
// -- This will overwrite an existing command --
// Cypress.Commands.overwrite("visit", (originalFn, url, options) => { ... })
/// <reference types="Cypress" />
Cypress.Commands.add('demoAdminLogin', () => {
    cy.visit('/')
    cy.get('[data-cy="adminButton"]').click()
    cy.contains('Administration').click()
    cy.contains('Manage Courses').click()
})

Cypress.Commands.add('demoStudentLogin1', () => {
    cy.visit('/')
    cy.get('[data-cy="studentButton"]').click()
    cy.contains('Questions').click()
    cy.contains('Submissions').click()
})

Cypress.Commands.add('demoStudentLogin', () => {
    cy.visit('/')
    cy.get('[data-cy="studentButton"]').click()
})

Cypress.Commands.add('submitQuestion', (title, content, opt1, opt2, opt3, opt4) => {
    cy.get('[data-cy="submitQuestion"]').click()
    cy.get('[data-cy="QuestionTitle"]').type(title)
    cy.get('[data-cy="QuestionContent"]').type(content)
    cy.get('[data-cy="Switch1"]').click( {force: true})
    cy.get('[data-cy="Option1"]').type(opt1)
    cy.get('[data-cy="Option2"]').type(opt2)
    cy.get('[data-cy="Option3"]').type(opt3)
    cy.get('[data-cy="Option4"]').type(opt4)
    cy.get('[data-cy="submitButton"]').click()
})

Cypress.Commands.add('submitInvalidQuestion', (title, content) => {

    cy.get('[data-cy="submitQuestion"]').click()
    cy.get('[data-cy="QuestionTitle"]').type(title)
    cy.get('[data-cy="QuestionContent"]').type(content)
    cy.get('[data-cy="submitButton"]').click()
})

Cypress.Commands.add('createCourseExecution', (name, acronym, academicTerm) => {
    cy.get('[data-cy="createButton"]').click()
    cy.get('[data-cy="Name"]').type(name)
    cy.get('[data-cy="Acronym"]').type(acronym)
    cy.get('[data-cy="AcademicTerm"]').type(academicTerm)
    cy.get('[data-cy="saveButton"]').click()
})

Cypress.Commands.add('answerQuiz', (n) => {
    cy.get('[data-cy="quizzes"]').click()
    cy.get('[data-cy="availableQuizzes"]').click()
    cy.get('[data-cy="quizNo' + n + '"]').click()
    cy.get('[data-cy="optionNo1"]').click()
    cy.get('[data-cy="nextQuestion"]').click()
    cy.get('[data-cy="optionNo0"]').click()
    cy.get('[data-cy="nextQuestion"]').click()
    cy.get('[data-cy="optionNo2"]').click()
    cy.get('[data-cy="nextQuestion"]').click()
    cy.get('[data-cy="optionNo3"]').click()
    cy.get('[data-cy="nextQuestion"]').click()
    cy.get('[data-cy="optionNo1"]').click()
    cy.get('[data-cy="endQuiz"]').click()
    cy.get('[data-cy="confirmEndQuiz"]').click()
})

Cypress.Commands.add('writeDiscussion', (content) => {
    cy.get('[data-cy="discussionText"]').type(content)
    cy.get('[data-cy="createDiscussion"]').click()
})

Cypress.Commands.add('viewMyDiscussions', () => {
    cy.get('[data-cy="discussions"]').click()
})

Cypress.Commands.add('replyDiscussion', (content) => {
  cy.exec(
    'PGPASSWORD= psql -d tutordb -U daniel -h localhost -c "WITH rep AS (INSERT INTO replies (discussion_user_id, teacher_id, message, date) VALUES (676, 677, \'' +
      content +
      '\', \'2020-01-01 00:00:01\') RETURNING id) UPDATE discussions SET reply_id = (SELECT id FROM rep) WHERE user_id = 676;"')
})

Cypress.Commands.add('openDiscussion', (n) => {
    cy.get('tbody > :nth-child(' + n + 1 + ') > .text-start')
      .click()
})

Cypress.Commands.add('closeErrorMessage', (name, acronym, academicTerm) => {
    cy.contains('Error')
        .parent()
        .find('button')
        .click()
})

Cypress.Commands.add('deleteCourseExecution', (acronym) => {
    cy.contains(acronym)
        .parent()
        .should('have.length', 1)
        .children()
        .should('have.length', 7)
        .find('[data-cy="deleteCourse"]')
        .click()
})

Cypress.Commands.add('createFromCourseExecution', (name, acronym, academicTerm) => {
    cy.contains(name)
        .parent()
        .should('have.length', 1)
        .children()
        .should('have.length', 7)
        .find('[data-cy="createFromCourse"]')
        .click()
    cy.get('[data-cy="Acronym"]').type(acronym)
    cy.get('[data-cy="AcademicTerm"]').type(academicTerm)
    cy.get('[data-cy="saveButton"]').click()
})

