// Operator CLI (run on the server, not exposed over HTTP):
//   npm run cli -w apps/backend -- grant-role <name|uuid|id> <admin|developer|moderator|support>
//   npm run cli -w apps/backend -- revoke-role <user> <role>
//   npm run cli -w apps/backend -- backup [file]
//   npm run cli -w apps/backend -- user <user>

import { join } from 'node:path';
import { ROLES, type Role } from '@lego/shared';
import { loadConfig } from './config.ts';
import { Db } from './db.ts';
import { audit } from './services/audit.ts';
import { findUser, rolesOf } from './services/users.ts';

const config = loadConfig();
const db = new Db(join(config.dataDir, 'lego.sqlite'));
db.migrate();
const [cmd, a, b] = process.argv.slice(2);

function user(ref: string | undefined) {
  if (!ref) throw new Error('missing user');
  const u = findUser(db, ref);
  if (!u) throw new Error(`user ${ref} not found (they must log in through the launcher once)`);
  return u;
}

try {
  switch (cmd) {
    case 'grant-role':
    case 'revoke-role': {
      const u = user(a);
      if (!b || !(ROLES as readonly string[]).includes(b)) throw new Error(`role must be one of ${ROLES.join(', ')}`);
      if (cmd === 'grant-role') db.run('INSERT OR IGNORE INTO roles(user_id, role, granted_by, granted_at) VALUES (:u, :r, :g, :t)', { u: u.id, r: b as Role, g: 'cli', t: Date.now() });
      else db.run('DELETE FROM roles WHERE user_id = :u AND role = :r', { u: u.id, r: b });
      audit(db, null, cmd === 'grant-role' ? 'roles.grant' : 'roles.revoke', u.id, { role: b, via: 'cli' }, null);
      console.log(`${u.mc_name}: ${rolesOf(db, u.id).join(', ') || '(no roles)'}`);
      break;
    }
    case 'backup': {
      const file = a ?? join(config.dataDir, 'backups', `lego-${new Date().toISOString().replace(/[:.]/g, '-')}.sqlite`);
      db.backup(file);
      console.log(`backup written to ${file}`);
      break;
    }
    case 'user': {
      const u = user(a);
      console.log({ ...u, roles: rolesOf(db, u.id) });
      break;
    }
    default:
      console.log('usage: cli.ts grant-role|revoke-role <user> <role> | backup [file] | user <user>');
      process.exitCode = 1;
  }
} catch (e) {
  console.error(String(e instanceof Error ? e.message : e));
  process.exitCode = 1;
} finally {
  db.close();
}
