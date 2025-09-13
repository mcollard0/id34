# ID34 Project Considerations & TODO Items

## Immediate Action Items

### Repository Management
- [ ] Push existing code to GitHub and new repo called `id34` (exact case) and ACE these contents there. Backup the APK and upload to the git repo.

### Core Infrastructure Updates
- [ ] Implement SQLCipher encryption for sqlite3
- [ ] Update code to be able to compile with current toolchain
- [ ] Implement application-level compression using GZIP (Option 1)
- [ ] Implement database backup and corruption recovery system
- [ ] Implement database optimization with scheduled VACUUM operations
- [ ] Upgrade API to latest Android version
- [ ] Update GUI elements for modern Android compatibility

### User Interface & Settings
- [ ] Determine if a settings page exists (in ID34 application), if not create one. Add setting to:
  - Enable/disable backup to GCM-Google Drive (default: true) 
  - Setting for backup over cellular network (default: true)
  - If SQLite (file on disk) size is greater than a set configurable size (512kb) show a warning

## Extended Analysis Items

### SQLite Encryption & Security
- [ ] Deep-dive into SQLCipher password handling
  - Explain pragma key, default PBKDF2(64K, HMAC-SHA512), cipher settings, salt storage, and how to rotate/upgrade pass-phrases
  - Demonstrate compiling SQLCipher on Ubuntu and integrating it with Android Room/Jetpack using a password

- [ ] Evaluate alternative encryption approaches (filesystem, application, custom password KDF)
  - Compare dm-crypt/LUKS, eCryptFS, Android scoped storage + KeyStore-sealed AES key that encrypts DB with a user password, and per-column crypto
  - Highlight pros/cons vs. SQLCipher regarding password change, multi-user, and backup-restore flows

- [ ] Android-specific analysis & password storage
  - Show how to store the DB key encrypted with Android KeyStore, derive it from user password or biometrics, enroll/de-enroll, and handle on-device backups
  - Cover SQLCipher for Android bindings and Room integration

### Password Management Strategy
- [ ] Design password management strategy
  - Specify password strength rules, rotation schedule, forgotten-password recovery, and secure input UI
  - Provide guideline to transition from hard-coded pass-phrase to user-supplied password protected by KeyStore hardware-backed keys

### Implementation & Examples
- [ ] Produce implementation examples & code samples
  - Ubuntu C example (CLI) opening a SQLCipher DB with a password
  - Kotlin Android example using Room + SQLCipher, KeyStore, and biometric prompt
  - Python example using pysqlcipher3

### Performance & Optimization
- [ ] Benchmark performance impact with and without password encryption
  - Measure insert/select throughput and battery/CPU impact on Android ARM and x86-64 Linux, varying page_size and KDF iterations

### Security Best Practices
- [ ] Outline security best practices
  - Cover secure random, password policies, memory sanitization, side-channel considerations, threat modeling, and code audit checklist

### Licensing & Costs
- [ ] Assess licensing and cost
  - Compare SQLCipher (BSD), wxSQLite3 (GPL/LGPL/commercial), SQLite SEE (proprietary), and filesystem solutions (GPL/BSD, kernel)

### Migration Planning
- [ ] Plan migration strategies from unencrypted to password-encrypted DB
  - Online PRAGMA rekey, offline export/import, staged rollout on Android (database migration step), and backup compatibility

### Documentation
- [ ] Compile final recommendations deliverable
  - Assemble a Markdown/HTML report with decision matrix, code snippets, and actionable next steps

## Detailed Implementation Plans

### Application-Level Compression Plan (Option 1 - GZIP)
- [ ] **Compression Strategy**
  - Use GZIP compression for text fields larger than 1KB
  - Compress during idle time to avoid UI blocking
  - Add `compressed` flag column to track compression status
  - Implement background compression service

- [ ] **Implementation Details**
  ```kotlin
  // Compress text data before storing
  fun compressText(text: String): ByteArray {
      if (text.length < 1024) return text.toByteArray() // Skip small text
      val outputStream = ByteArrayOutputStream()
      val gzipStream = GZIPOutputStream(outputStream)
      gzipStream.write(text.toByteArray(Charsets.UTF_8))
      gzipStream.close()
      return outputStream.toByteArray()
  }
  ```

- [ ] **Idle Time Compression**
  - Monitor app idle state (no user interaction for 30+ seconds)
  - Queue uncompressed large text for background compression
  - Use AsyncTask or WorkManager for background processing
  - Update compression status atomically

### Database Backup & Corruption Recovery Plan
- [ ] **Automatic Backup System**
  - Create backup before major operations (encryption, compression, schema changes)
  - Store backup with timestamp: `id34_backup_YYYYMMDD_HHMMSS.db`
  - Keep last 3 backups, rotate older ones
  - Compress backup files to save space

- [ ] **Corruption Detection**
  - Run `PRAGMA integrity_check` on startup
  - Check for specific corruption types:
    - Table structure corruption
    - Individual column corruption
    - Index corruption
    - Page corruption

- [ ] **Recovery Dialog Implementation**
  ```kotlin
  // Present corruption recovery options to user
  fun showCorruptionDialog(corruptionDetails: String) {
      AlertDialog.Builder(context)
          .setTitle("Database Corruption Detected")
          .setMessage("Database has become corrupted. $corruptionDetails\n\nDo you want to restore from last backup?")
          .setPositiveButton("OK") { _, _ -> restoreFromBackup() }
          .setNegativeButton("Cancel") { _, _ -> handleCorruptionManually() }
          .setCancelable(false)
          .show()
  }
  ```

- [ ] **Backup Verification**
  - Verify backup integrity before restoration
  - Test backup readability with sample queries
  - Log backup/restore operations for debugging

### Database Optimization Plan (#4)
- [ ] **Vacuum Scheduling System**
  - Store last vacuum time in app preferences
  - Schedule regular VACUUM operations:
    - **Weekly VACUUM**: Every 7 days
    - **Monthly VACUUM FULL**: Every 30 days (complete rebuild)
  - Check if automatic vacuum is enabled: `PRAGMA auto_vacuum`

- [ ] **Vacuum Implementation**
  ```sql
  -- Store vacuum metadata
  CREATE TABLE IF NOT EXISTS maintenance_log (
      id INTEGER PRIMARY KEY,
      operation TEXT,
      timestamp INTEGER,
      duration_ms INTEGER,
      result TEXT
  );
  
  -- Regular vacuum (weekly)
  PRAGMA optimize;
  VACUUM;
  
  -- Full vacuum (monthly)
  PRAGMA page_size = 4096;
  VACUUM;
  ANALYZE;
  ```

- [ ] **Optimization Scheduling**
  - Check maintenance schedule on app startup
  - Run maintenance during app idle time
  - Show progress dialog for long operations
  - Log performance improvements (database size reduction)

### API Upgrade Plan
- [ ] **Target API Assessment**
  - Current: compileSdkVersion 16, targetSdkVersion 15, minSdkVersion 11
  - Target: Latest stable Android API (API 34/Android 14)
  - Intermediate target: API 28 (Android 9) for broad compatibility

- [ ] **Migration Strategy**
  - **Phase 1**: Update build tools and Gradle
  - **Phase 2**: Migrate deprecated APIs
  - **Phase 3**: Update UI components to Material Design
  - **Phase 4**: Implement modern Android architecture (ViewModel, LiveData)
  - **Phase 5**: Update to latest API level

- [ ] **GUI Modernization Plan**
  - Replace ActionBar with Toolbar
  - Migrate to ConstraintLayout
  - Implement Material Design components
  - Add dark theme support
  - Update icons to vector drawables
  - Implement proper permission handling (runtime permissions)

- [ ] **Deprecated API Migration**
  - **GCM → FCM**: Migrate Google Cloud Messaging to Firebase Cloud Messaging
  - **AsyncTask → WorkManager**: Replace deprecated AsyncTask
  - **File storage**: Update to scoped storage (Android 10+)
  - **Network**: Migrate to OkHttp/Retrofit for API calls

## Compression Options for SQLite

### Built-in SQLite Compression
SQLite does not have built-in compression, but several approaches are available:

#### 1. Application-Level Compression
```sql
-- Store compressed BLOB data
INSERT INTO table_name (data) VALUES (compress_function(original_data));
```

#### 2. SQLite Extensions
- **SQLite Compress Extension**: Custom extension for transparent compression
- **ZLIB/GZIP compression**: Via custom functions or triggers

#### 3. Column-Level Compression
```kotlin
// Android example with GZIP compression
fun compressString(text: String): ByteArray {
    val outputStream = ByteArrayOutputStream()
    val gzipStream = GZIPOutputStream(outputStream)
    gzipStream.write(text.toByteArray())
    gzipStream.close()
    return outputStream.toByteArray()
}
```

#### 4. Page-Level Compression (SQLite 3.38+)
- Experimental feature for transparent page compression
- Requires custom SQLite build with compression support

#### 5. Vacuum with Page Size Optimization
```sql
PRAGMA page_size = 4096;  -- Optimize for your data
VACUUM;  -- Reclaim space and optimize storage
```

### Recommended Compression Strategy for ID34 (SELECTED APPROACH)
1. ✅ **Application-Level GZIP Compression (Option 1)**: Compress text data >1KB during idle time
2. ✅ **Database Optimization (#4)**: Weekly VACUUM + Monthly VACUUM FULL with scheduling
3. ✅ **Automatic Backup System**: Before major operations with corruption recovery
4. ✅ **Idle Time Processing**: Background compression and maintenance operations

### Implementation Priority (UPDATED)
1. 🎯 **Immediate Priority**: 
   - Application-level GZIP compression during idle time
   - Database backup system with corruption recovery dialog
   - Scheduled VACUUM operations (weekly/monthly)
   
2. 🎯 **High Priority**: 
   - API upgrade to modern Android (Phase 1-2: Build tools + deprecated APIs)
   - GUI modernization (Material Design components)
   
3. ⚠️ **Medium Priority**: 
   - Complete API upgrade (Phase 3-5: Architecture + latest API)
   - Advanced compression optimizations

---

## Implementation Timeline & Notes

### Phase 1: Core Infrastructure (Weeks 1-2)
- Implement GZIP compression with idle-time processing
- Add database backup system with corruption detection
- Implement scheduled VACUUM operations
- Update build.gradle for modern toolchain compatibility

### Phase 2: API Migration (Weeks 3-4)
- Migrate from API 16 → API 28 (intermediate target)
- Replace deprecated APIs (AsyncTask, GCM, file storage)
- Update GUI components to Material Design
- Implement runtime permissions

### Phase 3: Modernization (Weeks 5-6)
- Complete API upgrade to latest (API 34)
- Implement modern Android architecture (ViewModel, LiveData, Room)
- Add advanced features (dark theme, vector icons)
- Performance optimization and testing

### Technical Notes
- **Current State**: Android API level is quite old (target: 15, compile: 16)
- **Critical**: GCM is deprecated → must migrate to Firebase Cloud Messaging (FCM)
- **Architecture**: Consider migration to modern Android components (Room, WorkManager)
- **Testing**: Extensive testing required due to major API changes
- **Compatibility**: Maintain backward compatibility where possible
- **Performance**: Monitor battery usage during compression operations
- **Security**: Multiple critical vulnerabilities identified - see Security Assessment below

---

## 🚨 SECURITY ASSESSMENT & REMEDIATION PLAN

### Overall Risk Level: **HIGH** 
⚠️ **This application should NOT be deployed to production until critical security issues are resolved**

### 🚨 CRITICAL SECURITY FINDINGS

#### 1. Hardcoded Twitter API Credentials
- **Risk**: CRITICAL
- **File**: `IdeaAddActivity.java` lines 237-238
- **Impact**: Credentials extractable from APK, potential account compromise
- **Fix Plan**:
  - [ ] Remove hardcoded credentials from source code immediately
  - [ ] Store Twitter API credentials in SQLCipher encrypted database
  - [ ] Implement secure credential retrieval with Android KeyStore
  - [ ] Use environment variables for build-time credential injection

#### 2. Hardcoded GCM Sender ID
- **Risk**: HIGH
- **Files**: `IdeaListActivity.java` line 35, `GCMIntentService.java` line 17
- **Impact**: Unauthorized push notification access
- **Fix Plan**:
  - [ ] Move GCM Sender ID to encrypted configuration
  - [ ] Migrate GCM to Firebase Cloud Messaging (FCM)
  - [ ] Implement secure FCM token management

#### 3. Insecure HTTP Base URL Construction
- **Risk**: HIGH
- **File**: `Iserver.java` lines 75-76
- **Impact**: All API communications unencrypted, data interception risk
- **Fix Plan**:
  - [ ] **IMMEDIATELY** change all HTTP endpoints to HTTPS
  - [ ] Implement certificate pinning for API endpoints
  - [ ] Add network security config for Android 9+ compatibility
  - [ ] Validate SSL certificates in network layer

#### 4. Phone Number in URL Parameters
- **Risk**: HIGH
- **File**: `Iserver.java` multiple locations
- **Impact**: Phone numbers in server logs and network traffic
- **Fix Plan**:
  - [ ] Convert GET requests with phone numbers to POST with encrypted body
  - [ ] Implement request body encryption for sensitive data
  - [ ] Use tokens instead of direct phone number transmission
  - [ ] Add phone number hashing for server-side storage

#### 5. SQL Injection Vulnerability
- **Risk**: HIGH
- **File**: `SQLiteAdapter.java` lines 328-333
- **Impact**: Database compromise via SQL injection
- **Fix Plan**:
  - [ ] **IMMEDIATELY** replace string concatenation with parameterized queries
  - [ ] Implement input validation for all user inputs
  - [ ] Add SQL injection prevention patterns
  - [ ] Code audit for additional injection points

### ⚠️ MODERATE SECURITY FINDINGS

#### 6. Excessive Permissions
- **Risk**: MEDIUM
- **File**: `AndroidManifest.xml`
- **Issues**: READ_PHONE_STATE, WRITE_EXTERNAL_STORAGE, GET_ACCOUNTS
- **Fix Plan**:
  - [ ] Audit all permissions for necessity
  - [ ] Implement runtime permission requests (API 23+)
  - [ ] Use scoped storage instead of external storage
  - [ ] Remove unnecessary permissions

#### 7. Debug Information Exposure
- **Risk**: MEDIUM
- **Files**: Multiple Log.e(), Log.d() calls
- **Impact**: Sensitive data leakage to system logs
- **Fix Plan**:
  - [ ] Remove all debug logging in production builds
  - [ ] Implement conditional logging based on build type
  - [ ] Add ProGuard rules to strip logging
  - [ ] Implement secure logging wrapper

#### 8. Insecure Data Storage
- **Risk**: MEDIUM
- **Files**: SharedPreferences usage throughout
- **Impact**: Sensitive data accessible with root access
- **Fix Plan**:
  - [ ] **Migrate sensitive data to SQLCipher encrypted storage**
  - [ ] Use Android KeyStore for credential encryption keys
  - [ ] Implement secure SharedPreferences wrapper
  - [ ] Encrypt phone numbers and email addresses before storage

#### 9. Clear-Text Network Traffic
- **Risk**: MEDIUM
- **File**: `Iserver.java` - All HTTP requests
- **Impact**: Network traffic interception
- **Fix Plan**:
  - [ ] Implement HTTPS with certificate pinning
  - [ ] Add network security configuration
  - [ ] Use OkHttp with custom SSL socket factory
  - [ ] Implement request/response encryption layer

### ℹ️ LOW RISK FINDINGS

#### 10. Incomplete OAuth Implementation
- **Risk**: LOW
- **File**: `IdeaAddActivity.java` lines 289-307
- **Fix Plan**:
  - [ ] Complete Twitter OAuth implementation or remove dead code
  - [ ] Use modern OAuth 2.0 libraries
  - [ ] Implement proper token storage and refresh

#### 11. Poor Exception Handling
- **Risk**: LOW
- **Files**: Multiple locations with empty catch blocks
- **Fix Plan**:
  - [ ] Implement proper error handling throughout application
  - [ ] Add security-focused exception logging
  - [ ] Remove empty catch blocks

## SECURITY REMEDIATION IMPLEMENTATION PLAN

### Phase 0: Emergency Security Fixes (Week 0)
🚨 **CRITICAL - Must be completed before any other development**

- [ ] **Remove hardcoded Twitter API credentials**
  - Extract credentials from source code
  - Store in encrypted SQLite database using SQLCipher
  - Implement secure credential retrieval

- [ ] **Fix SQL injection vulnerability**
  - Replace string concatenation with parameterized queries
  - Add input validation
  - Test for additional injection points

- [ ] **Convert HTTP to HTTPS**
  - Update all API endpoints to HTTPS
  - Test SSL certificate validation
  - Implement basic certificate pinning

### Phase 1: Core Security Implementation (Weeks 1-2)
- [ ] **Encrypted Credential Storage**
  ```kotlin
  // Store Twitter credentials in SQLCipher database
  CREATE TABLE credentials (
      id INTEGER PRIMARY KEY,
      service TEXT NOT NULL,
      credential_type TEXT NOT NULL,
      encrypted_value BLOB NOT NULL,
      created_timestamp INTEGER,
      last_used_timestamp INTEGER
  );
  ```

- [ ] **Secure Network Layer**
  ```kotlin
  // Implement HTTPS with certificate pinning
  val client = OkHttpClient.Builder()
      .certificatePinner(certificatePinner)
      .addInterceptor(EncryptionInterceptor())
      .build()
  ```

- [ ] **Input Validation Framework**
  ```kotlin
  // Parameterized query example
  fun getIdeasByCategory(catId: String): Cursor {
      val selection = "category_id = ?"
      val selectionArgs = arrayOf(catId)
      return db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null)
  }
  ```

### Phase 2: Advanced Security Features (Weeks 3-4)
- [ ] **Android KeyStore Integration**
  - Generate encryption keys in hardware-backed KeyStore
  - Implement biometric authentication for sensitive operations
  - Add key rotation and management

- [ ] **Network Security Enhancements**
  - Implement request/response encryption
  - Add API request signing
  - Implement secure phone number tokenization

- [ ] **Data Protection**
  - Encrypt all PII data before SQLite storage
  - Implement secure data wipe on app uninstall
  - Add data export encryption

### Phase 3: Security Monitoring & Compliance (Weeks 5-6)
- [ ] **Security Monitoring**
  - Add security event logging
  - Implement intrusion detection
  - Add security metrics and alerts

- [ ] **Code Security**
  - Enable ProGuard/R8 obfuscation
  - Remove debug symbols from release builds
  - Implement anti-tampering measures

### Security Testing Plan
- [ ] **Static Analysis**
  - Run SAST tools (SonarQube, Checkmarx)
  - Manual code security review
  - Dependency vulnerability scanning

- [ ] **Dynamic Testing**
  - Network traffic analysis
  - Runtime security testing
  - SQL injection testing

- [ ] **Penetration Testing**
  - APK reverse engineering assessment
  - Network security testing
  - Data storage security validation

### Compliance & Documentation
- [ ] **Security Documentation**
  - Document encryption methods and key management
  - Create security architecture diagrams
  - Maintain security changelog

- [ ] **Privacy Compliance**
  - Implement GDPR-compliant data handling
  - Add privacy policy and consent mechanisms
  - Implement data deletion capabilities

### Post-Implementation Validation
- [ ] **Security Verification**
  - Verify no hardcoded secrets remain in APK
  - Confirm all network traffic is encrypted
  - Validate parameterized queries prevent injection
  - Test credential encryption/decryption

---

**⚠️ IMPORTANT**: The security fixes in Phase 0 are **mandatory** and must be completed before any other development work. The application poses significant security risks to users in its current state.
