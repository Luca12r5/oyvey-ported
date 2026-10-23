-- Pictures in private messages (screenshots). The file lives in <dataDir>/chat/<id>;
-- only the sender and the receiver may load it.
ALTER TABLE messages ADD COLUMN image_id TEXT;
CREATE TABLE message_images (
  id         TEXT PRIMARY KEY,
  owner_id   TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  peer_id    TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  mime       TEXT NOT NULL,
  size       INTEGER NOT NULL,
  created_at INTEGER NOT NULL
);
CREATE INDEX message_images_owner ON message_images(owner_id, created_at);
