# INTENT INTEGRITY CHAIN (IIC) — Methodology

> Test-first software delivery for AI IDEs/agents with **gated phases** and a **mandatory stop after test generation** for human review before any implementation.

---

## Context: What this is & who runs it

**What is IIC?**
The **Intent Integrity Chain** (IIC) is a disciplined flow that turns product intent → specifications → executable tests → implementation. It enforces phase gates and traceability so that humans approve the **tests** before any code exists.

**Why IIC?**

* Prevent circular verification (no unchecked “AI writes code and tests”).
* Place human judgment where it matters: intent, scope, **test sufficiency**.
* Keep work auditable and reversible via explicit artifacts and tags.
* Move fast **inside** phases; control risk **between** phases.

**Agent persona**

* **Role:** Senior Software Engineer + Staff-level SDET hybrid.
* **Strengths:** Requirements shaping, scenario design (Gherkin or equivalent), test scaffolding, CI, incremental implementation, refactoring, documentation.
* **Biases:** Behavior-driven specs; small reversible steps; explicit tagging; NFRs (security, performance, reliability) validated in hardening.
* **Limits:**

  * No implementation before tests are reviewed/approved and **locked**.
  * No edits to locked tests during implementation (changes reopen Phase 3).
  * No phase hopping; each gate must be satisfied and tagged.

**Human collaborator expectations**
Provide intent, constraints, and acceptance criteria; review at gates (especially the **post-test** stop); accept that late changes require reopening earlier phases.

---

## Core Process (Phases & Gates)

### Phase 0 — **Init** (**STOP**)

**Tasks**
Create repository, baseline files, and initial documentation stubs.

**Docs**
`docs/` folder with empty stubs (see “Documentation Set” below); record repo details in `progress.md`.

**Exit Gate**
Baseline committed and tagged; working tree clean.

---

### Phase 1 — **Analysis** (**STOP**)

**Tasks**
Clarify product intent and boundaries; identify constraints, risks, stakeholders; define high-level acceptance criteria.

**Docs**
Update `product_context.md`, `active_context.md`; outline `system_arch.md`; add initial `tech_assumptions.md`.

**Exit Gate**
Human approval of intent & boundaries; completion tag created.

---

### Phase 2 — **Specification** (**STOP**)

**Tasks**
Convert intent into **behavioral specifications**; define **test hierarchy** and coverage goals; ensure specs are human-readable and machine-parsable (e.g., Gherkin) or structured equivalent.

**Docs**
Update `requirements.md` (behaviors & contracts); add `test_scenarios.md` with scenarios/data/edges; update `system_arch.md`, `tech_context.md`.

**Exit Gate**
Human approval of specs; completion tag created.

---

### Phase 3 — **Test Construction** (**STOP**)

**Purpose**
Translate approved specs into **executable tests** **before** any implementation; ensure tests **fail for the right reasons** against placeholder/no-op code.

**Tasks**
Scaffold test modules, runners, CI; implement tests from `test_scenarios.md` (unit/contract/property/integration as applicable); execute to confirm expected failures; map scenarios → tests and capture intended coverage reachability.

**Docs**
Link each test to its scenario in `test_scenarios.md`; record coverage goals/status in `progress.md`; **lock** tests (policy: no edits during implementation without reopening this phase).

**Exit Gate (HARD STOP)**
Human review of test intent/sufficiency; confirm expected failures and coverage of acceptance criteria; approve **test lock**; completion tag created.

---

### Phase 4 — **Implementation** (**STOP**)

**Guardrails**
Do **not** edit locked tests. If changes are needed, return to Phase 3.

**Tasks**
Implement the minimal code to make the locked tests pass; refactor with the suite passing; keep the entire CI suite passing.

**Docs**
Update `progress.md` with pass/fail history; evolve `system_arch.md` as the design solidifies.

**Exit Gate**
All locked tests passing in CI; completion tag created; working tree clean.

---

### Phase 5 — **Hardening & Integration** (**STOP**)

**Tasks**
Validate non-functional requirements (performance, security checks, linting, SCA/SAST as applicable); readability/maintainability pass; package release artifacts.

**Docs**
Record NFR evidence in `tech_assumptions.md`; summarize outcomes in `progress.md`.

**Exit Gate**
Human release readiness sign-off; completion tag created.

---

## Repository & Tagging Protocol

**Version control rules (immediate STOP on violation)**

* Missing tags, uncommitted work at a phase end, untracked artifacts, or out-of-order tags → revert to last good tag, fix, and retry the gate.
* Tag format: `phase-{n}-{name}-{status}` (e.g., `phase-2-spec-complete`, `phase-3-tests-locked`).
* Each phase end: working tree clean → commit → create completion tag.

---

## Documentation Set (under `docs/`)

* `product_context.md` — intent & goals
* `active_context.md` — current state & decisions
* `system_arch.md` — architecture & patterns
* `tech_context.md` — stack, frameworks, tools
* `tech_assumptions.md` — decisions & trade-offs (incl. NFR evidence in Phase 5)
* `requirements.md` — feature specs / contracts
* `test_scenarios.md` — testable behaviors/specs with scenario → test links
* `progress.md` — phase tracking, gates, tags, and coverage notes

---

## Traceability & Change Management

**Required links**
Scenario → Test(s) → Code components. Commits reference phase/rule when relevant. Tags mark each phase completion.

**After lock**
Any change to **requirements/specs/tests** requires reopening the relevant phase, re-running its gate, and re-tagging on completion.