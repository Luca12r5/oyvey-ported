export const FEEDBACK_KINDS = ['bug', 'suggestion', 'minigame', 'cosmetic', 'theme', 'performance', 'support'] as const;
export type FeedbackKind = (typeof FEEDBACK_KINDS)[number];
export const FEEDBACK_STATUSES = ['new', 'reviewing', 'planned', 'in_progress', 'done', 'rejected'] as const;
export type FeedbackStatus = (typeof FEEDBACK_STATUSES)[number];
export const REPORT_REASONS = ['harassment', 'cheating', 'impersonation', 'inappropriate_name', 'spam', 'other'] as const;
export type ReportReason = (typeof REPORT_REASONS)[number];
