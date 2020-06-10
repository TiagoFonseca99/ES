import store from '@/store';
import * as storage from '@/storage';
import RemoteServices from '@/services/RemoteServices';
import Course from '@/models/user/Course';
import User from '@/models/user/User';
import router from '@/router';

export const SESSION_TOKEN = 'session';
export const COURSE_TOKEN = 'course';
export const LOGGED_TOKEN = 'logged';

export function checkLogged() {
  let session = storage.getCookie(SESSION_TOKEN);

  if (session == 'true' || session == 'false') {
    let logged = storage.get(LOGGED_TOKEN);
    storage.persist(LOGGED_TOKEN, String(logged == 'true'));
    return logged == 'true';
  }

  storage.createCookie(SESSION_TOKEN, 'true');
  storage.persist(LOGGED_TOKEN, 'false');
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
  } else {
    storage.remove(COURSE_TOKEN);
  }

  return true;
}

export async function checkCourse(user: User) {
  if (store.getters.getCurrentCourse == null && user.coursesNumber > 1) {
    let course = storage.get(COURSE_TOKEN);

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

    await router.push({ name: 'courses' });

    return true;
  } else if (user.coursesNumber === 1) {
    store.commit(
      'currentCourse',
      (Object.values(user.courses)[0] as Course[])[0]
    );
  }

  await router.push({ name: 'home' });

  return true;
}

export async function logout() {
  storage.persist(LOGGED_TOKEN, 'false');
  await RemoteServices.logout();
}
