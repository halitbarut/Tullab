# Specification Quality Checklist: Lesson Rate Locking and Historical Payment Calculation

**Purpose**: Validate specification completeness and quality before proceeding to planning
**Created**: 2026-09-04
**Feature**: [spec.md](../spec.md)

## Content Quality

- [x] No implementation details (languages, frameworks, APIs)
- [x] Focused on user value and business needs
- [x] Written for non-technical stakeholders
- [x] All mandatory sections completed

## Requirement Completeness

- [x] No [NEEDS CLARIFICATION] markers remain
- [x] Requirements are testable and unambiguous
- [x] Success criteria are measurable
- [x] Success criteria are technology-agnostic (no implementation details)
- [x] All acceptance scenarios are defined
- [x] Edge cases are identified
- [x] Scope is clearly bounded
- [x] Dependencies and assumptions identified

## Feature Readiness

- [x] All functional requirements have clear acceptance criteria
- [x] User scenarios cover primary flows
- [x] Feature meets measurable outcomes defined in Success Criteria
- [x] No implementation details leak into specification

## Notes

- Core domain rules formally encoded:
  1. Completed lessons maintain their own pricing snapshot and are never automatically altered by student profile rate changes.
  2. "Locked" protects against automatic cascading changes, but permits explicit manual editing of an unpaid completed lesson's pricing.
  3. Scheduled future lessons prompt tutor whether to adopt new profile rate; completed and paid lessons are never affected.
  4. Dual pricing modes supported: Per Hour (`duration × hourlyRate`) and Flat Fee (fixed amount).
  5. Avoid storing redundant `lessonValue` — deterministically computed on demand from pricing mode, duration, and rate/fee.
  6. Legacy lessons migrated to Per-Hour mode using student's effective rate at migration time.
  7. Student profile rate acts strictly as default for new lessons; all debt/payment calculations use lesson snapshots.
- Specification is complete and ready for planning (`/speckit-plan`).
