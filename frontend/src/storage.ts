export function persist(name: string, value: string, session: boolean = true) {
  if (session) {
    sessionStorage.setItem(name, value);
  } else {
    localStorage.setItem(name, value);
  }
}

export function get(name: string, session: boolean = true) {
  if (session) {
    return getSession(name);
  } else {
    return getLocal(name);
  }
}

export function getSession(name: string) {
  return sessionStorage.getItem(name);
}

export function getLocal(name: string) {
  return localStorage.getItem(name);
}

export function remove(name: string, session: boolean = true) {
  if (session) {
    sessionStorage.removeItem(name);
  } else {
    localStorage.removeItem(name);
  }
}

export function removeAll(name: string) {
  sessionStorage.removeItem(name);
  localStorage.removeItem(name);
}
