// File is based on https://golb.hplar.ch/2019/08/webpush-java.html

import { register } from 'register-service-worker';
import RemoteServices from '@/services/RemoteServices';
import axios from 'axios';

let key!: string;
let subscribed: Boolean = false;

export function registerWorker() {
  if (process.env.NODE_ENV === 'production') {
    register(`${process.env.BASE_URL}sw.js`, {
      async ready() {
        navigator.serviceWorker.addEventListener('message', () => {
          displayLastMessages();
        });
      },

      async registered() {
        key = await RemoteServices.getWorkerKey();
        await subscribe();
        console.log(Notification.permission);
      },

      cached() {},
      updatefound() {},
      updated() {},
      offline() {},
      error(error) {
        console.error('Error during service worker registration:', error);
      }
    });
  }
}

async function isSubscribed(subscription: PushSubscription) {
  subscribed = await RemoteServices.isWorkerSubscribed(subscription);
}

async function subscribe() {
  const registration = await navigator.serviceWorker.ready;
  console.log(key);
  const subscription = await registration.pushManager.subscribe({
    userVisibleOnly: true,
    applicationServerKey: key
  });

  if (subscription) {
    await isSubscribed(subscription);
    if (!subscribed) {
      await RemoteServices.subscribeWorker(subscription);
    }
  } else {
    console.error('Subscription denied');
  }
}

async function displayLastMessages() {
  caches.open('notifications').then(data => {
    console.log(data); // Add to notifications
  });
}
