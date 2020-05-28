import store from '@/store';
import * as storage from '@/storage';
import AuthDto from '@/models/user/AuthDto';
import RemoteServices from '@/services/RemoteServices';
import Course from '@/models/user/Course';

export const LOGIN_TOKEN = 'token';
export const SESSION_TOKEN = 'session';
export const COURSE_TOKEN = 'course';

export function checkLogged(name: string) {
  let session = storage.getLocal(SESSION_TOKEN);

  let token: string | null;

  // Check if session is set
  if (session == 'false' || session == 'true') {
    token = storage.get(name, session == 'true');
  } else {
    storage.persist(SESSION_TOKEN, 'true');
    token = storage.getSession(LOGIN_TOKEN);
  }

  if (token != null && token.trim().length != 0) {
    store.commit('token', token);
    return true;
  } else {
    store.commit('token', '');
    return false;
  }
}

export async function testToken() {
  let authResponse: AuthDto | null;

  try {
    authResponse = await RemoteServices.checkToken();
  } catch (Error) {
    authResponse = null;
  }

  // Variable has a good value
  // checkLogged ensures this
  let session = storage.getLocal(SESSION_TOKEN);

  if (authResponse != null) {
    store.commit('session', session == 'true');
    store.commit('login', authResponse);

    // Check if more than 1 course
    if (
      store.getters.getCurrentCourse == null &&
      authResponse.user.coursesNumber != 1
    ) {
      let course = storage.get(COURSE_TOKEN, session == 'true');

      try {
        if (course && JSON.parse(course)) {
          let parsed = new Course(JSON.parse(course));

          let array = authResponse.user.courses[parsed.name!];
          if (array) {
            for (let i = 0; i < array.length; i++) {
              if (array[i].courseExecutionId == parsed.courseExecutionId) {
                store.commit('currentCourse', parsed);

                return true;
              }
            }
          }
        }
      } catch (Error) {}

      storage.remove(COURSE_TOKEN, session == 'true');
      storage.removeAll(LOGIN_TOKEN);
      store.commit('logout');

      return false;
    } else {
      store.commit(
        'currentCourse',
        (Object.values(authResponse.user.courses)[0] as Course[])[0]
      );
    }
  } else {
    storage.removeAll(LOGIN_TOKEN);
    storage.removeAll(COURSE_TOKEN);
    store.commit('token', '');
  }

  return true;
}
