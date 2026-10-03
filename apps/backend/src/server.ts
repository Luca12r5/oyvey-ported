import { createServer } from 'node:http';
import { createApp } from './app.ts';
import { loadConfig } from './config.ts';

const config = loadConfig();
const app = createApp(config);
const persisted = app.db.get<{ value: string }>("SELECT value FROM system_state WHERE key = 'maintenance'");
if (persisted) config.maintenance = persisted.value === '1';

const server = createServer((req, res) => {
  void app.handle(req, res);
});
server.headersTimeout = 15_000;
server.requestTimeout = 30_000;
server.listen(config.port, config.host, () => {
  console.log(`[lego-backend] listening on http://${config.host}:${config.port} (data: ${config.dataDir})`);
  if (!config.secureCookies) console.warn('[lego-backend] LEGO_SECURE_COOKIES=0: cookies are not marked Secure (development only)');
});

function shutdown() {
  console.log('[lego-backend] shutting down');
  app.events.close();
  server.close(() => {
    app.db.close();
    process.exit(0);
  });
  setTimeout(() => process.exit(1), 5000).unref();
}
process.on('SIGINT', shutdown);
process.on('SIGTERM', shutdown);
