importScripts("https://www.gstatic.com/firebasejs/10.12.0/firebase-app-compat.js");
importScripts("https://www.gstatic.com/firebasejs/10.12.0/firebase-messaging-compat.js");

firebase.initializeApp({
  apiKey:            "AIzaSyCFwu1ABxO454F3Q9ZzNvwKn5qBwOiewqc",
  authDomain:        "hello-22d56.firebaseapp.com",
  projectId:         "hello-22d56",
  storageBucket:     "hello-22d56.firebasestorage.app",
  messagingSenderId: "827125474375",
  appId:             "1:827125474375:web:641871262fa276ff5c7d62",
});

const messaging = firebase.messaging();
