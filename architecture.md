# ID34 Java Android Rewrite Architecture
## Problem statement
This rewrite replaces the legacy category-based Android app with a Java Android local-first app that matches the TS implementation's schema and interaction model so SQLite backups are interchangeable.
## Current state
The active Android project is rebuilt around a single `MainActivity` with local SQLite persistence and TS-compatible schema semantics. Legacy app components (multi-activity category flows, GCM, and server coupling) are removed from the active `app/src/main` codepath.
## Database schema
The app now uses:
- `ideas(id TEXT PRIMARY KEY, content TEXT NOT NULL, created_at TEXT NOT NULL, updated_at TEXT NOT NULL, deleted INTEGER DEFAULT 0)`
- `ideas_fts` as `fts5(id, content)`
- `backups_registry(filename TEXT PRIMARY KEY, timestamp TEXT NOT NULL, size TEXT NOT NULL, idea_count INTEGER NOT NULL)`
`IdeasDatabaseHelper` creates this schema and performs one-time migration from legacy `tblIdea` when present and `ideas` is still empty:
- `name -> content`
- timestamp normalization to ISO-8601 UTC
- id generation as `idea_` + random alphanumeric substring
- `deleted` flag preserved
Categories are intentionally not migrated because the new model is flat.
## API endpoints exposed and used and their purposes
Local app uses Google Drive REST v3 helper calls (via OkHttp) for backup workflows:
- `GET /drive/v3/files` to discover `Id34` folder and list matching backups
- `POST /drive/v3/files` to create `Id34` folder if missing
- `POST /upload/drive/v3/files?uploadType=multipart` to upload `backup_cloud_{timestamp}.sqlite`
- `DELETE /drive/v3/files/{id}` to purge backups beyond retention
No legacy `id34.info/converse.php` endpoint is used in the rewrite.
## Key business logic rules
- Soft delete only (`deleted = 1`) for idea removal.
- FTS index is rebuilt after data writes using delete+insert strategy.
- Search behavior: FTS match first, fallback to `LIKE` when no FTS hits.
- Heatmap logic uses cleaned tokens with stopword filtering and computes heat buckets from 1-6.
- Backup registry records local backup metadata for UI listing.
- Backup retention is bounded to user-selected count (1-99).
## UI architecture and feature status
- Single-activity layout (`MainActivity`) with:
  - top search input + live result list
  - center heatmap word cloud rendered via wrapping `FlowLayout`
  - bottom idea input with 512-char counter and submit
  - right-side drawer with filtered ideas and inline edit/delete
  - backup dialog with retention selector and actions
- Material theme uses indigo primary (`#4F46E5`) aligned with TS styling.
## Auth and backup integration status
- Google Sign-In client (`drive.file` scope) is wired into `GoogleAuthHelper`.
- Drive backup helper (`DriveBackupHelper`) implements folder lookup/create, upload, list, and purge primitives.
- Main activity initializes sign-in and backup plumbing; final access-token exchange wiring is still required for end-to-end Drive operations.
## Known issues and constraints
- Local build validation currently depends on an available Android SDK path (`ANDROID_HOME` or `local.properties sdk.dir`).
- OAuth token acquisition for Drive helper execution is not yet fully finalized in UI callbacks.
- Legacy folder `2.0.OLD` is retained as archive material and backup source; active code is under root `app/`.
