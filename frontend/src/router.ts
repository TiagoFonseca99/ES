import Vue from 'vue';
import Router from 'vue-router';
import Store from '@/store';

import LoginView from '@/views/LoginView.vue';
import CourseSelectionView from '@/views/CourseSelectionView.vue';

import HomeView from '@/views/HomeView.vue';
import ManagementView from '@/views/teacher/ManagementView.vue';
import QuestionsView from '@/views/teacher/questions/QuestionsView.vue';
import TopicsView from '@/views/teacher/TopicsView.vue';
import TournamentsView from '@/views/teacher/tournaments/TournamentsView.vue';
import SelectedTournamentView from '@/views/teacher/tournaments/SelectedTournamentView.vue';
import QuizzesView from '@/views/teacher/quizzes/QuizzesView.vue';
import StudentsView from '@/views/teacher/students/StudentsView.vue';
import CourseDashboardView from '@/views/teacher/CourseDashboardView.vue';
import StudentView from '@/views/student/StudentView.vue';
import ParticipantsTournament from '@/views/student/tournament/ParticipantsTournament.vue';

import MyTournamentsView from '@/views/student/tournament/MyTournamentsView.vue';
import AllTournamentView from './views/student/tournament/AllTournamentView.vue';
import OpenTournamentView from './views/student/tournament/OpenTournamentView.vue';
import ClosedTournamentView from './views/student/tournament/ClosedTournamentView.vue';
import AvailableQuizzesView from '@/views/student/AvailableQuizzesView.vue';
import SolvedQuizzesView from '@/views/student/SolvedQuizzesView.vue';
import QuizView from '@/views/student/quiz/QuizView.vue';
import ResultsView from '@/views/student/quiz/ResultsView.vue';
import ScanView from '@/views/student/ScanView.vue';
import DiscussionView from '@/views/student/discussion/DiscussionView.vue';
import DashboardView from '@/views/student/dashboard/DashboardView.vue';
import StudentDashboardView from '@/views/teacher/students/DashboardView.vue';
import SearchStudentView from '@/views/student/dashboard/SearchStudentView.vue';
import AnnouncementView from '@/views/teacher/announcements/AnnouncementView.vue';
import NotificationsView from '@/views/NotificationsView.vue';

import AdminManagementView from '@/views/admin/AdminManagementView.vue';
import NotFoundView from '@/views/NotFoundView.vue';
import ImpExpView from '@/views/teacher/impexp/ImpExpView.vue';
import AssessmentsView from '@/views/teacher/assessments/AssessmentsView.vue';
import CreateQuizzesView from '@/views/student/CreateQuizzesView.vue';
import SubmissionView from './views/student/questions/SubmissionView.vue';
import AllSubmissionsView from './views/student/questions/AllSubmissionsView.vue';
import ReviewsView from './views/teacher/reviews/ReviewsView.vue';
import StudentReviews from './views/student/questions/StudentReviewsView.vue';
import CoursesView from '@/views/admin/Courses/CoursesView.vue';
import { Student } from '@/models/management/Student';
import * as session from '@/session';

Vue.use(Router);

let router = new Router({
  mode: 'history',
  base: process.env.BASE_URL,
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
      meta: { title: process.env.VUE_APP_NAME, requiredAuth: 'None' }
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: {
        title: process.env.VUE_APP_NAME + ' - Login',
        requiredAuth: 'None'
      }
    },
    {
      path: '/courses',
      name: 'courses',
      component: CourseSelectionView,
      meta: {
        title: process.env.VUE_APP_NAME + ' - Course Selection',
        requiredAuth: 'Login'
      }
    },
    {
      path: '/course/dashboard',
      name: 'course-dashboard',
      component: CourseDashboardView,
      meta: {
        title: process.env.VUE_APP_NAME + ' - Course Dashboard',
        requiredAuth: 'Teacher'
      }
    },
    {
      path: '/management',
      name: 'management',
      component: ManagementView,
      children: [
        {
          path: 'questions',
          name: 'questions-management',
          component: QuestionsView,
          meta: {
            title: process.env.VUE_APP_NAME + ' - Questions',
            requiredAuth: 'Teacher'
          }
        },
        {
          path: 'reviews',
          name: 'reviews-management',
          component: ReviewsView,
          meta: {
            title: process.env.VUE_APP_NAME + ' - Reviews',
            requiredAuth: 'Teacher'
          }
        },
        {
          path: 'announcements',
          name: 'announcements',
          component: AnnouncementView,
          meta: {
            title: process.env.VUE_APP_NAME + ' - Announcements',
            requiredAuth: 'Teacher'
          }
        },
        {
          path: 'topics',
          name: 'topics-management',
          component: TopicsView,
          meta: {
            title: process.env.VUE_APP_NAME + ' - Topics',
            requiredAuth: 'Teacher'
          }
        },
        {
          path: 'tournaments',
          name: 'tournaments-management',
          component: TournamentsView,
          meta: {
            title: process.env.VUE_APP_NAME + ' - Tournaments',
            requiredAuth: 'Teacher'
          }
        },
        {
          path: 'quizzes',
          name: 'quizzes-management',
          component: QuizzesView,
          meta: {
            title: process.env.VUE_APP_NAME + ' - Quizzes',
            requiredAuth: 'Teacher'
          }
        },
        {
          path: 'assessments',
          name: 'assessments-management',
          component: AssessmentsView,
          meta: {
            title: process.env.VUE_APP_NAME + ' - Assessment Topics',
            requiredAuth: 'Teacher'
          }
        },
        {
          path: 'students',
          name: 'students-management',
          component: StudentsView,
          meta: {
            title: process.env.VUE_APP_NAME + ' - Students',
            requiredAuth: 'Teacher'
          }
        },
        {
          path: 'user',
          name: 'student-dashboard',
          component: StudentDashboardView,
          props: route => ({ username: route.query.username }),
          meta: {
            title: process.env.VUE_APP_NAME + ' - User Dashboard',
            requiredAuth: 'Teacher'
          }
        },
        {
          path: 'impexp',
          name: 'impexp-management',
          component: ImpExpView,
          meta: {
            title: process.env.VUE_APP_NAME + ' - ImpExp',
            requiredAuth: 'Teacher'
          }
        },
        {
          path: 'notifications',
          name: 'notifications-teacher',
          component: NotificationsView,
          props: route => ({ username: route.query.username }),
          meta: {
            title: process.env.VUE_APP_NAME + ' - Notifications',
            requiredAuth: 'Teacher'
          }
        }
      ]
    },
    {
      path: '/student',
      name: 'student',
      component: StudentView,
      children: [
        {
          path: 'available',
          name: 'available-quizzes',
          component: AvailableQuizzesView,
          meta: {
            title: process.env.VUE_APP_NAME + ' - Available Quizzes',
            requiredAuth: 'Student'
          }
        },
        {
          path: 'create',
          name: 'create-quizzes',
          component: CreateQuizzesView,
          meta: {
            title: process.env.VUE_APP_NAME + ' - Create Quizzes',
            requiredAuth: 'Student'
          }
        },
        {
          path: 'solved',
          name: 'solved-quizzes',
          component: SolvedQuizzesView,
          meta: {
            title: process.env.VUE_APP_NAME + ' - Solved Quizzes',
            requiredAuth: 'Student'
          }
        },
        {
          path: 'quiz',
          name: 'solve-quiz',
          component: QuizView,
          meta: {
            title: process.env.VUE_APP_NAME + ' - Quiz',
            requiredAuth: 'Student'
          }
        },
        {
          path: 'results',
          name: 'quiz-results',
          component: ResultsView,
          meta: {
            title: process.env.VUE_APP_NAME + ' - Results',
            requiredAuth: 'Student'
          }
        },
        {
          path: 'scan',
          name: 'scan',
          component: ScanView,
          meta: {
            title: process.env.VUE_APP_NAME + ' - Scan',
            requiredAuth: 'Student'
          }
        },
        {
          path: 'myTournaments',
          name: 'my-tournaments',
          component: MyTournamentsView,
          meta: {
            title: process.env.VUE_APP_NAME + ' - Tournament',
            requiredAuth: 'Student'
          }
        },
        {
          path: 'all',
          name: 'all-tournament',
          component: AllTournamentView,
          meta: {
            title: process.env.VUE_APP_NAME + ' - Tournament',
            requiredAuth: 'Student'
          }
        },
        {
          path: 'open',
          name: 'open-tournament',
          component: OpenTournamentView,
          meta: {
            title: process.env.VUE_APP_NAME + ' - Tournament',
            requiredAuth: 'Student'
          }
        },
        {
          path: 'closed',
          name: 'closed-tournament',
          component: ClosedTournamentView,
          meta: {
            title: process.env.VUE_APP_NAME + ' - Tournament',
            requiredAuth: 'Student'
          }
        },
        {
          path: 'submissions',
          name: 'submissions',
          component: SubmissionView,
          meta: {
            title: process.env.VUE_APP_NAME + ' - Create Submissions',
            requiredAuth: 'Student'
          }
        },
        {
          path: 'all-submissions',
          name: 'all-submissions',
          component: AllSubmissionsView,
          meta: {
            title: process.env.VUE_APP_NAME + ' - All Submissions',
            requiredAuth: 'Student'
          }
        },
        {
          path: 'reviews',
          name: 'reviews',
          component: StudentReviews,
          meta: {
            title: process.env.VUE_APP_NAME + ' - Submission Reviews',
            requiredAuth: 'Student'
          }
        },
        {
          path: 'discussions',
          name: 'discussions',
          component: DiscussionView,
          meta: {
            title: process.env.VUE_APP_NAME + ' - Discussions',
            requiredAuth: 'Student'
          }
        },
        {
          path: 'user',
          name: 'dashboard',
          component: DashboardView,
          props: route => ({ username: route.query.username }),
          meta: {
            title: process.env.VUE_APP_NAME + ' - User Dashboard',
            requiredAuth: 'Student'
          }
        },
        {
          path: 'search',
          name: 'search',
          component: SearchStudentView,
          meta: {
            title: process.env.VUE_APP_NAME + ' - Search Student',
            requiredAuth: 'Student'
          }
        },
        {
          path: 'notifications',
          name: 'notifications-student',
          component: NotificationsView,
          props: route => ({ username: route.query.username }),
          meta: {
            title: process.env.VUE_APP_NAME + ' - Notifications',
            requiredAuth: 'Student'
          }
        },
        {
          path: 'tournament',
          name: 'tournament participants',
          component: ParticipantsTournament,
          props: route => ({ id: route.query.id }),
          meta: {
            title: process.env.VUE_APP_NAME + ' - Tournament Participants',
            requiredAuth: 'Student'
          }
        }
      ]
    },
    {
      path: '/teacher/tournament',
      name: 'tournament dashboard',
      component: SelectedTournamentView,
      props: route => ({ id: route.query.id }),
      meta: {
        title: process.env.VUE_APP_NAME + ' - Tournament Dashboard',
        requiredAuth: 'Teacher'
      }
    },
    {
      path: '/admin',
      name: 'admin',
      component: AdminManagementView,
      children: [
        {
          path: 'courses',
          name: 'courseAdmin',
          component: CoursesView,
          meta: {
            title: process.env.VUE_APP_NAME + ' - Manage Courses',
            requiredAuth: 'Admin'
          }
        }
      ]
    },
    {
      path: '**',
      name: 'not-found',
      component: NotFoundView,
      meta: { title: 'Page Not Found', requiredAuth: 'None' }
    }
  ]
});

router.beforeEach(async (to, from, next) => {
  if (!Store.getters.isLoggedIn && session.checkLogged()) {
    let valid = await session.testToken();
    if (!valid) {
      next('/');
      return;
    }
  }

  if (to.meta.requiredAuth == 'None') {
    next();
  } else if (to.meta.requiredAuth == 'Admin' && Store.getters.isAdmin) {
    next();
  } else if (to.meta.requiredAuth == 'Teacher' && Store.getters.isTeacher) {
    next();
  } else if (to.meta.requiredAuth == 'Student' && Store.getters.isStudent) {
    next();
  } else if (to.meta.requiredAuth == 'Login' && Store.getters.isLoggedIn) {
    next();
  } else {
    next('/');
  }
});

router.afterEach(async (to, from) => {
  document.title = to.meta.title;
  await Store.dispatch('clearLoading');
});

export default router;
