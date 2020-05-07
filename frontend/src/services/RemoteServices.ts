import axios from 'axios';
import Store from '@/store';
import Question from '@/models/management/Question';
import { Quiz } from '@/models/management/Quiz';
import Course from '@/models/user/Course';
import StatementCorrectAnswer from '@/models/statement/StatementCorrectAnswer';
import StudentStats from '@/models/statement/StudentStats';
import StatementQuiz from '@/models/statement/StatementQuiz';
import SolvedQuiz from '@/models/statement/SolvedQuiz';
import Topic from '@/models/management/Topic';
import { Student } from '@/models/management/Student';
import Assessment from '@/models/management/Assessment';
import AuthDto from '@/models/user/AuthDto';
import StatementAnswer from '@/models/statement/StatementAnswer';
import { QuizAnswers } from '@/models/management/QuizAnswers';
import Tournament from '@/models/user/Tournament';
import Submission from '@/models/management/Submission';
import Review from '@/models/management/Review';
import Discussion from '@/models/management/Discussion';
import Reply from '@/models/management/Reply';
import Dashboard from '@/models/management/Dashboard';

const httpClient = axios.create();
httpClient.defaults.timeout = 10000;
httpClient.defaults.baseURL = process.env.VUE_APP_ROOT_API;
httpClient.defaults.headers.post['Content-Type'] = 'application/json';
httpClient.interceptors.request.use(
  config => {
    if (!config.headers.Authorization) {
      const token = Store.getters.getToken;

      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
    }

    return config;
  },
  error => Promise.reject(error)
);

export default class RemoteServices {
  static async fenixLogin(code: string): Promise<AuthDto> {
    return httpClient
      .get(`/auth/fenix?code=${code}`)
      .then(response => {
        return new AuthDto(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async demoStudentLogin(): Promise<AuthDto> {
    return httpClient
      .get('/auth/demo/student')
      .then(response => {
        return new AuthDto(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async demoTeacherLogin(): Promise<AuthDto> {
    return httpClient
      .get('/auth/demo/teacher')
      .then(response => {
        return new AuthDto(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async demoAdminLogin(): Promise<AuthDto> {
    return httpClient
      .get('/auth/demo/admin')
      .then(response => {
        return new AuthDto(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async getUserStats(): Promise<StudentStats> {
    return httpClient
      .get(
        `/executions/${Store.getters.getCurrentCourse.courseExecutionId}/stats`
      )
      .then(response => {
        return new StudentStats(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async getQuestions(): Promise<Question[]> {
    return httpClient
      .get(`/courses/${Store.getters.getCurrentCourse.courseId}/questions`)
      .then(response => {
        return response.data.map((question: any) => {
          return new Question(question);
        });
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async exportCourseQuestions(): Promise<Blob> {
    return httpClient
      .get(
        `/courses/${Store.getters.getCurrentCourse.courseId}/questions/export`,
        {
          responseType: 'blob'
        }
      )
      .then(response => {
        return new Blob([response.data], {
          type: 'application/zip, application/octet-stream'
        });
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async getAvailableQuestions(): Promise<Question[]> {
    return httpClient
      .get(
        `/courses/${Store.getters.getCurrentCourse.courseId}/questions/available`
      )
      .then(response => {
        return response.data.map((question: any) => {
          return new Question(question);
        });
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async createQuestion(question: Question): Promise<Question> {
    return httpClient
      .post(
        `/courses/${Store.getters.getCurrentCourse.courseId}/questions/`,
        question
      )
      .then(response => {
        return new Question(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async updateQuestion(question: Question): Promise<Question> {
    return httpClient
      .put(`/questions/${question.id}`, question)
      .then(response => {
        return new Question(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async deleteQuestion(questionId: number) {
    return httpClient.delete(`/questions/${questionId}`).catch(async error => {
      throw Error(await this.errorMessage(error));
    });
  }

  static async setQuestionStatus(
    questionId: number,
    status: String
  ): Promise<Question> {
    return httpClient
      .post(`/questions/${questionId}/set-status`, status, {})
      .then(response => {
        return new Question(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async uploadImage(file: File, questionId: number): Promise<string> {
    let formData = new FormData();
    formData.append('file', file);
    return httpClient
      .put(`/questions/${questionId}/image`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })
      .then(response => {
        return response.data as string;
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async updateQuestionTopics(questionId: number, topics: Topic[]) {
    return httpClient.put(`/questions/${questionId}/topics`, topics);
  }

  static async getTopics(): Promise<Topic[]> {
    return httpClient
      .get(`/courses/${Store.getters.getCurrentCourse.courseId}/topics`)
      .then(response => {
        return response.data.map((topic: any) => {
          return new Topic(topic);
        });
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async getAvailableQuizzes(): Promise<StatementQuiz[]> {
    return httpClient
      .get(
        `/executions/${Store.getters.getCurrentCourse.courseExecutionId}/quizzes/available`
      )
      .then(response => {
        return response.data.map((statementQuiz: any) => {
          return new StatementQuiz(statementQuiz);
        });
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async generateStatementQuiz(params: object): Promise<StatementQuiz> {
    return httpClient
      .post(
        `/executions/${Store.getters.getCurrentCourse.courseExecutionId}/quizzes/generate`,
        params
      )
      .then(response => {
        return new StatementQuiz(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async getSolvedQuizzes(): Promise<SolvedQuiz[]> {
    return httpClient
      .get(
        `/executions/${Store.getters.getCurrentCourse.courseExecutionId}/quizzes/solved`
      )
      .then(response => {
        return response.data.map((solvedQuiz: any) => {
          return new SolvedQuiz(solvedQuiz);
        });
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async getQuizByQRCode(quizId: number): Promise<StatementQuiz> {
    return httpClient
      .get(`/quizzes/${quizId}/byqrcode`)
      .then(response => {
        return new StatementQuiz(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async exportQuiz(quizId: number): Promise<Blob> {
    return httpClient
      .get(`/quizzes/${quizId}/export`, {
        responseType: 'blob'
      })
      .then(response => {
        return new Blob([response.data], {
          type: 'application/zip, application/octet-stream'
        });
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async startQuiz(quizId: number) {
    return httpClient.get(`/quizzes/${quizId}/start`).catch(async error => {
      throw Error(await this.errorMessage(error));
    });
  }

  static async submitAnswer(quizId: number, answer: StatementAnswer) {
    return httpClient
      .post(`/quizzes/${quizId}/submit`, answer)
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async concludeQuiz(
    quizId: number
  ): Promise<StatementCorrectAnswer[] | void> {
    return httpClient
      .get(`/quizzes/${quizId}/conclude`)
      .then(response => {
        if (response.data) {
          return response.data.map((answer: any) => {
            return new StatementCorrectAnswer(answer);
          });
        }
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async createTopic(topic: Topic): Promise<Topic> {
    return httpClient
      .post(
        `/courses/${Store.getters.getCurrentCourse.courseId}/topics/`,
        topic
      )
      .then(response => {
        return new Topic(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async updateTopic(topic: Topic): Promise<Topic> {
    return httpClient
      .put(`/topics/${topic.id}`, topic)
      .then(response => {
        return new Topic(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async deleteTopic(topic: Topic) {
    return httpClient.delete(`/topics/${topic.id}`).catch(async error => {
      throw Error(await this.errorMessage(error));
    });
  }

  static async getNonGeneratedQuizzes(): Promise<Quiz[]> {
    return httpClient
      .get(
        `/executions/${Store.getters.getCurrentCourse.courseExecutionId}/quizzes/non-generated`
      )
      .then(response => {
        return response.data.map((quiz: any) => {
          return new Quiz(quiz);
        });
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async deleteQuiz(quizId: number) {
    return httpClient.delete(`/quizzes/${quizId}`).catch(async error => {
      throw Error(await this.errorMessage(error));
    });
  }

  static async getQuiz(quizId: number): Promise<Quiz> {
    return httpClient
      .get(`/quizzes/${quizId}`)
      .then(response => {
        return new Quiz(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async getQuizAnswers(quizId: number): Promise<QuizAnswers> {
    return httpClient
      .get(`/quizzes/${quizId}/answers`)
      .then(response => {
        return new QuizAnswers(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async saveQuiz(quiz: Quiz): Promise<Quiz> {
    if (quiz.id) {
      return httpClient
        .put(`/quizzes/${quiz.id}`, quiz)
        .then(response => {
          return new Quiz(response.data);
        })
        .catch(async error => {
          throw Error(await this.errorMessage(error));
        });
    } else {
      return httpClient
        .post(
          `/executions/${Store.getters.getCurrentCourse.courseExecutionId}/quizzes`,
          quiz
        )
        .then(response => {
          return new Quiz(response.data);
        })
        .catch(async error => {
          throw Error(await this.errorMessage(error));
        });
    }
  }

  static async getCourseStudents(course: Course) {
    return httpClient
      .get(`/executions/${course.courseExecutionId}/students`)
      .then(response => {
        return response.data.map((student: any) => {
          return new Student(student);
        });
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async getCourseDashboardInfo(executionId: number): Promise<Dashboard> {
    return httpClient
      .get(`/dashboard/${executionId}`)
      .then(response => {
        return new Dashboard(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async getAssessments(): Promise<Assessment[]> {
    return httpClient
      .get(
        `/executions/${Store.getters.getCurrentCourse.courseExecutionId}/assessments`
      )
      .then(response => {
        return response.data.map((assessment: any) => {
          return new Assessment(assessment);
        });
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async getAvailableAssessments() {
    return httpClient
      .get(
        `/executions/${Store.getters.getCurrentCourse.courseExecutionId}/assessments/available`
      )
      .then(response => {
        return response.data.map((assessment: any) => {
          return new Assessment(assessment);
        });
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async saveAssessment(assessment: Assessment) {
    if (assessment.id) {
      return httpClient
        .put(`/assessments/${assessment.id}`, assessment)
        .then(response => {
          return new Assessment(response.data);
        })
        .catch(async error => {
          throw Error(await this.errorMessage(error));
        });
    } else {
      return httpClient
        .post(
          `/executions/${Store.getters.getCurrentCourse.courseExecutionId}/assessments`,
          assessment
        )
        .then(response => {
          return new Assessment(response.data);
        })
        .catch(async error => {
          throw Error(await this.errorMessage(error));
        });
    }
  }

  static async deleteAssessment(assessmentId: number) {
    return httpClient
      .delete(`/assessments/${assessmentId}`)
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async setAssessmentStatus(
    assessmentId: number,
    status: string
  ): Promise<Assessment> {
    return httpClient
      .post(`/assessments/${assessmentId}/set-status`, status, {
        headers: {
          'Content-Type': 'text/html'
        }
      })
      .then(response => {
        return new Assessment(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static getCourses(): Promise<Course[]> {
    return httpClient
      .get('/courses/executions')
      .then(response => {
        return response.data.map((course: any) => {
          return new Course(course);
        });
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async activateCourse(course: Course): Promise<Course> {
    return httpClient
      .post('/courses/activate', course)
      .then(response => {
        return new Course(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async createExternalCourse(course: Course): Promise<Course> {
    return httpClient
      .post('/courses/external', course)
      .then(response => {
        return new Course(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async submitQuestion(submission: Submission): Promise<Submission> {
    return httpClient
      .post('/student/submissions', submission)
      .then(response => {
        return new Submission(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async changeSubmission(submission: Submission) {
    return httpClient
      .put('/management/reviews/changeSubmission', submission)
      .then(() => {})
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async resubmitQuestion(
    submission: Submission,
    questionId: number
  ): Promise<Submission> {
    return httpClient
      .put(`/student/reviews/${questionId}`, submission)
      .then(response => {
        return new Submission(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async getSubmissions(): Promise<Submission[]> {
    return httpClient
      .get('/student/submissions')
      .then(response => {
        return response.data.map((submission: any) => {
          return new Submission(submission);
        });
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async deleteSubmission(questionId: number) {
    return httpClient
      .delete(`/student/submissions/${questionId}`)
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async createReview(review: Review): Promise<Review> {
    return httpClient
      .post('/management/reviews', review)
      .then(response => {
        return new Review(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async getSubsToTeacher(): Promise<Submission[]> {
    return httpClient
      .get('/management/reviews')
      .then(response => {
        return response.data.map((submission: any) => {
          return new Submission(submission);
        });
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async getReviewsToTeacher(): Promise<Review[]> {
    return httpClient
      .get('/management/reviews/showReviews')
      .then(response => {
        return response.data.map((review: any) => {
          return new Review(review);
        });
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async getSubmissionReviews(): Promise<Review[]> {
    return httpClient
      .get('/student/reviews')
      .then(response => {
        return response.data.map((review: any) => {
          return new Review(review);
        });
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async deleteCourse(courseExecutionId: number | undefined) {
    return httpClient
      .delete(`/executions/${courseExecutionId}`)
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async createDiscussion(discussion: Discussion): Promise<Discussion> {
    return httpClient
      .post('/discussions', discussion)
      .then(response => {
        return new Discussion(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async getDiscussions(userId: number): Promise<Discussion[]> {
    return httpClient
      .get('/discussions?userId=' + userId)
      .then(response => {
        return response.data.map((discussion: any) => {
          return new Discussion(discussion);
        });
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async getDiscussionsByQuestion(
    questionId: number
  ): Promise<Discussion[]> {
    return httpClient
      .get('/discussions/question?questionId=' + questionId)
      .then(response => {
        return response.data.map((discussion: any) => {
          return new Discussion(discussion);
        });
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async setAvailability(
    discussion: Discussion,
    available: boolean
  ): Promise<Discussion> {
    return httpClient
      .put('/discussions?available=' + available, discussion)
      .then(response => {
        return new Discussion(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async createReply(
    message: string,
    discussion: Discussion
  ): Promise<Reply> {
    return httpClient
      .post('/discussions/replies?message=' + message, discussion)
      .then(response => {
        return new Reply(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async toggleDiscussionStats(): Promise<Dashboard> {
    return httpClient
      .put('/dashboard/discussions')
      .then(response => {
        return new Dashboard(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async toggleSubmissionStats(): Promise<Dashboard> {
    return httpClient
        .put('/dashboard/submissions')
        .then(response => {
          return new Dashboard(response.data);
        })
        .catch(async error => {
          throw Error(await this.errorMessage(error));
        });
  }

  static async toggleTournamentStats(): Promise<Dashboard> {
    return httpClient
      .put('/dashboard/tournaments')
      .then(response => {
        return new Dashboard(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async exportAll() {
    return httpClient
      .get('/admin/export', {
        responseType: 'blob'
      })
      .then(response => {
        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement('a');
        link.href = url;
        let dateTime = new Date();
        link.setAttribute(
          'download',
          `export-${dateTime.toLocaleString()}.zip`
        );
        document.body.appendChild(link);
        link.click();
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async getDashboardInfo(): Promise<Dashboard> {
    return httpClient
      .get('/dashboard')
      .then(response => {
        return new Dashboard(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async errorMessage(error: any): Promise<string> {
    if (error.message === 'Network Error') {
      return 'Unable to connect to server';
    } else if (error.message.split(' ')[0] === 'timeout') {
      return 'Request timeout - Server took too long to respond';
    } else if (error.response) {
      return error.response.data.message;
    } else if (error.message === 'Request failed with status code 403') {
      await Store.dispatch('logout');
      return 'Unauthorized access or Expired token';
    } else {
      console.log(error);
      return 'Unknown Error - Contact admin';
    }
  }

  static async createTournament(
    topicsID: Number[],
    tournament: Tournament
  ): Promise<Tournament> {
    let path: string = '/tournaments?';
    for (let topicID of topicsID) {
      path += 'topicsId=' + topicID + '&';
    }
    path = path.substring(0, path.length - 1);
    return httpClient
      .post(path, tournament)
      .then(response => {
        return new Tournament(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async editTournament(
    startTime: string,
    endTime: string,
    numberOfQuestions: number,
    topicsToAdd: Number[],
    topicsToRemove: Number[],
    tournament: Tournament
  ): Promise<Tournament> {
    let result = tournament;
    if (result.endTime && endTime != result.endTime) {
      tournament = await this.editEndTime(result);
    }
    if (result.startTime && startTime != result.startTime) {
      tournament = await this.editStartTime(result);
    }
    if (
      result.numberOfQuestions &&
      numberOfQuestions != result.numberOfQuestions
    ) {
      tournament = await this.editNumberOfQuestions(result);
    }
    if (topicsToAdd.length > 0) {
      tournament = await this.addTopics(topicsToAdd, result);
    }
    if (topicsToRemove.length > 0) {
      tournament = await this.removeTopics(topicsToRemove, result);
    }
    return new Tournament(tournament);
  }

  static async editStartTime(tournament: Tournament): Promise<Tournament> {
    return httpClient
      .put('/tournaments/editStartTime', tournament)
      .then(response => {
        return new Tournament(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async editEndTime(tournament: Tournament): Promise<Tournament> {
    return httpClient
      .put('/tournaments/editEndTime', tournament)
      .then(response => {
        return new Tournament(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async editNumberOfQuestions(
    tournament: Tournament
  ): Promise<Tournament> {
    let numberOfQuestions: string = '';
    if (tournament.numberOfQuestions) {
      numberOfQuestions = tournament.numberOfQuestions.toString();
    }
    let path: string =
      '/tournaments/editNumberOfQuestions?numberOfQuestions=' +
      numberOfQuestions;
    return httpClient
      .put(path, tournament)
      .then(response => {
        return new Tournament(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async addTopics(
    topicsID: Number[],
    tournament: Tournament
  ): Promise<Tournament> {
    let path: string = '/tournaments/addTopics?';
    for (let topicID of topicsID) {
      path += 'topicsId=' + topicID + '&';
    }
    path = path.substring(0, path.length - 1);
    return httpClient
      .put(path, tournament)
      .then(response => {
        return new Tournament(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async removeTopics(
    topicsID: Number[],
    tournament: Tournament
  ): Promise<Tournament> {
    let path: string = '/tournaments/removeTopics?';
    for (let topicID of topicsID) {
      path += 'topicsId=' + topicID + '&';
    }
    path = path.substring(0, path.length - 1);
    return httpClient
      .put(path, tournament)
      .then(response => {
        return new Tournament(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static getTournaments(): Promise<Tournament[]> {
    return httpClient
      .get('/tournaments/getTournaments')
      .then(response => {
        return response.data.map((tournament: any) => {
          return new Tournament(tournament, Store.getters.getUser);
        });
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static getOpenTournaments(): Promise<Tournament[]> {
    return httpClient
      .get('/tournaments/getOpenTournaments')
      .then(response => {
        return response.data.map((tournament: any) => {
          return new Tournament(tournament, Store.getters.getUser);
        });
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static getUserTournaments(): Promise<Tournament[]> {
    return httpClient
      .get('/tournaments/getUserTournaments')
      .then(response => {
        return response.data.map((tournament: any) => {
          return new Tournament(tournament, Store.getters.getUser);
        });
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static solveTournament(tournament: Tournament) {
    return httpClient
      .put('tournaments/solveQuiz', tournament)
      .then(response => {
        return new StatementQuiz(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static joinTournament(tournament: Tournament) {
    return httpClient
      .put('tournaments/joinTournament', tournament)
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static switchTournamentNamePermission() {
    return httpClient
      .put('/switchTournamentNamePermission')
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static switchTournamentScorePermission() {
    return httpClient
      .put('/switchTournamentScorePermission')
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static leaveTournament(tournament: Tournament) {
    return httpClient
      .put('tournaments/leaveTournament', tournament)
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static cancelTournament(tournament: Tournament) {
    return httpClient
      .put('tournaments/cancelTournament', tournament)
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }
}
