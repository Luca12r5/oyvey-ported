import { test } from 'node:test';
import assert from 'node:assert/strict';
import { call, grantRole, login, start } from './helpers.ts';

const PNG = Buffer.from('89504e470d0a1a0a0000000d49484452000000010000000108060000001f15c4890000000d4944415478da63f8ffff3f0005fe02fea7d6a3be0000000049454e44ae426082', 'hex');

test('friend requests, privacy-aware friend list, notes, removal', async (t) => {
  const h = await start();
  t.after(() => h.close());
  const a = await login(h, 'Alice');
  const b = await login(h, 'Bob');

  assert.equal((await a.call('POST', '/api/friends/requests', { player: 'Nobody' })).status, 404);
  assert.equal((await a.call('POST', '/api/friends/requests', { player: 'Alice' })).status, 400);
  assert.equal((await a.call('POST', '/api/friends/requests', { player: 'bob' })).body.status, 'requested');
  const incoming = await b.call('GET', '/api/friends');
  assert.equal(incoming.body.incoming[0].name, 'Alice');
  // Bob asking back accepts automatically.
  assert.equal((await b.call('POST', '/api/friends/requests', { player: 'Alice' })).body.status, 'friends');

  await b.call('POST', '/api/me/presence', { status: 'in_game', gameVersion: '1.21.11', activity: 'Tennis' });
  let fl = await a.call('GET', '/api/friends');
  assert.equal(fl.body.friends[0].status, 'in_game');
  assert.equal(fl.body.friends[0].activity, 'Tennis');

  // Bob hides his status from everyone.
  await b.call('PUT', '/api/me/privacy', { profile: 'everyone', status: 'nobody', activity: 'nobody', messages: 'friends', friendRequests: true });
  fl = await a.call('GET', '/api/friends');
  assert.equal(fl.body.friends[0].status, 'hidden');
  assert.equal(fl.body.friends[0].activity, null);

  assert.equal((await a.call('PUT', `/api/friends/${b.id}/note`, { note: 'met on tennis server' })).status, 200);
  assert.equal((await a.call('GET', '/api/friends')).body.friends[0].note, 'met on tennis server');
  await a.call('DELETE', `/api/friends/${b.id}`);
  assert.equal((await b.call('GET', '/api/friends')).body.friends.length, 0);
});

test('private messages respect privacy and blocks', async (t) => {
  const h = await start();
  t.after(() => h.close());
  const a = await login(h, 'Carol');
  const b = await login(h, 'Dave');

  // Default: messages only from friends.
  assert.equal((await a.call('POST', `/api/messages/${b.id}`, { body: 'hi' })).status, 403);
  await a.call('POST', '/api/friends/requests', { player: 'Dave' });
  await b.call('POST', `/api/friends/requests/${a.id}/accept`);
  const sent = await a.call('POST', `/api/messages/${b.id}`, { body: 'hi ‮evil' });
  assert.equal(sent.status, 200);
  assert.equal(sent.body.body, 'hi evil', 'bidi control characters are stripped');
  const conv = await b.call('GET', '/api/messages');
  assert.equal(conv.body.conversations[0].unread, 1);
  const thread = await b.call('GET', `/api/messages/${a.id}`);
  assert.equal(thread.body.messages.length, 1);
  assert.equal((await b.call('GET', '/api/messages')).body.conversations[0].unread, 0);

  await b.call('POST', '/api/blocks', { player: 'Carol' });
  assert.equal((await a.call('POST', `/api/messages/${b.id}`, { body: 'hello?' })).status, 403);
  assert.equal((await a.call('GET', '/api/friends')).body.friends.length, 0, 'blocking removes the friendship');
  assert.equal((await a.call('POST', '/api/friends/requests', { player: 'Dave' })).status, 403);
});

test('parties: invite friends only, join, chat, leader transfer, leave', async (t) => {
  const h = await start();
  t.after(() => h.close());
  const a = await login(h, 'Leader');
  const b = await login(h, 'Member');
  const c = await login(h, 'Stranger');
  await a.call('POST', '/api/friends/requests', { player: 'Member' });
  await b.call('POST', `/api/friends/requests/${a.id}/accept`);

  const p = await a.call('POST', '/api/party');
  assert.equal(p.status, 200);
  assert.equal((await a.call('POST', '/api/party/invite', { player: 'Stranger' })).status, 403);
  assert.equal((await a.call('POST', '/api/party/invite', { player: 'Member' })).status, 200);
  assert.equal((await c.call('POST', `/api/party/${p.body.id}/join`)).status, 404, 'no invite, no join');
  const joined = await b.call('POST', `/api/party/${p.body.id}/join`);
  assert.equal(joined.body.members.length, 2);

  assert.equal((await b.call('POST', '/api/party/invite', { player: 'Leader' })).status, 403, 'only the leader invites');
  await b.call('POST', '/api/party/messages', { body: 'gg' });
  assert.equal((await a.call('GET', '/api/party/messages')).body.messages[0].body, 'gg');

  await a.call('POST', '/api/party/leader', { userId: b.id });
  assert.equal((await a.call('GET', '/api/party')).body.party.leaderId, b.id);
  await b.call('POST', '/api/party/leave');
  assert.equal((await a.call('GET', '/api/party')).body.party.leaderId, a.id, 'leadership passes on when the leader leaves');
  await a.call('POST', '/api/party/leave');
  assert.equal((await a.call('GET', '/api/party')).body.party, null);
});

test('reports and moderation', async (t) => {
  const h = await start();
  t.after(() => h.close());
  const a = await login(h, 'Reporter');
  const b = await login(h, 'Griefer');
  const mod = await login(h, 'Moddy');
  grantRole(h, mod.id, 'moderator');
  assert.equal((await a.call('POST', '/api/reports', { player: 'Griefer', reason: 'harassment', details: 'spam in chat' })).status, 200);
  assert.equal((await a.call('POST', '/api/reports', { player: 'Griefer', reason: 'harassment', details: 'again' })).status, 409);
  assert.equal((await a.call('GET', '/api/admin/reports')).status, 403);
  const reps = await mod.call('GET', '/api/admin/reports');
  assert.equal(reps.body.reports.length, 1);
  assert.equal((await mod.call('PATCH', `/api/admin/reports/${reps.body.reports[0].id}`, { status: 'actioned' })).status, 200);

  // Ban revokes sessions; moderators cannot ban admins.
  assert.equal((await mod.call('POST', '/api/admin/ban', { userId: b.id, hours: 24, reason: 'harassment' })).status, 200);
  assert.equal((await b.call('GET', '/api/me')).status, 401);
  const admin = await login(h, 'TopAdmin');
  grantRole(h, admin.id, 'admin');
  assert.equal((await mod.call('POST', '/api/admin/ban', { userId: admin.id, hours: 1, reason: 'nope' })).status, 403);
});

test('feedback: attachments validated by content, visibility, voting, staff status', async (t) => {
  const h = await start();
  t.after(() => h.close());
  const a = await login(h, 'Tester1');
  const b = await login(h, 'Tester2');
  const staff = await login(h, 'Support');
  grantRole(h, staff.id, 'support');

  const fake = await a.call('POST', '/api/feedback', {
    kind: 'bug', title: 'Crash on start', body: 'Game crashes when I open the menu',
    attachments: [{ filename: 'virus.exe', data: Buffer.from('MZ\u0000\u0000binary').toString('base64') }],
  });
  assert.equal(fake.status, 400);

  const bug = await a.call('POST', '/api/feedback', {
    kind: 'bug', title: 'Crash on start', body: 'Game crashes when I open the menu',
    attachments: [{ filename: 'shot.png', data: PNG.toString('base64') }, { filename: 'latest', data: Buffer.from('[12:00] crash').toString('base64') }],
  });
  assert.equal(bug.status, 200);
  const idea = await a.call('POST', '/api/feedback', { kind: 'minigame', title: 'Mini golf', body: 'Please add a mini golf mode' });

  // Bug tickets are private, suggestions public.
  assert.equal((await b.call('GET', `/api/feedback/${bug.body.id}`)).status, 404);
  assert.equal((await b.call('GET', `/api/feedback/${idea.body.id}`)).status, 200);
  const own = await a.call('GET', `/api/feedback/${bug.body.id}`);
  assert.equal(own.body.attachments.length, 2);
  assert.deepEqual(own.body.attachments.map((x: any) => x.mime).sort(), ['image/png', 'text/plain']);
  const dl = await fetch(`${h.base}/api/feedback/attachments/${own.body.attachments.find((x: any) => x.mime === 'text/plain').id}`, { headers: { Authorization: `Bearer ${a.token}` } });
  assert.equal(dl.status, 200);
  assert.match(dl.headers.get('content-disposition') ?? '', /attachment/);
  assert.equal(await dl.text(), '[12:00] crash');

  await b.call('POST', `/api/feedback/${idea.body.id}/vote`, { up: true });
  await b.call('POST', `/api/feedback/${idea.body.id}/vote`, { up: true });
  assert.equal((await a.call('GET', '/api/feedback?sort=votes')).body.items.find((x: any) => x.id === idea.body.id).votes, 1, 'one vote per user');

  assert.equal((await b.call('PATCH', `/api/feedback/${bug.body.id}/status`, { status: 'planned' })).status, 403);
  assert.equal((await staff.call('PATCH', `/api/feedback/${bug.body.id}/status`, { status: 'in_progress' })).status, 200);
  assert.equal((await staff.call('GET', '/api/feedback?status=in_progress')).body.items.length, 1);
});

test('public profiles and leaderboards honour privacy', async (t) => {
  const h = await start();
  t.after(() => h.close());
  const a = await login(h, 'Public');
  const b = await login(h, 'Private');
  await b.call('PUT', '/api/me/privacy', { profile: 'nobody', status: 'nobody', activity: 'nobody', messages: 'nobody', friendRequests: false });
  await a.call('POST', '/api/me/game-results', { gameId: 'tennis', score: 7, won: true });
  await b.call('POST', '/api/me/game-results', { gameId: 'tennis', score: 9, won: true });

  assert.equal((await call(h, 'GET', '/api/public/profiles/Public')).status, 200);
  assert.equal((await call(h, 'GET', '/api/public/profiles/Private')).status, 404);
  assert.equal((await b.call('GET', '/api/public/profiles/Private')).status, 200, 'owner can see own profile');
  const board = await call(h, 'GET', '/api/public/leaderboard/tennis');
  assert.deepEqual(board.body.entries.map((e: any) => e.name), ['Public']);
  assert.equal((await a.call('POST', '/api/friends/requests', { player: 'Private' })).status, 403);
});
