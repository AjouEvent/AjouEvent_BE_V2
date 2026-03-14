self.addEventListener("install", function (e) {
  console.log("fcm sw install..");
  self.skipWaiting();
});

self.addEventListener("activate", function (e) {
  console.log("fcm sw activate..");
});

self.addEventListener("push", function (event) {
  if (!event.data || !event.data.json()) {
    console.error("Push event does not contain valid JSON data.");
    return;
  }

  const data = event.data.json();
  console.log("Received push notification:", data);

  // 🔹 `unread_count`가 문자열일 경우 숫자로 변환
  const unreadCount = data.data?.unread_count ? Number(data.data.unread_count) : 0;
  console.log("Parsed unread count:", unreadCount);
  

  let promises = [];

  // 🔹 iOS 및 지원되는 기기에서 배지 업데이트
  if ("setAppBadge" in self.navigator) {
    console.log(unreadCount)
    const badgePromise = self.navigator.setAppBadge(unreadCount).catch(console.error);
    promises.push(badgePromise);
  }

  // 🔹 iOS에서는 showNotification() 필수 실행
  const notificationPromise = self.registration.showNotification(data.notification.title, {
    body: data.notification.body,
    icon: data.notification.icon,
    data: { click_action: data.data.click_action },
  });
  promises.push(notificationPromise);

  // 🔹 PWA 실행 중일 경우 UI 동기화
  self.clients.matchAll({ type: "window", includeUncontrolled: true }).then((clients) => {
    clients.forEach((client) => {
      client.postMessage({
        type: "updateBadge",
        count: unreadCount, // 🟢 안 읽은 알림 개수 전달
      });
    });
  });

  event.waitUntil(Promise.all(promises));
});

self.addEventListener("notificationclick", function (event) {
  const resultURL =
    event.notification.data && event.notification.data.click_action;

  if (!resultURL) {
    const notificationTitle = "URL is missing.";
    const notificationOptions = {
      body: "Notification click action URL is missing.",
    };
    self.registration.showNotification(notificationTitle, notificationOptions);
    return;
  }

  event.waitUntil(
    clients
      .matchAll({ type: "window", includeUncontrolled: true })
      .then(function (clientList) {
        for (var i = 0; i < clientList.length; i++) {
          var client = clientList[i];
          if (client.url === resultURL && "focus" in client) {
            return client.focus();
          }
        }
        if (clients.openWindow) {
          return clients.openWindow(resultURL);
        }
      })
  );

  event.notification.close();
});
