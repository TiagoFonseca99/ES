import store from '@/store';
import * as storage from '@/storage';
import RemoteServices from '@/services/RemoteServices';
import Course from '@/models/user/Course';
import User from '@/models/user/User';

export const SESSION_TOKEN = 'session';
export const COURSE_TOKEN = 'course';
export const LOGGED_TOKEN = 'logged';

export function checkLogged() {
  let session = storage.getCookie(SESSION_TOKEN);

  if (session == 'true' || session == 'false') {
    let logged = storage.getLocal(LOGGED_TOKEN);
    storage.persist(LOGGED_TOKEN, String(logged == 'true'), false);
    return logged == 'true';
  }

  storage.createCookie(SESSION_TOKEN, 'true');
  storage.persist(LOGGED_TOKEN, 'false', false);
  return false;
}

export async function testToken() {
  let user: User | null;

  try {
    user = await RemoteServices.checkToken();
  } catch (Error) {
    user = null;
  }

  let session = storage.getCookie(SESSION_TOKEN);

  if (user != null) {
    store.commit('session', session == 'true');
    store.commit('login', user);

    // Check if more than 1 course
    if (store.getters.getCurrentCourse == null && user.coursesNumber != 1) {
      let course = storage.get(COURSE_TOKEN, session == 'true');

      try {
        if (course && JSON.parse(course)) {
          let parsed = new Course(JSON.parse(course));

          let array = user.courses[parsed.name!];
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
      store.commit('logout');

      return false;
    } else {
      store.commit(
        'currentCourse',
        (Object.values(user.courses)[0] as Course[])[0]
      );
    }
  } else {
    storage.removeAll(COURSE_TOKEN);
  }

  return true;
}

export async function logout() {
  await RemoteServices.logout();
  storage.persist(LOGGED_TOKEN, 'false', false);
  storage.removeAll(COURSE_TOKEN);
}
