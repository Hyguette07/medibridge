import { describe, expect, it } from "vitest";
import { auditActionLabel, clinicianMayReadNotes, DISCLAIMER } from "./privacy";

describe("PrivacyGuard mirror", () => {
  it("denies notes without consent and allows with consent", () => {
    expect(clinicianMayReadNotes(false)).toBe(false);
    expect(clinicianMayReadNotes(true)).toBe(true);
  });

  it("keeps the educational disclaimer explicit", () => {
    expect(DISCLAIMER.toLowerCase()).toContain("not a medical device");
    expect(DISCLAIMER.toLowerCase()).toContain("not clinical advice");
  });

  it("labels access-timeline actions for patients", () => {
    expect(auditActionLabel("VIEW_NOTES")).toBe("Viewed care notes");
    expect(auditActionLabel("CONSENT_GRANT")).toBe("Granted consent");
  });
});
