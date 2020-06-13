import Cookies from 'js-cookie';

export function persist(name: string, value: string) {
  localStorage.setItem(name, value);
}

export function get(name: string) {
  return localStorage.getItem(name);
}

export function remove(name: string) {
  localStorage.removeItem(name);
}

export function createCookie(
  name: string,
  value: string,
  expiry: number | undefined = undefined
) {
  Cookies.set(name, value, { expires: expiry, sameSite: 'strict' });
}

export function getCookie(name: string) {
  return Cookies.get(name);
}

export function removeCookie(name: string) {
  Cookies.remove(name);
}
