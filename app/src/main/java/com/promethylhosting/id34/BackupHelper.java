package com.promethylhosting.id34;

import android.content.Context;
import android.util.Log;
import timber.log.Timber;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;

/**
 * BackupHelper - Automatic Dated Backup System for ID34
 * 
 * Implements user backup rules:
 * - Create dated backups in backup/ folder
 * - Format: {name}.{iso-8601 date}.{ext}
 * - Max 50 copies for files under 150KB
 * - Max 25 copies for files larger than 150KB
 * - Automatic cleanup of old backups
 * - Made before git push or large/complicated changes
 * 
 * Example backup names:
 * - myFile.2025-01-05T14-30-45.py
 * - database.2025-01-05T14-30-45.db
 * - config.2025-01-05T14-30-45.json
 * 
 * Thread-safe operations with proper error handling
 * 
 * @author ID34 Team
 * @version 2.0-crypto
 */
public class BackupHelper {
    
    private static final String LOG_TAG = "BackupHelper";
    private static final String BACKUP_DIR_NAME = "backup";
    
    // Size thresholds from user rules
    private static final long SIZE_THRESHOLD_BYTES = 150 * 1024; // 150KB
    private static final int MAX_BACKUPS_SMALL_FILES = 50;
    private static final int MAX_BACKUPS_LARGE_FILES = 25;
    
    // ISO-8601 date format for backup filenames
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern( "yyyy-MM-dd'T'HH-mm-ss" );
    
    private final Context context;
    private final File backupDirectory;
    
    /**
     * Initialize BackupHelper with application context
     */
    public BackupHelper( Context context ) {
        this.context = context;
        this.backupDirectory = new File( context.getFilesDir(), BACKUP_DIR_NAME );
        ensureBackupDirectoryExists();
    }
    
    /**
     * Create dated backup of a file
     * 
     * @param sourceFile File to backup
     * @return BackupResult with success status and backup file info
     */
    public BackupResult createBackup( File sourceFile ) {
        if ( sourceFile == null || !sourceFile.exists() ) {
            String error = "Source file does not exist: " + 
                          ( sourceFile != null ? sourceFile.getPath() : "null" );
            Timber.w( error );
            return BackupResult.failure( error );
        }
        
        try {
            Timber.d( "Creating backup for: %s", sourceFile.getPath() );
            
            // Generate backup filename
            String backupFileName = generateBackupFileName( sourceFile );
            File backupFile = new File( backupDirectory, backupFileName );
            
            // Copy source file to backup location
            copyFile( sourceFile, backupFile );
            
            // Manage backup count
            manageBackupCount( sourceFile );
            
            Timber.i( "Backup created: %s (size: %d bytes)", 
                     backupFileName, backupFile.length() );
            
            return BackupResult.success( backupFile );
            
        } catch ( Exception e ) {
            String error = "Failed to create backup for " + sourceFile.getName() + ": " + e.getMessage();
            Timber.e( e, error );
            return BackupResult.failure( error );
        }
    }
    
    /**
     * Create backup before ACP (Add, Commit, Push) operation
     * This is called before major git operations per user rules
     */
    public BackupResult createACPBackup( File sourceFile, String changeDescription ) {
        Timber.i( "Creating pre-ACP backup for: %s - %s", sourceFile.getName(), changeDescription );
        
        BackupResult result = createBackup( sourceFile );
        
        if ( result.isSuccess() ) {
            // Log ACP backup creation
            Timber.i( "Pre-ACP backup created for upcoming changes: %s", changeDescription );
        }
        
        return result;
    }
    
    /**
     * Create backup before large or complicated changes
     */
    public BackupResult createPreChangeBackup( File sourceFile, String changeType ) {
        Timber.i( "Creating pre-change backup for: %s - Change type: %s", 
                 sourceFile.getName(), changeType );
        
        BackupResult result = createBackup( sourceFile );
        
        if ( result.isSuccess() ) {
            Timber.i( "Pre-change backup created for: %s", changeType );
        }
        
        return result;
    }
    
    /**
     * Get backup statistics for a file
     */
    public BackupStats getBackupStats( File sourceFile ) {
        String baseFileName = getBaseFileName( sourceFile );
        File[] backups = backupDirectory.listFiles( ( dir, name ) -> 
            name.startsWith( baseFileName ) && isBackupFile( name, baseFileName ) );
        
        if ( backups == null ) {
            return new BackupStats( 0, 0, null, null );
        }
        
        // Sort by creation time (newest first)
        Arrays.sort( backups, Comparator.comparingLong( File::lastModified ).reversed() );
        
        long totalSize = Arrays.stream( backups ).mapToLong( File::length ).sum();
        File newest = backups.length > 0 ? backups[0] : null;
        File oldest = backups.length > 0 ? backups[backups.length - 1] : null;
        
        return new BackupStats( backups.length, totalSize, newest, oldest );
    }
    
    /**
     * Clean up old backups manually (beyond automatic cleanup)
     */
    public int cleanupOldBackups( File sourceFile, int maxAge_days ) {
        long cutoffTime = System.currentTimeMillis() - ( maxAge_days * 24L * 60 * 60 * 1000 );
        
        String baseFileName = getBaseFileName( sourceFile );
        File[] backups = backupDirectory.listFiles( ( dir, name ) -> 
            name.startsWith( baseFileName ) && isBackupFile( name, baseFileName ) );
        
        if ( backups == null ) return 0;
        
        int deletedCount = 0;
        for ( File backup : backups ) {
            if ( backup.lastModified() < cutoffTime ) {
                if ( backup.delete() ) {
                    deletedCount++;
                    Timber.d( "Deleted old backup: %s", backup.getName() );
                }
            }
        }
        
        if ( deletedCount > 0 ) {
            Timber.i( "Cleaned up %d old backups for %s", deletedCount, sourceFile.getName() );
        }
        
        return deletedCount;
    }
    
    // --- Private Helper Methods ---
    
    private void ensureBackupDirectoryExists() {
        if ( !backupDirectory.exists() ) {
            if ( backupDirectory.mkdirs() ) {
                Timber.d( "Created backup directory: %s", backupDirectory.getPath() );
            } else {
                Timber.e( "Failed to create backup directory: %s", backupDirectory.getPath() );
            }
        }
    }
    
    private String generateBackupFileName( File sourceFile ) {
        String fileName = sourceFile.getName();
        String timestamp = LocalDateTime.now().format( DATE_FORMATTER );
        
        int lastDotIndex = fileName.lastIndexOf( '.' );
        if ( lastDotIndex > 0 ) {
            // File has extension: name.timestamp.ext
            String baseName = fileName.substring( 0, lastDotIndex );
            String extension = fileName.substring( lastDotIndex );
            return baseName + "." + timestamp + extension;
        } else {
            // File has no extension: name.timestamp
            return fileName + "." + timestamp;
        }
    }
    
    private String getBaseFileName( File sourceFile ) {
        String fileName = sourceFile.getName();
        int lastDotIndex = fileName.lastIndexOf( '.' );
        return ( lastDotIndex > 0 ) ? fileName.substring( 0, lastDotIndex ) : fileName;
    }
    
    private boolean isBackupFile( String fileName, String baseFileName ) {
        // Check if filename matches pattern: baseFileName.YYYY-MM-DDTHH-mm-ss[.ext]
        if ( !fileName.startsWith( baseFileName + "." ) ) return false;
        
        // Extract the timestamp part
        String afterBase = fileName.substring( baseFileName.length() + 1 );
        
        // Check if it contains a valid timestamp pattern
        return afterBase.matches( "\\d{4}-\\d{2}-\\d{2}T\\d{2}-\\d{2}-\\d{2}.*" );
    }
    
    private void copyFile( File source, File destination ) throws IOException {
        java.nio.file.Files.copy( source.toPath(), destination.toPath(), 
                                 java.nio.file.StandardCopyOption.REPLACE_EXISTING );
    }
    
    private void manageBackupCount( File sourceFile ) {
        long fileSize = sourceFile.length();
        int maxBackups = ( fileSize < SIZE_THRESHOLD_BYTES ) ? 
                        MAX_BACKUPS_SMALL_FILES : MAX_BACKUPS_LARGE_FILES;
        
        String baseFileName = getBaseFileName( sourceFile );
        File[] backups = backupDirectory.listFiles( ( dir, name ) -> 
            name.startsWith( baseFileName ) && isBackupFile( name, baseFileName ) );
        
        if ( backups == null ) return;
        
        if ( backups.length > maxBackups ) {
            // Sort by last modified (oldest first for deletion)
            Arrays.sort( backups, Comparator.comparingLong( File::lastModified ) );
            
            int toDelete = backups.length - maxBackups;
            for ( int i = 0; i < toDelete; i++ ) {
                if ( backups[i].delete() ) {
                    Timber.d( "Deleted old backup: %s", backups[i].getName() );
                }
            }
            
            Timber.i( "Managed backup count: deleted %d old backups, keeping %d", 
                     toDelete, maxBackups );
        }
    }
    
    // --- Result Classes ---
    
    /**
     * Result of backup operation
     */
    public static class BackupResult {
        private final boolean success;
        private final String message;
        private final File backupFile;
        
        private BackupResult( boolean success, String message, File backupFile ) {
            this.success = success;
            this.message = message;
            this.backupFile = backupFile;
        }
        
        public static BackupResult success( File backupFile ) {
            return new BackupResult( true, "Backup created successfully", backupFile );
        }
        
        public static BackupResult failure( String errorMessage ) {
            return new BackupResult( false, errorMessage, null );
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public File getBackupFile() {
            return backupFile;
        }
    }
    
    /**
     * Backup statistics for a file
     */
    public static class BackupStats {
        private final int count;
        private final long totalSize;
        private final File newest;
        private final File oldest;
        
        public BackupStats( int count, long totalSize, File newest, File oldest ) {
            this.count = count;
            this.totalSize = totalSize;
            this.newest = newest;
            this.oldest = oldest;
        }
        
        public int getCount() {
            return count;
        }
        
        public long getTotalSize() {
            return totalSize;
        }
        
        public File getNewest() {
            return newest;
        }
        
        public File getOldest() {
            return oldest;
        }
        
        public String getFormattedSize() {
            if ( totalSize < 1024 ) return totalSize + " B";
            if ( totalSize < 1024 * 1024 ) return ( totalSize / 1024 ) + " KB";
            return ( totalSize / ( 1024 * 1024 ) ) + " MB";
        }
    }
}
