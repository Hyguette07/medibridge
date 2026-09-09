export const DISCLAIMER =
  "This is educational software — not a medical device, not a doctor, and not clinical advice.";

export function clinicianMayReadNotes(activeConsent: boolean): boolean {
  return activeConsent;
}

export function auditActionLabel(action: string): string {
  const labels: Record<string, string> = {
    VIEW_PATIENT: "Viewed coordination profile",
    VIEW_NOTES: "Viewed care notes",
    WRITE_NOTE: "Wrote a care note",
    VIEW_AUDIT: "Viewed access timeline",
    VIEW_OWN_AUDIT: "Opened own access timeline",
    BOOK_APPOINTMENT: "Booked a visit",
    CANCEL_APPOINTMENT: "Cancelled a visit",
    CONSENT_GRANT: "Granted consent",
    CONSENT_REVOKE: "Revoked consent",
    LOGIN: "Signed in",
    LOGOUT: "Signed out",
    REGISTER: "Created an account",
  };
  return labels[action] ?? action.replaceAll("_", " ").toLowerCase();
}

export function formatWhen(iso: string): string {
  try {
    return new Intl.DateTimeFormat(undefined, {
      dateStyle: "medium",
      timeStyle: "short",
    }).format(new Date(iso));
  } catch {
    return iso;
  }
}
