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
    'openSubmissions',
    () => {
        cy.contains('Questions').click();
        cy.contains('Submissions').click();
    }
);

Cypress.Commands.add(
    'submitQuestion',
    (title, content, opt1, opt2, opt3, opt4) => {
        cy.openSubmissions();
        cy.get('[data-cy="submitQuestion"]').click();
        cy.get('[data-cy="QuestionTitle"]').type(title);
        cy.get('[data-cy="QuestionContent"]').type(content);
        cy.get('[data-cy="Switch1"]').click({ force: true });
        cy.get('[data-cy="Option1"]').type(opt1);
        cy.get('[data-cy="Option2"]').type(opt2);
        cy.get('[data-cy="Option3"]').type(opt3);
        cy.get('[data-cy="Option4"]').type(opt4);
        cy.get('[data-cy="submitButton"]').click();
        cy.contains(title)
            .parent()
            .should('have.length', 1)
            .children()
            .should('have.length', 6);
        cy.wait(500);
    }
);

Cypress.Commands.add(
    'viewQuestion',
    (title) => {
        cy.contains(title)
            .parent()
            .should('have.length', 1)
            .children()
            .should('have.length', 6)
            .find('[data-cy="viewQuestion"]')
            .click();
        cy.get('[data-cy="close"]').click();
    }
);

Cypress.Commands.add('submitInvalidQuestion', (title, content) => {
    cy.openSubmissions();
    cy.get('[data-cy="submitQuestion"]').click();
    cy.get('[data-cy="QuestionTitle"]').type(title);
    cy.get('[data-cy="QuestionContent"]').type(content);
    cy.get('[data-cy="submitButton"]').click();
});

Cypress.Commands.add('deleteSubmission', title => {
    cy.contains(title)
        .parent()
        .should('have.length', 1)
        .children()
        .should('have.length', 6)
        .find('[data-cy="deleteSubmission"]')
        .click();
});

Cypress.Commands.add('teacherReviewsSubmission', () => {
    cy.exec('PGPASSWORD= psql -d tutordb -U dserafim1999 -h localhost -c "with sub as (select s.id from submissions s join questions q on s.question_id=q.id where q.title=\'Test Question\') insert into reviews(current_date,justification,status,student_id,submission_id,user_id) values (current_timestamp,\'Excelente pergunta\', \'APPROVED\', 676, (select * from sub), 677);" ')
});

Cypress.Commands.add('createCourseExecution', (name, acronym, academicTerm) => {
    cy.get('[data-cy="createButton"]').click();
    cy.get('[data-cy="Name"]').type(name);
    cy.get('[data-cy="Acronym"]').type(acronym);
    cy.get('[data-cy="AcademicTerm"]').type(academicTerm);
    cy.get('[data-cy="saveButton"]').click();
});

Cypress.Commands.add('getSubmissionStatus', (title, status, justification) => {
    cy.contains('Questions').click();
    cy.contains('Reviews').click();
    cy.contains(status);
    cy.contains(title)
        .parent()
        .should('have.length', 1)
        .children()
        .should('have.length', 5)
        .find('[data-cy="view"]')
        .click();
    cy.get('[data-cy="close"]').click();
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