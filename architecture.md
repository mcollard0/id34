# ID34 Android Application Architecture

## Overview
ID34 is an Android application for managing ideas organized by categories. It uses a local SQLite database with server synchronization capabilities and Google Cloud Messaging (GCM) for push notifications.

## Database Schema

### Current Database: `id34_id34` (Version 2)

#### Table: `tblCategory`
```sql
CREATE TABLE `tblCategory` (
  `id` unsigned int(8) PRIMARY KEY NOT NULL,
  `uid` unsigned int(8) NOT NULL,
  `updated` timestamp NOT NULL default '1970-01-01 06:00:00',
  `num` unsigned int(8) NOT NULL,
  `cat` varchar(57) NOT NULL
);
```
**Purpose**: Stores idea categories with hierarchical organization support

#### Table: `tblIdea` 
```sql
CREATE TABLE `tblIdea` (
  `id` unsigned int(8) PRIMARY KEY NOT NULL,
  `uid` unsigned int(8) NOT NULL,
  `name` varchar(255) NOT NULL,
  `created` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `updated` timestamp NOT NULL default '1970-01-01 06:00:00',
  `reminder` timestamp NOT NULL default '1970-01-01 06:00:00',
  `num` unsigned int(8) NOT NULL,
  `cid0` unsigned int(8) NOT NULL,
  `cid1` unsigned int(8) NOT NULL,
  `cid2` unsigned int(8) NOT NULL,
  `cid3` unsigned int(8) NOT NULL,
  `cid4` unsigned int(8) NOT NULL,
  `deleted` tinyint(1) NOT NULL default '0',
  `completed` tinyint(1) NOT NULL default '0'
);

-- Index for category lookups
CREATE INDEX cid0 ON tblIdea (`cid0`,`cid1`,`cid2`,`cid3`,`cid4`);
```
**Purpose**: Stores individual ideas with multi-level category associations (cid0-cid4), completion status, and soft deletion

#### Table: `tblResponses` 
```sql
CREATE TABLE `tblResponses` (
  `msg` varchar(25) NOT NULL
);
```
**Purpose**: Stores server response messages (currently minimal implementation)

#### Temporary Table: `search`
```sql
CREATE TEMP TABLE search AS SELECT * FROM tblIdea;
```
**Purpose**: Runtime search optimization table

## API Endpoints & Server Communication

### Base Server Communication
- **Server**: `https://id34.info/converse.php` ✅ **SECURE HTTPS**
- **Authentication**: Phone number based (`mPhoneNumber`)
- **Session Management**: Base URL with embedded session parameters

### Key API Operations

#### Data Synchronization
```http
GET /converse.php?aa=alcoholics&From={mPhoneNumber}&Body=hh&syncdate={timestamp}
```
**Purpose**: Sync categories from server

```http
GET /converse.php?aa=alcoholics&From={mPhoneNumber}&Body=Einstein=plus&syncdate={timestamp}
```
**Purpose**: Sync ideas from server

#### Server Operations
```http
GET /converse.php?aa=alcoholics&From={mPhoneNumber}&Body=!getdatetime
```
**Purpose**: Get server timestamp

```http
GET /converse.php?Body=!complete&id={id}
```
**Purpose**: Toggle idea completion status

```http
GET /converse.php?Body=!delete&id={id}
```
**Purpose**: Toggle idea deletion status

### Critical Security Issues ⚠️
1. **All communication over HTTP** - data transmitted in plain text
2. **Phone numbers in URL parameters** - logged in server access logs
3. **No request authentication** beyond phone number
4. **No input sanitization** on server requests

## Application Architecture

### Core Components

#### Activities
- **`Splash`**: Application entry point
- **`IdeaListActivity`**: Main activity displaying idea categories
- **`IdeaDetailActivity`**: View/edit individual ideas
- **`IdeaAddActivity`**: Create new ideas with Twitter integration

#### Services
- **`GCMIntentService`**: Handle Google Cloud Messaging notifications
- **`ServerInteractionService`**: Background server synchronization

#### Data Layer
- **`SQLiteAdapter`**: Primary database interface class
- **`Iserver`**: Server communication layer

### Data Flow
1. **Local-First Architecture**: SQLite database as primary data store
2. **Background Sync**: Server synchronization via HTTP API calls
3. **Push Notifications**: GCM integration for real-time updates
4. **Offline Capability**: Full functionality without network connection

## Key Business Logic Rules

### Category System
- **Hierarchical Categories**: Ideas can belong to up to 5 category levels (cid0-cid4)
- **Category Resolution**: Categories resolved by name to ID for database storage
- **Auto-Complete**: Category suggestions from existing database entries

### Idea Management
- **Soft Deletion**: Ideas marked as deleted=1, not physically removed
- **Completion Tracking**: Boolean completion status for task management
- **Reminder System**: Timestamp-based reminder functionality
- **Server Sync**: Ideas synchronized bidirectionally with remote server

### User Identification
- **Phone Number Based**: Users identified by phone number (`mPhoneNumber`)
- **GCM Integration**: Push notification targeting via GCM registration ID
- **Multi-User Support**: User ID (`uid`) field in all records

## Current Feature Status

### ✅ Implemented Features
- Local SQLite database with full CRUD operations
- Category-based idea organization with hierarchical support  
- Server synchronization for categories and ideas
- Google Cloud Messaging integration
- Offline functionality with local data persistence
- Idea completion and deletion status tracking
- Auto-complete category suggestions

### ⚠️ Partially Implemented
- **Twitter Integration**: OAuth setup present but incomplete
- **Search Functionality**: Temp search table created but limited implementation
- **Reminder System**: Database fields present, UI integration unclear

### ❌ Missing/Planned Features
- Modern Android architecture (Room, ViewModel, LiveData)
- HTTPS secure communication
- SQLite database encryption
- Data compression for large text fields
- Comprehensive error handling and offline sync conflict resolution

## Known Issues & Technical Constraints

### Critical Security Vulnerabilities
1. **SQL Injection** in `SQLiteAdapter.queryIdeasByCatName()` lines 328-333 (RESOLVED 2025-09-13) ✅
2. **HTTP Communication** - all data transmitted unencrypted (RESOLVED 2025-09-13) ✅
3. **Hardcoded Credentials** - Twitter API keys embedded in source (RESOLVED 2025-09-13) ✅
4. **Debug Logging** - sensitive data in system logs

### Technical Debt
- **Deprecated APIs**: Targeting Android API 15, minimum API 11
- **Legacy HTTP Client**: Using deprecated Apache HttpClient
- **Legacy GCM**: Google Cloud Messaging deprecated, needs Firebase migration
- **No Architecture Components**: Missing modern Android patterns

### Performance Constraints  
- **No Database Optimization**: Missing VACUUM, index optimization
- **Synchronous Network Calls**: Blocking UI thread potential
- **Large Text Storage**: No compression for idea content
- **Memory Management**: Potential cursor leaks in database operations

## Database Migration History

### Version 1 → Version 2
- Added `tblResponses` table
- Added composite index on `tblIdea(cid0,cid1,cid2,cid3,cid4)`
- Migration strategy: **DROP ALL TABLES** and recreate ⚠️ **DATA LOSS**

### Planned Migrations
- **SQLCipher Integration**: Encrypt existing database
- **Schema Optimization**: Add proper foreign keys and constraints  
- **Compression Fields**: Add compressed content columns
- **Audit Trail**: Add created/modified by fields

## Dependencies & External Services

### Current Dependencies
- **SQLite**: Local database storage
- **Apache HttpClient**: HTTP communication (deprecated)
- **Google GCM**: Push notifications (deprecated)
- **JSON Processing**: org.json for API responses

### Planned Dependencies
- **SQLCipher**: Database encryption
- **OkHttp/Retrofit**: Modern HTTP client
- **Firebase Cloud Messaging**: Replace GCM
- **Android Architecture Components**: Room, ViewModel, LiveData, WorkManager

## Development Environment

### Current Build Configuration
- **Target SDK**: 15 (Android 4.0.3)
- **Min SDK**: 11 (Android 3.0)  
- **Compile SDK**: 16
- **Build Tools**: Legacy Android build system

### Required Modernization
- **Target SDK**: 34 (Android 14)
- **Min SDK**: 21 (Android 5.0) minimum for security features
- **Build Tools**: Modern Gradle build system
- **Kotlin Support**: Migrate from Java to Kotlin

---

**Last Updated**: 2025-09-13  
**Status**: Legacy application requiring comprehensive security and modernization updates  
**Priority**: Critical security fixes required before any production deployment