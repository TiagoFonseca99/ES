// File is based on https://golb.hplar.ch/2019/08/webpush-java.html

self.addEventListener('activate', event => event.waitUntil(clients.claim()));

self.addEventListener('push', event => event.waitUntil(handlePushEvent(event)));

self.addEventListener('notificationclick', event =>
  event.waitUntil(handleNotificationClick(event))
);

async function handlePushEvent(event) {
  const notifications = await caches.open('notifications');

  const msg = event.data.json();

  await self.registration.showNotification(msg.title, {
    body: msg.content,
    icon: '/logo-192x192.png',
    data: msg.type,
    vibrate: [200, 200, 50, 50, 200, 200]
  });

  await notifications.put(msg.id, new Response(msg));

  const allClients = await clients.matchAll({ includeUncontrolled: true });
  for (const client of allClients) {
    client.postMessage(msg);
  }
}

async function handleNotificationClick(event) {
  let openClient = null;
  const allClients = await clients.matchAll({
    includeUncontrolled: true,
    type: 'window'
  });

  openClient = allClients[0];

  if (openClient) {
    await openClient.focus();
  }

  event.notification.close();
}
