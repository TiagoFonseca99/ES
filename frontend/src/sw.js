// File is based on https://golb.hplar.ch/2019/08/webpush-java.html

self.addEventListener('activate', event => event.waitUntil(clients.claim()));

self.addEventListener('push', event => event.waitUntil(handlePushEvent(event)));

self.addEventListener('notificationclick', event =>
  event.waitUntil(handleNotificationClick(event))
);

self.addEventListener('notificationclose', event =>
  console.info('notificationclose event fired')
);

async function handlePushEvent(event) {
  console.info('Push event received');

  const visible = await visibleNotifications();
  const notifications = await caches.open('notifications');

  console.info('Notification received');

  const msg = event.data.json();

  if (!visible) {
    await self.registration.showNotification(msg.title, {
      body: msg.body,
      icon: '/icon.png',
      data: msg.type,
      vibrate: [200, 200, 50, 50, 200, 200]
    });
  }

  await notifications.put('notification', new Response(msg.body));

  const allClients = await clients.matchAll({ includeUncontrolled: true });
  for (const client of allClients) {
    client.postMessage('data-updated');
  }
}

async function handleNotificationClick(event) {
  console.log(event);
  let openClient = null;
  const allClients = await clients.matchAll({
    includeUncontrolled: true,
    type: 'window'
  });

  // TODO: use router and switch to redirect to URL
  openClient = allClients[0];

  if (openClient) {
    await openClient.focus();
  } else {
    await clients.openWindow(urlToOpen1);
  }

  event.notification.close();
}

async function visibleNotifications() {
  const allClients = await clients.matchAll({ includeUncontrolled: true });
  for (const client of allClients) {
    if (client.visibilityState === 'visible') {
      return true;
    }
  }

  return false;
}
