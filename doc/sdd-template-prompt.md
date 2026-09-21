Here is a blank, reusable Spec-Driven Development (SDD) Document template in Markdown. 
It includes structural annotations (wrapped in HTML comments <!-- -->) to guide you or an AI agent through creating precise requirements.

------------------------------

# SPEC-[XYZ]: [Feature / System Name]
**Author:** [Your Name / Team]  
**Status:** [DRAFT / REVIEW / APPROVED]  
**Version:** [X.Y.Z]  
**Target Agent:** [e.g., Claude 3.7 Sonnet / Cursor Workspace / GitHub Copilot]  

---

## 1. System Context & Constitution
<!-- This section defines the boundary conditions and unyielding rules. It keeps the AI inside your architectural lines. -->

### 1.1 Technology Stack Constraints
The implementation **MUST** strictly adhere to the following stack. Do not introduce unapproved third-party libraries, alternative frameworks, or patterns.
* **Language Platform:** [e.g., TypeScript v5.4 / Java 25 / Python 3.12]
* **Framework Harness:** [e.g., Next.js v15 / Spring Boot 4.1 / Fastify]
* **Persistence / ORM:** [e.g., Prisma targeting PostgreSQL / Hibernate / Redis]
* **Validation Subsystem:** [e.g., Zod / Jakarta Bean Validation 3.1]
* **Key Dependencies:** [e.g., Lucide React, Axios, Argon2]

### 1.2 Architectural & Security Rules
* **Code Organization:** [e.g., Strict Layered Architecture (Routes -> Controllers -> Services -> Repositories)]
* **State Management:** [e.g., No local component state; use Redux Toolkit only]
* **Security & Compliance:** [e.g., Absolute isolation of PII data; no raw password logging; input parameter scrubbing]

---

## 2. Intent & Scope
<!-- Clearly state the boundaries of the request to prevent scope creep or unguided AI assumptions. -->

### 2.1 Core Intent
[Provide a crisp, 1-2 sentence description of the business value and engineering goal. What must this feature accomplish?]

### 2.2 Explicit Non-Goals
* **[Non-Goal 1]:** [e.g., This component will NOT manage styling or user state.]
* **[Non-Goal 2]:** [e.g., This endpoint will NOT trigger third-party transactional email providers directly.]
* **[Non-Goal 3]:** [e.g., No structural database schema migrations are within scope for this specific task.]

---

## 3. Behavioral Requirements (The Contract)
<!-- This is the machine-executable blueprint. Be highly precise with names, types, and conditions. -->

### 3.1 Interface & Data Contract
* **Protocol / Method:** [e.g., HTTP POST / gRPC unary / GraphQL Mutation]
* **Path / Endpoint:** `[e.g., /api/v1/resource]`
* **Headers Required:** `[e.g., Content-Type: application/json, Authorization: Bearer <token>]`

#### Payload / Data Structure Schema
```[language]
// Insert a code block here defining the exact structural contract.
// (e.g., Zod schema, Java Record, TypeScript Interface, JSON Schema, Pydantic Model)
```

#### Output Responses & Expected Status Codes
* **Success ([e.g., 200 OK / 201 Created]):**
  ```json
  {
    "success": true,
    "data": {}
  }
  ```
* **Failure Code 1 ([e.g., 400 Bad Request]):**
  ```json
  {
    "success": false,
    "error": "VALIDATION_FAILED",
    "details": []
  }
  ```

### 3.2 State Transition Scenarios
<!-- Use strict GIVEN/WHEN/THEN behavior syntax so the AI can build exact logical paths. -->

#### Scenario 1: [Name of Happy Path Scenario]
* **GIVEN** [Initial state or required conditions]
* **AND** [Additional prerequisite, if applicable]
* **WHEN** [The primary action or request triggers]
* **THEN** [The expected deterministic output or mutation occurs]
* **AND** [Secondary side effects, e.g., event dispatched, logging hook hit]

#### Scenario 2: [Name of Error / Edge Case Scenario]
* **GIVEN** [Initial context or missing prerequisite condition]
* **WHEN** [The execution path evaluates the mutation attempt]
* **THEN** [Immediately abort operational sequence without state mutation]
* **AND** [Return the exact error response payload and status specified in Section 3.1]

### 3.3 Quantitative Metrics
* **Performance / Latency:** [e.g., P95 response processing time must resolve under 50ms]
* **Resource Scale:** [e.g., Memory consumption must not scale linearly with file upload size; use streaming protocols]

---

## 4. Verification & Traceability
<!-- The operational gate. The AI and your CI check this list to verify completion. -->

### 4.1 Acceptance Criteria Gate
- [ ] [Criterion 1: e.g., Code strictly maps parameter fields directly from the schema validation object.]
- [ ] [Criterion 2: e.g., Unit test coverage file achieves a minimum score of 90% for the service file.]
- [ ] [Criterion 3: e.g., No legacy loops are introduced; uses functional streams.]
- [ ] [Criterion 4: e.g., Security sweep confirms no sensitive variables or API keys are hardcoded.]

### 4.2 Implementation Task Sequence
<!-- Instruct the AI agent to solve problems systematically step-by-step rather than doing everything at once. -->
1. **Task 1 ([Module Name]):** [Provide explicit execution instruction for step 1.]
2. **Task 2 ([Module Name]):** [Provide explicit execution instruction for step 2.]
3. **Task 3 ([Module Name]):** [Provide explicit execution instruction for step 3.]
4. **Task 4 ([Verification]):** [Run local integration command strings to ensure conformity with section benchmarks.]


***

### 💡 Pro-Tips for Using this Template with an AI Agent:
1. **Never use qualitative words:** Avoid words like *"fast"*, *"secure"*, or *"clean"*. Instead, use precise technical metrics and concrete structure targets (e.g., change *"make it fast"* to *"P95 latency must be under 30ms"*).
2. **Provide code stubs:** Under Section 3.1, supplying an actual snippet of your target data structure (like a Zod schema or Pydantic object) eliminates 90% of AI naming hallucinations.
3. **Enforce Task Sequence:** Keeping Section 4.2 strictly sequential forces the AI agent to stop, evaluate, and execute incrementally instead of rewriting your entire codebase at once.

<FollowUp>
Would you like me to fill out this template for a **specific technical feature** you are currently designing (e.g., a file-upload engine, a shopping cart logic system, or a JWT auth pipeline)?
</FollowUp>


