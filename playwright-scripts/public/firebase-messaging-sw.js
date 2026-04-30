self.addEventListener('install', function () {
  self.skipWaiting();
});

self.addEventListener('activate', function () {});

self.addEventListener('push', function (event) {
  if (!event.data) return;
  const data = event.data.json();
  event.waitUntil(
    self.registration.showNotification(data.notification.title, {
      body: data.notification.body,
      icon: data.notification.icon,
    })
  );
});
