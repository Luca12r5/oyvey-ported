-- Player-designed capes: stored as validated design data (CapeSpec JSON), never as images.
CREATE TABLE custom_capes (
  user_id TEXT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
  spec_json TEXT NOT NULL,
  spec_id TEXT NOT NULL,
  updated_at INTEGER NOT NULL
);
-- Daily budget for AI cape designs (cost control).
CREATE TABLE cape_ai_usage (
  user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  day TEXT NOT NULL,
  count INTEGER NOT NULL DEFAULT 0,
  PRIMARY KEY (user_id, day)
);
