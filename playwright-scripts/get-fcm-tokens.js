const { chromium } = require('playwright');
const axios = require('axios');
const dotenv = require('dotenv');
const http = require('http');
const fs = require('fs');
const os = require('os');
const path = require('path');

dotenv.config();

// ── 환경 변수 ───────────────────────────────────────────────────────────────
const FIREBASE_CONFIG = {
  apiKey: process.env.FIREBASE_API_KEY,
  authDomain: process.env.FIREBASE_AUTH_DOMAIN,
  projectId: process.env.FIREBASE_PROJECT_ID,
  storageBucket: process.env.FIREBASE_STORAGE_BUCKET,
  messagingSenderId: process.env.FIREBASE_MESSAGING_SENDER_ID,
  appId: process.env.FIREBASE_APP_ID,
  measurementId: process.env.FIREBASE_MEASUREMENT_ID,
};
const VAPID_KEY = process.env.VAPID_KEY;
const BE_URL = process.env.BE_URL || 'http://localhost:8080';
const TOKEN_COUNT = parseInt(process.env.TOKEN_COUNT || '1', 10);
const CONCURRENCY = parseInt(process.env.CONCURRENCY || '5', 10);
const HEADLESS = process.env.HEADLESS !== 'false';
const PORT = 4000;

// ── 정적 파일 서버 (public/ 폴더) ─────────────────────────────────────────
function startServer() {
  const publicDir = path.join(__dirname, 'public');
  const mimeTypes = {
    '.html': 'text/html',
    '.js': 'application/javascript',
  };

  const server = http.createServer((req, res) => {
    const filePath = path.join(publicDir, req.url === '/' ? '/get-token.html' : req.url);
    const ext = path.extname(filePath);
    const contentType = mimeTypes[ext] || 'text/plain';

    fs.readFile(filePath, (err, data) => {
      if (err) {
        res.writeHead(404);
        res.end('Not found: ' + req.url);
        return;
      }
      res.writeHead(200, { 'Content-Type': contentType });
      res.end(data);
    });
  });

  return new Promise((resolve) => {
    server.listen(PORT, () => {
      console.log(`[server] Serving public/ on http://localhost:${PORT}`);
      resolve(server);
    });
  });
}

// HEADLESS=false 일 때 열어둘 컨텍스트 목록 (main 종료 전 대기용)
const openContexts = [];

// ── 토큰 1개 발급 ──────────────────────────────────────────────────────────
// browser.newContext()는 incognito 모드 → Chrome Push API 완전 차단
// launchPersistentContext()로 일반 프로필 모드 사용
async function fetchOneToken(index) {
  const userDataDir = path.join(os.tmpdir(), `pw-fcm-${index}-${Date.now()}`);
  const context = await chromium.launchPersistentContext(userDataDir, {
    channel: 'chrome',
    headless: HEADLESS,
    permissions: ['notifications'],
  });
  await context.grantPermissions(['notifications']);

  const page = await context.newPage();

  await page.addInitScript(
    ({ config, vapidKey }) => {
      window.__FIREBASE_CONFIG__ = config;
      window.__VAPID_KEY__ = vapidKey;
    },
    { config: FIREBASE_CONFIG, vapidKey: VAPID_KEY }
  );

  try {
    await page.goto(`http://localhost:${PORT}/get-token.html`);

    // 최대 30초 대기 — getToken()이 완료되면 title이 바뀜
    await page.waitForFunction(
      () => document.title === 'TOKEN_READY' || document.title === 'TOKEN_ERROR',
      { timeout: 30_000 }
    );

    const title = await page.title();
    if (title === 'TOKEN_ERROR') {
      const error = await page.evaluate(() => window.__FCM_ERROR__);
      throw new Error(error);
    }

    const token = await page.evaluate(() => window.__FCM_TOKEN__);
    console.log(`[${index + 1}] token: ${token.slice(0, 30)}...`);

    if (!HEADLESS) {
      // 창을 닫지 않고 목록에 보관 — 저장 완료 후 main()에서 대기
      openContexts.push({ context, userDataDir });
    }

    return token;
  } finally {
    if (HEADLESS) {
      await context.close();
      fs.rmSync(userDataDir, { recursive: true, force: true });
    }
  }
}

// ── BE에 토큰 등록 ─────────────────────────────────────────────────────────
async function registerTokenWithBE(token, index) {
  const email = `test${index + 1}@ajou.ac.kr`;
  const response = await axios.post(`${BE_URL}/api/v2/auth/test-login/fcm`, {
    email,
    fcmToken: token,
  });
  return { email, accessToken: response.data.accessToken, fcmToken: token };
}

// ── 토큰 발급 + BE 등록 묶음 ───────────────────────────────────────────────
async function fetchAndRegister(index) {
  console.log(`[${index + 1}/${TOKEN_COUNT}] 토큰 발급 중...`);
  const token = await fetchOneToken(index);
  const registered = await registerTokenWithBE(token, index);
  console.log(`[${index + 1}/${TOKEN_COUNT}] ✅ BE 등록 완료: ${registered.email}`);
  return { ...registered, success: true };
}

// ── 메인 ───────────────────────────────────────────────────────────────────
async function main() {
  console.log(`\n🚀 FCM 토큰 ${TOKEN_COUNT}개 발급 시작 (동시 ${CONCURRENCY}개)\n`);

  if (!FIREBASE_CONFIG.apiKey || !VAPID_KEY) {
    console.error('[error] .env 파일을 확인하세요. FIREBASE_* 와 VAPID_KEY 가 필요합니다.');
    process.exit(1);
  }

  const server = await startServer();
  const results = new Array(TOKEN_COUNT);

  // 동시 실행 풀: CONCURRENCY개의 워커가 남은 작업을 순서대로 처리
  let next = 0;
  const worker = async () => {
    while (next < TOKEN_COUNT) {
      const i = next++;
      try {
        results[i] = await fetchAndRegister(i);
      } catch (err) {
        console.error(`[${i + 1}/${TOKEN_COUNT}] ❌ 실패: ${err.message}`);
        results[i] = { index: i, error: err.message, success: false };
      }
    }
  };

  await Promise.all(Array.from({ length: Math.min(CONCURRENCY, TOKEN_COUNT) }, worker));

  server.close();

  // 결과 저장
  const outputPath = path.join(__dirname, 'tokens.json');
  fs.writeFileSync(outputPath, JSON.stringify(results, null, 2));

  const succeeded = results.filter((r) => r.success).length;
  const failed = results.filter((r) => !r.success).length;
  const tokenCsv = results
    .filter((r) => r.success)
    .map((r) => r.fcmToken)
    .join(',');

  const csvPath = path.join(__dirname, 'tokens.csv');
  fs.writeFileSync(csvPath, tokenCsv);

  console.log(`\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`);
  console.log(`결과: 성공 ${succeeded}개 / 실패 ${failed}개`);
  console.log(`저장(JSON): ${outputPath}`);
  console.log(`저장(CSV) : ${csvPath}`);
  console.log(`\n[FCM 토큰 목록]`);
  console.log(tokenCsv);
  console.log(`━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n`);

  if (!HEADLESS && openContexts.length > 0) {
    console.log(`🔔 Chrome 창 ${openContexts.length}개 유지 중 — push 알림 수신 대기`);
    console.log(`   Ctrl+C 로 종료하면 창이 닫힙니다.\n`);
    await new Promise(() => {});
  }
}

main().catch((err) => {
  console.error('[fatal]', err);
  process.exit(1);
});