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
  cy.visit('/');
  cy.get('[data-cy="adminButton"]').click();
  cy.contains('Administration').click();
  cy.contains('Manage Courses').click();
});

Cypress.Commands.add('demoStudentLogin', () => {
  cy.visit('/');
  cy.get('[data-cy="studentButton"]').click();
});

Cypress.Commands.add(
  'submitQuestion',
  (title, content, opt1, opt2, opt3, opt4) => {
    cy.get('[data-cy="submitQuestion"]').click();
    cy.get('[data-cy="QuestionTitle"]').type(title);
    cy.get('[data-cy="QuestionContent"]').type(content);
    cy.get('[data-cy="Switch1"]').click({ force: true });
    cy.get('[data-cy="Option1"]').type(opt1);
    cy.get('[data-cy="Option2"]').type(opt2);
    cy.get('[data-cy="Option3"]').type(opt3);
    cy.get('[data-cy="Option4"]').type(opt4);
    cy.get('[data-cy="submitButton"]').click();
  }
);

Cypress.Commands.add('submitInvalidQuestion', (title, content) => {
  cy.get('[data-cy="submitQuestion"]').click();
  cy.get('[data-cy="QuestionTitle"]').type(title);
  cy.get('[data-cy="QuestionContent"]').type(content);
  cy.get('[data-cy="submitButton"]').click();
});

Cypress.Commands.add('createTournament', numberOfQuestions => {
  cy.get('[data-cy="createButton"]')
    .should('be.visible')
    .click({ force: true });
  cy.time();
  cy.get('[data-cy="NumberOfQuestions"]').type(numberOfQuestions, { force: true });
  cy.selectTopic('Adventure Builder');
  cy.selectTopic('Architectural Style');
  cy.get('[data-cy="saveButton"]').click();
});

Cypress.Commands.add('time', () => {
  cy.get('label')
    .contains('Start Time')
    .click({ force: true });
  cy.get('.v-date-picker-header')
    .should('have.length', 1)
    .children()
    .should('have.length', 3)
    .eq(2)
    .click()
    .wait(500);
  cy.get('.v-date-picker-table')
    .should('have.length', 1)
    .contains('16')
    .click()
    .get('.v-card__actions')
    .contains('OK')
    .click()
    .should('not.be.visible');

  cy.get('label')
    .contains('End Time')
    .click({ force: true });
  cy.get('.v-date-picker-header:visible')
    .should('have.length', 1)
    .children()
    .should('have.length', 3)
    .eq(2)
    .click()
    .wait(500);
  cy.get('.v-date-picker-table:visible')
    .should('have.length', 1)
    .contains('18')
    .click()
    .get('.v-card__actions:visible')
    .contains('OK')
    .click()
    .should('not.be.visible');
});

Cypress.Commands.add('selectTopic', topic => {
  cy.get('[data-cy="Topics"]')
    .should('be.visible')
    .should('have.length', 1)
    .children()
    .should('have.length', 4)
    .contains(topic)
    .parent()
    .should('have.length', 1)
    .children()
    .should('have.length', 2)
    .find('[data-cy="addTopic"]')
    .click();
});

Cypress.Commands.add('joinTournament', (tournament) => {
  cy.get('tbody')
    .children()
    .eq(tournament)
    .children()
    .should('have.length', 9)
    .eq(8)
    .find('[data-cy="JoinTournament"]')
    .click({ force: true });
});

Cypress.Commands.add('createCourseExecution', (name, acronym, academicTerm) => {
  cy.get('[data-cy="createButton"]').click();
  cy.get('[data-cy="Name"]').type(name);
  cy.get('[data-cy="Acronym"]').type(acronym);
  cy.get('[data-cy="AcademicTerm"]').type(academicTerm);
  cy.get('[data-cy="saveButton"]').click();
});

Cypress.Commands.add('closeErrorMessage', (name, acronym, academicTerm) => {
  cy.contains('Error')
    .parent()
    .find('button')
    .click();
});

Cypress.Commands.add('deleteCourseExecution', acronym => {
  cy.contains(acronym)
    .parent()
    .should('have.length', 1)
    .children()
    .should('have.length', 7)
    .find('[data-cy="deleteCourse"]')
    .click();
});

Cypress.Commands.add(
  'createFromCourseExecution',
  (name, acronym, academicTerm) => {
    cy.contains(name)
      .parent()
      .should('have.length', 1)
      .children()
      .should('have.length', 7)
      .find('[data-cy="createFromCourse"]')
      .click();
    cy.get('[data-cy="Acronym"]').type(acronym);
    cy.get('[data-cy="AcademicTerm"]').type(academicTerm);
    cy.get('[data-cy="saveButton"]').click();
  }
);
