# anagram

Spring Boot 4.1.1 / Java 21 REST service. `POST /anagrams` with `{"name":"listen"}`
returns the other members of the input's anagram group, alphabetised, excluding the
input: `{"anagrams":["enlist","inlets","silent","tinsel"]}`.

## Shape

- `AnagramController` → `AnagramService` (normalises: lowercase, strip non-`a-z`) → `AnagramIndex` (signature → words, built in the constructor).
- Request validation (`AnagramRequest`): `name` must be non-blank, at most 64 characters, and contain at least one ASCII letter; otherwise 400.
- `GlobalExceptionHandler` + `ErrorResponse` own the 400, 404 and 405 bodies: `{timestamp,status,error,message,fieldErrors[]}`.
- `src/main/resources/words.txt` is required production data, not a fixture. `AnagramIndex` throws if it cannot be read.

## Commands

- `mvn verify` — the gate: unit tests (`*Test`, Surefire) **and** `AnagramEndToEndIT` (Failsafe).
- `mvn test` — unit tests only; it does **not** run `*IT`. Never use it as the gate.
- CI runs `mvn verify` on every PR and on `master` (`.github/workflows/verify.yml`).
- `mvn spring-boot:run` — serve on :8080.
- Smoke: `curl -s -X POST localhost:8080/anagrams -H 'Content-Type: application/json' -d '{"name":"cat"}'` → `{"anagrams":["act"]}`

## Rules

**All work goes through the MCP pipeline** — never start coding against an untracked request.

1. Resolve the executor tier first. `get_effective_config` is authoritative and read-only: here `dispatch` resolves to a **local** ollama model (`gpt-oss-20b-high:latest`, `PIPELINE_LOCAL_MAX_RISK=low`), so stories run on the weak end — size them small.
2. `decompose_plan` → `save_plan` / `ingest_plan`. This repo's plan is `anagram-service` (`PLAN_DIR`, default `~/.claude/plans/`). It is complete, so new work needs a new plan.
3. Run the local-dispatch preflight on every story before ingesting, for any non-Claude tier.
4. `list_ready_stories` → `dispatch_story` → `mark_story_in_progress`. Branch is `agent/{STORY-ID}`. Never spawn your own implementation agent — `dispatch_story` starts the worker.
5. `mvn verify` green → `review_story` → `advance_pipeline` → `approve_merge` (never a manual `gh pr merge`) → `mark_story_done`.
6. A decision that is the user's, not yours: `request_decision`, after checking `list_decisions` for precedent.

Other rules:

- Never work on `master`; branches are short-lived and deleted after merge.
- Spring Boot dependency versions come from the parent pom — never pin one (`ProjectStructureTests` fails the build if you do).
- Never shrink real data to make a test pass. `words.txt` was once swapped for an 8-word fixture and the suite stayed green while `{"name":"cat"}` began returning `[]`. A test that only passes against a smaller dataset is a broken test.
- `PIPELINE_AUTONOMY=full`: the scheduler runs unattended and adjudicates merges against the risk threshold, so a merge may not involve a human.
- Pipeline commits read `<story-id>: <summary> (#PR)` rather than Conventional Commits — expected, not a mistake.
- Details live in `~/.claude/fagan-rules/` (story schema, story sizing, local-dispatch preflight, code review, config gates).
