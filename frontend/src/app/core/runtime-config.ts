import { InjectionToken } from '@angular/core';

export interface RuntimeConfig {
  apiBaseUrl: string;
  googleClientId: string;
  requestTimeoutMs: number;
}
export const RUNTIME_CONFIG = new InjectionToken<RuntimeConfig>('Public runtime configuration');
export function parseRuntimeConfig(value: unknown): RuntimeConfig {
  if (!value || typeof value !== 'object') throw new Error('Invalid configuration');
  const v = value as Record<string, unknown>;
  if (
    typeof v['apiBaseUrl'] !== 'string' ||
    typeof v['googleClientId'] !== 'string' ||
    typeof v['requestTimeoutMs'] !== 'number'
  )
    throw new Error('Invalid configuration');
  const url = new URL(v['apiBaseUrl']);
  if (
    url.username ||
    url.password ||
    url.search ||
    url.hash ||
    !(
      url.protocol === 'https:' ||
      (url.protocol === 'http:' && ['localhost', '127.0.0.1', '[::1]'].includes(url.hostname))
    )
  )
    throw new Error('Invalid API origin');
  if (
    !Number.isInteger(v['requestTimeoutMs']) ||
    v['requestTimeoutMs'] < 1000 ||
    v['requestTimeoutMs'] > 60000
  )
    throw new Error('Invalid timeout');
  if (v['googleClientId'] && !/^[\w-]+\.apps\.googleusercontent\.com$/.test(v['googleClientId']))
    throw new Error('Invalid client ID');
  return {
    apiBaseUrl: v['apiBaseUrl'].replace(/\/$/, ''),
    googleClientId: v['googleClientId'],
    requestTimeoutMs: v['requestTimeoutMs'],
  };
}
