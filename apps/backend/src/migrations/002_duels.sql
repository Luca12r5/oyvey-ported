-- Who may challenge you to a duel: 'everyone' | 'friends' | 'nobody' (blocks always apply).
ALTER TABLE users ADD COLUMN privacy_duels TEXT NOT NULL DEFAULT 'everyone';
