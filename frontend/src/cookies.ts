import Cookies from 'js-cookie';
import store from '@/store.ts';

export function checkLogged(name: string) {
  let token = Cookies.get(name);

  if (token && token.trim().length != 0) {
    store.commit('token', token);
    return true;
  } else {
    store.commit('token', '');
    return false;
  }
}

export function createCookie(name: string, value: string) {
  Cookies.set(name, value);
}

export function deleteCookie(name: string) {
  Cookies.remove(name);
}
