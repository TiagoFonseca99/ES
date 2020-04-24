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


Cypress.Commands.add('createCourseExecution', (name, acronym, academicTerm) => {
    cy.get('[data-cy="createButton"]').click();
    cy.get('[data-cy="courseExecutionNameInput"]').type(name);
    cy.get('[data-cy="courseExecutionAcronymInput"]').type(acronym);
    cy.get('[data-cy="courseExecutionAcademicTermInput"]').type(academicTerm);
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
    cy.get('[data-cy="courseExecutionAcronymInput"]').type(acronym);
    cy.get('[data-cy="courseExecutionAcademicTermInput"]').type(academicTerm);
    cy.get('[data-cy="saveButton"]').click();
  }
);

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
    cy.get('tbody > :nth-child(' + n + 1 + ') > .text-start').click()
})

Cypress.Commands.add('submitReply', (message) => {
    cy.get('[data-cy="filterDiscussions"]').click();
    cy.get('[data-cy="filterDiscussions"]').click();
    cy.get('[data-cy="showQuestionDialog"]').first().click();
    cy.get('[data-cy="ReplyMessage"]').type(message);
    cy.get('[data-cy="submitReply"]').click();
});

Cypress.Commands.add('submitEmptyReply', () => {
    cy.get('[data-cy="filterDiscussions"]').click();
    cy.get('[data-cy="filterDiscussions"]').click();
    cy.get('[data-cy="showQuestionDialog"]').first().click();
    cy.get('[data-cy="submitReply"]').click();
});

Cypress.Commands.add(
  'openSubmissions',
  () => {
      cy.contains('Questions').click();
      cy.contains('Submissions').click();
  }
);

Cypress.Commands.add('openTeacherQuestions', () => {
    cy.get('[data-cy="Management"]').click()
    cy.get('[data-cy="Questions"]').click()
})

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
    cy.exec('PGPASSWORD= psql -d tutordb -U daniel -h localhost -c "with sub as (select s.id from submissions s join questions q on s.question_id=q.id where q.title=\'Test Question\') insert into reviews(current_date,justification,status,student_id,submission_id,user_id) values (current_timestamp,\'Excelente pergunta\', \'APPROVED\', 676, (select * from sub), 677);" ')
});

Cypress.Commands.add('ApproveSubmissions', (title, justification) => {
    cy.get('[data-cy="Management"]').click();
    cy.get('[data-cy="Reviews"]').click();
    cy.get('[data-cy="Search"]').click();
    cy.contains(title)
      .parent()
      .should('have.length', 1)
      .children()
      .should('have.length', 6)
      .find('[data-cy="createReview"]')
      .click();
    cy.get('[data-cy="Justification"]').type(justification);
    cy.get('[data-cy="Approve"]').click();
});

Cypress.Commands.add('RejectSubmissions', (title, justification) => {
    cy.get('[data-cy="Management"]').click();
    cy.get('[data-cy="Reviews"]').click();
    cy.get('[data-cy="Search"]').click();
    cy.contains(title)
      .parent()
      .should('have.length', 1)
      .children()
      .should('have.length', 6)
      .find('[data-cy="createReview"]')
      .click();
    cy.get('[data-cy="Justification"]').type(justification);
    cy.get('[data-cy="Reject"]').click();
});

Cypress.Commands.add('TeacherDeleteSubmission', title => {
    cy.contains(title)
      .parent()
      .should('have.length', 1)
      .children()
      .should('have.length', 6)
      .find('[data-cy="deleteSubmission"]')
      .click();
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
