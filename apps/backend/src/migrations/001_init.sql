-- LEGO backend schema v1. Timestamps are unix epoch milliseconds (INTEGER).

CREATE TABLE users (
  id            TEXT PRIMARY KEY,               -- random id, never the MC uuid
  mc_uuid       TEXT NOT NULL UNIQUE,           -- dashed lowercase uuid from the session server
  mc_name       TEXT NOT NULL,
  mc_name_lower TEXT NOT NULL,
  credits       INTEGER NOT NULL DEFAULT 0 CHECK (credits >= 0),
  bio           TEXT NOT NULL DEFAULT '',
  banned_until  INTEGER,
  ban_reason    TEXT,
  created_at    INTEGER NOT NULL,
  last_seen     INTEGER NOT NULL,
  -- privacy: 'everyone' | 'friends' | 'nobody'
  privacy_profile  TEXT NOT NULL DEFAULT 'everyone',
  privacy_status   TEXT NOT NULL DEFAULT 'friends',
  privacy_activity TEXT NOT NULL DEFAULT 'friends',
  privacy_messages TEXT NOT NULL DEFAULT 'friends',
  allow_friend_requests INTEGER NOT NULL DEFAULT 1,
  custom_tag_text TEXT
);
CREATE INDEX users_name ON users(mc_name_lower);

CREATE TABLE roles (
  user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  role    TEXT NOT NULL,
  granted_by TEXT,
  granted_at INTEGER NOT NULL,
  PRIMARY KEY (user_id, role)
);

CREATE TABLE sessions (
  token_hash TEXT PRIMARY KEY,                  -- sha256 of the bearer/cookie token
  user_id    TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  kind       TEXT NOT NULL,                     -- 'client' | 'web'
  csrf       TEXT NOT NULL,
  created_at INTEGER NOT NULL,
  expires_at INTEGER NOT NULL,
  last_used  INTEGER NOT NULL,
  user_agent TEXT
);
CREATE INDEX sessions_user ON sessions(user_id);

CREATE TABLE auth_challenges (
  id         TEXT PRIMARY KEY,
  server_id  TEXT NOT NULL,
  created_at INTEGER NOT NULL,
  used       INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE web_codes (
  code_hash  TEXT PRIMARY KEY,
  user_id    TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  expires_at INTEGER NOT NULL,
  used       INTEGER NOT NULL DEFAULT 0
);

-- Every credit change. users.credits is updated in the same transaction.
CREATE TABLE credit_ledger (
  id            INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id       TEXT NOT NULL REFERENCES users(id),
  delta         INTEGER NOT NULL,
  balance_after INTEGER NOT NULL,
  kind          TEXT NOT NULL,   -- grant|deduct|purchase|daily|quest|battlepass|promo|payment|subscription|refund
  reason        TEXT NOT NULL,
  ref           TEXT,
  actor_id      TEXT,            -- admin who made the change, null for system
  idem_key      TEXT UNIQUE,     -- prevents double booking
  created_at    INTEGER NOT NULL
);
CREATE INDEX ledger_user ON credit_ledger(user_id, id);

CREATE TABLE audit_log (
  id         INTEGER PRIMARY KEY AUTOINCREMENT,
  actor_id   TEXT,
  action     TEXT NOT NULL,
  target_id  TEXT,
  details    TEXT NOT NULL DEFAULT '{}',
  ip         TEXT,
  created_at INTEGER NOT NULL
);
CREATE INDEX audit_target ON audit_log(target_id, id);

CREATE TABLE inventory (
  user_id     TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  item_id     TEXT NOT NULL,
  source      TEXT NOT NULL,     -- shop|battlepass|grant|event|staff|gift
  acquired_at INTEGER NOT NULL,
  PRIMARY KEY (user_id, item_id)
);

CREATE TABLE equipped (
  user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  slot    TEXT NOT NULL,
  item_id TEXT NOT NULL,
  PRIMARY KEY (user_id, slot)
);

CREATE TABLE friend_requests (
  from_id    TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  to_id      TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  created_at INTEGER NOT NULL,
  PRIMARY KEY (from_id, to_id)
);

-- Stored in both directions so each side can keep its own note.
CREATE TABLE friends (
  user_id    TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  friend_id  TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  note       TEXT NOT NULL DEFAULT '',
  created_at INTEGER NOT NULL,
  PRIMARY KEY (user_id, friend_id)
);

CREATE TABLE blocks (
  user_id    TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  blocked_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  created_at INTEGER NOT NULL,
  PRIMARY KEY (user_id, blocked_id)
);

CREATE TABLE reports (
  id          INTEGER PRIMARY KEY AUTOINCREMENT,
  reporter_id TEXT NOT NULL REFERENCES users(id),
  target_id   TEXT NOT NULL REFERENCES users(id),
  reason      TEXT NOT NULL,
  details     TEXT NOT NULL DEFAULT '',
  status      TEXT NOT NULL DEFAULT 'open',  -- open|actioned|dismissed
  handled_by  TEXT,
  created_at  INTEGER NOT NULL
);

CREATE TABLE messages (
  id         INTEGER PRIMARY KEY AUTOINCREMENT,
  from_id    TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  to_id      TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  body       TEXT NOT NULL,
  created_at INTEGER NOT NULL,
  read_at    INTEGER
);
CREATE INDEX messages_pair ON messages(from_id, to_id, id);
CREATE INDEX messages_to ON messages(to_id, read_at);

CREATE TABLE parties (
  id         TEXT PRIMARY KEY,
  leader_id  TEXT NOT NULL REFERENCES users(id),
  created_at INTEGER NOT NULL
);
CREATE TABLE party_members (
  party_id  TEXT NOT NULL REFERENCES parties(id) ON DELETE CASCADE,
  user_id   TEXT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
  joined_at INTEGER NOT NULL,
  PRIMARY KEY (party_id, user_id)
);
CREATE TABLE party_invites (
  party_id   TEXT NOT NULL REFERENCES parties(id) ON DELETE CASCADE,
  user_id    TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  invited_by TEXT NOT NULL,
  created_at INTEGER NOT NULL,
  PRIMARY KEY (party_id, user_id)
);
CREATE TABLE party_messages (
  id         INTEGER PRIMARY KEY AUTOINCREMENT,
  party_id   TEXT NOT NULL REFERENCES parties(id) ON DELETE CASCADE,
  from_id    TEXT NOT NULL REFERENCES users(id),
  body       TEXT NOT NULL,
  created_at INTEGER NOT NULL
);

CREATE TABLE feedback (
  id         INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id    TEXT NOT NULL REFERENCES users(id),
  kind       TEXT NOT NULL,
  title      TEXT NOT NULL,
  body       TEXT NOT NULL,
  status     TEXT NOT NULL DEFAULT 'new',
  votes      INTEGER NOT NULL DEFAULT 0,
  created_at INTEGER NOT NULL,
  updated_at INTEGER NOT NULL
);
CREATE INDEX feedback_status ON feedback(status, id);
CREATE TABLE feedback_votes (
  feedback_id INTEGER NOT NULL REFERENCES feedback(id) ON DELETE CASCADE,
  user_id     TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  PRIMARY KEY (feedback_id, user_id)
);
CREATE TABLE feedback_comments (
  id          INTEGER PRIMARY KEY AUTOINCREMENT,
  feedback_id INTEGER NOT NULL REFERENCES feedback(id) ON DELETE CASCADE,
  user_id     TEXT NOT NULL REFERENCES users(id),
  body        TEXT NOT NULL,
  staff       INTEGER NOT NULL DEFAULT 0,
  created_at  INTEGER NOT NULL
);
CREATE TABLE attachments (
  id          TEXT PRIMARY KEY,
  feedback_id INTEGER NOT NULL REFERENCES feedback(id) ON DELETE CASCADE,
  filename    TEXT NOT NULL,
  mime        TEXT NOT NULL,
  size        INTEGER NOT NULL,
  sha256      TEXT NOT NULL,
  created_at  INTEGER NOT NULL
);

CREATE TABLE promo_codes (
  code       TEXT PRIMARY KEY,
  credits    INTEGER NOT NULL CHECK (credits > 0),
  item_id    TEXT,
  max_uses   INTEGER NOT NULL,
  uses       INTEGER NOT NULL DEFAULT 0,
  expires_at INTEGER,
  created_by TEXT,
  created_at INTEGER NOT NULL
);
CREATE TABLE promo_redemptions (
  code    TEXT NOT NULL REFERENCES promo_codes(code),
  user_id TEXT NOT NULL REFERENCES users(id),
  created_at INTEGER NOT NULL,
  PRIMARY KEY (code, user_id)
);

CREATE TABLE daily_claims (
  user_id   TEXT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
  last_day  TEXT NOT NULL,       -- YYYY-MM-DD (UTC)
  streak    INTEGER NOT NULL
);

-- Metric counters per period ('d:2026-10-03', 'w:2026-W40', 'total').
CREATE TABLE metrics (
  user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  metric  TEXT NOT NULL,
  period  TEXT NOT NULL,
  value   INTEGER NOT NULL DEFAULT 0,
  PRIMARY KEY (user_id, metric, period)
);
CREATE TABLE quest_claims (
  user_id  TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  quest_id TEXT NOT NULL,
  period   TEXT NOT NULL,
  claimed_at INTEGER NOT NULL,
  PRIMARY KEY (user_id, quest_id, period)
);

CREATE TABLE pass_progress (
  user_id   TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  season_id TEXT NOT NULL,
  xp        INTEGER NOT NULL DEFAULT 0,
  premium   INTEGER NOT NULL DEFAULT 0,
  PRIMARY KEY (user_id, season_id)
);
CREATE TABLE pass_claims (
  user_id   TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  season_id TEXT NOT NULL,
  tier      INTEGER NOT NULL,
  track     TEXT NOT NULL,       -- free|premium
  claimed_at INTEGER NOT NULL,
  PRIMARY KEY (user_id, season_id, tier, track)
);

CREATE TABLE subscriptions (
  user_id    TEXT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
  tier       TEXT NOT NULL,
  expires_at INTEGER NOT NULL,
  updated_at INTEGER NOT NULL
);

-- Verified payment events (Stripe). The unique event id makes webhook
-- delivery idempotent.
CREATE TABLE payments (
  event_id   TEXT PRIMARY KEY,
  user_id    TEXT NOT NULL REFERENCES users(id),
  product_id TEXT NOT NULL,
  amount     INTEGER NOT NULL,
  currency   TEXT NOT NULL,
  created_at INTEGER NOT NULL
);

CREATE TABLE game_results (
  id         INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id    TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  game_id    TEXT NOT NULL,
  score      INTEGER NOT NULL,
  won        INTEGER NOT NULL DEFAULT 0,
  created_at INTEGER NOT NULL
);
CREATE INDEX game_results_board ON game_results(game_id, score DESC);

CREATE TABLE news (
  id         INTEGER PRIMARY KEY AUTOINCREMENT,
  title      TEXT NOT NULL,
  body       TEXT NOT NULL,
  kind       TEXT NOT NULL DEFAULT 'news',   -- news|changelog|maintenance
  author_id  TEXT REFERENCES users(id),
  published  INTEGER NOT NULL DEFAULT 1,
  created_at INTEGER NOT NULL
);

CREATE TABLE settings_sync (
  user_id    TEXT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
  data       TEXT NOT NULL,
  updated_at INTEGER NOT NULL
);

CREATE TABLE system_state (
  key   TEXT PRIMARY KEY,
  value TEXT NOT NULL
);
