describe('Teacher walkthrough', () => {

   beforeEach(() => {
        cy.demoTeacherLogin()
        cy.addSubmissionInfo(676)
        cy.addSubmissionInfo(678)
        cy.addDiscussionsInfo(676)
        cy.addDiscussionsInfo(678)
    })
    afterEach(() => {
        cy.removeDiscussionInfo(676)
        cy.removeDiscussionInfo(678)
        cy.removeSubmissionInfo()
        cy.removeSubmissionInfo()
        cy.contains('Logout').click()
    })

    it('login checks dashboard info', () => {
        cy.openCourseDashboard();
        cy.checkCourseInfo('Demo Course', 'DemoCourse', '1st Semester', 'TECNICO', 'ACTIVE');

        //check tournaments info
        cy.checkSubmissionsInfo(6,2,4);
        cy.checkDiscussionsInfo(4,2);
    });
});



