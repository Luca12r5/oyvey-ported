// Role-based permissions. Checked server-side on every admin/dev endpoint.

export const ROLES = ['admin', 'developer', 'moderator', 'support'] as const;
export type Role = (typeof ROLES)[number];

export const PERMISSIONS = [
  'users.view', 'users.ban', 'roles.manage', 'credits.grant', 'credits.deduct', 'feedback.manage',
  'reports.manage', 'news.manage', 'audit.view', 'promo.manage', 'system.backup',
] as const;
export type Permission = (typeof PERMISSIONS)[number];

export const ROLE_PERMISSIONS: Record<Role, readonly Permission[]> = {
  admin: PERMISSIONS,
  developer: ['users.view', 'credits.grant', 'feedback.manage', 'news.manage', 'audit.view', 'promo.manage'],
  moderator: ['users.view', 'users.ban', 'reports.manage', 'feedback.manage'],
  support: ['users.view', 'feedback.manage'],
};

export function hasPermission(roles: readonly string[], p: Permission): boolean {
  return roles.some((r) => (ROLE_PERMISSIONS as Record<string, readonly Permission[]>)[r]?.includes(p) ?? false);
}

/** Credit changes with an absolute value at or above this need explicit confirmation. */
export const LARGE_CREDIT_CHANGE = 5000;
/** Hard cap per single admin credit change. */
export const MAX_CREDIT_CHANGE = 1_000_000;
