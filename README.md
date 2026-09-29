# Online Voting System

A full-stack portfolio project: **Angular (standalone, SPA)** frontend + **Spring Boot 3 / Java 21** REST API
secured with **JWT**, backed by **MySQL**.

---

## 1. The login bug you had — root cause & fix

Your original backend used Spring Security's `formLogin()` **without** `.loginPage("/login")`.
Without that, Spring Security auto-generates its own login page for `GET /login` via a filter that
runs *before* your `@Controller` — so your custom `login.html` was never actually shown. If you'd
added `.loginPage("/login")` to fix that, it would have hit `LoginController`, which returned the
view name `"login"` — but there was no Thymeleaf/JSP dependency in `pom.xml` and no view resolver,
so Spring forwarded the request back to `/login` itself → **"Circular view path [login]"** error.

**Fix:** the backend is now a stateless JWT REST API (matching the Angular + Spring Boot spec you
described) — there is no server-rendered login page at all anymore. Angular calls
`POST /api/auth/login` and gets a JWT back. See the comment block at the top of
`voting/voting/src/main/java/com/votingapp/voting/config/SecurityConfig.java` for the full explanation.

---

## 2. What's included / added

- Full JWT authentication: register → OTP verify (email **and** mobile) → login → (optional) login OTP → JWT.
- Generic OTP engine (`OtpVerification` + `OtpType`) covering `EMAIL_VERIFICATION`, `MOBILE_VERIFICATION`,
  `LOGIN_OTP`, `PASSWORD_RESET`. Only a BCrypt **hash** of the OTP is stored, never the raw code.
  **Dev mode** (`app.otp.dev-mode=true`) prints OTPs to the backend console log instead of sending
  real email/SMS — watch the console when testing.
- Election lifecycle: `DRAFT → UPCOMING → ACTIVE → CLOSED → RESULT_DECLARED`, plus `CANCELLED`.
  A scheduled job (`ElectionSchedulerService`, runs every 60s) auto-transitions UPCOMING→ACTIVE→CLOSED
  based on start/end time; DRAFT/CANCELLED/RESULT_DECLARED only change via explicit admin action.
- Candidate management, restricted to DRAFT/UPCOMING elections to protect ballot integrity.
- **Privacy-oriented voting design** (see section 4 below).
- One-vote-per-election enforced at both the application layer and a DB unique constraint.
- Centralized audit log (`audit_logs`) + per-login `login_logs` (IP, browser, OS, success/failure —
  never overwritten, one row per login).
- Global exception handling (`@RestControllerAdvice`) → consistent `{success, message, timestamp}` JSON.
- Admin dashboard stats, user search/block/unblock, paginated login/audit log viewers.
- Angular SPA (JWT interceptor, route guards, reactive forms) covering the voter and admin flows.

## 3. Known, deliberate scope simplifications

To keep this buildable and testable as a portfolio project:
- **Per-election voter eligibility whitelist was simplified**: any `ACTIVE` (fully verified, unblocked)
  user can vote in any election, rather than maintaining a separate `election_voters` allow-list per
  election. The `voter_participation` table is still there and still enforces one-vote-per-election —
  extending it to a real eligibility whitelist is a natural next step (add a `VoterEligibility` table
  and check it in `VoteService.castVote`).
- Roles are a single `role` column on `users` (`ADMIN`/`VOTER`) rather than a `roles`/`user_roles`
  many-to-many join — enough for two roles, easy to extend later.
- Real SMS/Email sending is not wired up (no third-party account to configure) — dev-mode console
  logging stands in for it, exactly as the original spec asked for.

## 4. Vote privacy design (as requested)

Two tables instead of one:
- `voter_participation (user_id, election_id, has_voted, voted_at)` — tracks **who** voted.
- `votes (election_id, candidate_id, vote_reference, voted_at)` — tracks **what** was voted, with
  **no** `user_id` column at all.

A simple `votes(user_id, election_id, candidate_id)` design is easier to build but every row
directly says "this user voted for this candidate" — anyone with DB access (or a future bug in an
admin screen) can deanonymize every ballot. Splitting the two tables means even a full DB dump
never links a specific voter to a specific candidate choice — joining the tables only tells you
vote *counts*, not *who cast which one*. The trade-off: it's slightly more complex (two writes in
one transaction instead of one) and you lose the ability to ask "who did user X vote for" even for
legitimate admin/audit purposes — which, for a real election, is exactly the point.

## 5. Project structure

```
Voting_Full/
├── voting/voting/          Spring Boot backend (Java 21, Maven)
├── voting-frontend/        Angular 21 SPA
├── voting_db.sql           Full MySQL schema (also auto-created by Hibernate on first run)
└── README.md               This file
```

## 6. Running it

### Backend
```
cd voting/voting
mvn spring-boot:run
```
- Requires Java 21 and Maven (Maven wrapper `./mvnw` also included).
- Create the MySQL database first: `mysql -u root -p < ../../voting_db.sql`
  (or just let `spring.jpa.hibernate.ddl-auto=update` create it on first run).
- Update `src/main/resources/application.properties` with your MySQL username/password if different
  from `root` / `MySql@123`.
- On first run, a default admin account is seeded automatically:
  **admin@votingapp.com / Admin@123** (change via `app.admin.*` properties, or just log in and
  change the password). Watch the console for OTPs (dev mode).
- API runs on `http://localhost:8089/api/...`

### Frontend
```
cd voting-frontend
npm install
npm start
```
- Runs on `http://localhost:4200`, already pointed at `http://localhost:8089/api`
  (see `src/app/core/config/api.config.ts` if you deploy the backend elsewhere).
- `node_modules` is **not** included in this zip to keep it small — run `npm install` first.

## 7. Testing the full flow

1. Start MySQL, then the backend, then the frontend.
2. Register a new voter → watch the backend console for two OTPs (email + mobile) → verify both on
   the `/auth/verify-otp` screen → log in.
3. Log in as `admin@votingapp.com` / `Admin@123` (in a separate browser/incognito window, since the
   JWT is stored per-browser) → create an election → add at least 2 candidates → Publish it.
4. Set the election's start time a minute or two in the future — the scheduler will flip it to
   `ACTIVE` automatically once that time passes (check again after ~1 minute, or set the start time
   to "now").
5. As the voter, open the election and cast a vote → note the vote reference shown.
6. Try voting again in the same election — you'll get "You have already voted in this election."
7. As admin, close/cancel or wait for the election to auto-close, then Declare Results.

## 8. API reference

See the full list of endpoints in the original spec — they're implemented as described, under
`/api/auth/*`, `/api/users/*`, `/api/elections/*`, `/api/admin/*`. Controllers are one file each
under `voting/voting/src/main/java/com/votingapp/voting/controller/` if you want the exact request/
response shapes.
