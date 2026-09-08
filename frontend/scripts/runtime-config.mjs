import { existsSync, writeFileSync } from 'node:fs';
for (const file of ['.env.local', '.env']) {
  if (existsSync(file)) process.loadEnvFile(file);
}
const apiBaseUrl = process.env.PUBLIC_API_BASE_URL;
const googleClientId = process.env.PUBLIC_GOOGLE_CLIENT_ID ?? '';
const requestTimeoutMs = Number(process.env.PUBLIC_REQUEST_TIMEOUT_MS);
if (
  !apiBaseUrl ||
  !Number.isInteger(requestTimeoutMs) ||
  requestTimeoutMs < 1000 ||
  requestTimeoutMs > 60000
) {
  throw new Error(
    'Configure PUBLIC_API_BASE_URL and PUBLIC_REQUEST_TIMEOUT_MS in frontend/.env.local. See .env.example.',
  );
}
const url = new URL(apiBaseUrl);
if (
  url.username ||
  url.password ||
  url.search ||
  url.hash ||
  !(
    url.protocol === 'https:' ||
    (url.protocol === 'http:' && ['localhost', '127.0.0.1', '[::1]'].includes(url.hostname))
  )
) {
  throw new Error(
    'API must use HTTPS, except loopback development. Credentials and query parameters are forbidden.',
  );
}
if (googleClientId && !/^[\w-]+\.apps\.googleusercontent\.com$/.test(googleClientId))
  throw new Error('Invalid public Google Client ID.');
// Explicit allowlist: never serialize process.env or backend secrets.
writeFileSync(
  'public/runtime-config.json',
  JSON.stringify({ apiBaseUrl: apiBaseUrl.replace(/\/$/, ''), googleClientId, requestTimeoutMs }),
);
