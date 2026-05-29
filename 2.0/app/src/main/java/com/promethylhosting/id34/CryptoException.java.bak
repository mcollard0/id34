package com.promethylhosting.id34;

/**
 * Comprehensive Cryptographic Exception Hierarchy for ID34
 * 
 * Provides specific exception types for different cryptographic failures,
 * enabling precise error handling and user-friendly error messages.
 * 
 * Exception Hierarchy:
 * CryptoException (base)
 * ├── KeyException
 * │   ├── KeyGenerationException
 * │   ├── KeyNotFoundException  
 * │   └── KeyRotationException
 * ├── EncryptionException
 * │   ├── CipherInitializationException
 * │   └── EncryptionFailedException
 * ├── DecryptionException
 * │   ├── InvalidCipherException
 * │   ├── TamperedDataException
 * │   └── DecryptionFailedException
 * └── MigrationException
 *     ├── LegacyDatabaseException
 *     └── DataMigrationException
 * 
 * All exceptions follow user preferences for formatting ( spaces, semicolons )
 * 
 * @author ID34 Security Team
 * @version 2.0-crypto
 */
public class CryptoException extends Exception {
    
    private final CryptoErrorCode errorCode;
    private final String userFriendlyMessage;
    
    /**
     * Error codes for different types of cryptographic failures
     */
    public enum CryptoErrorCode {
        // Key Management Errors (1000-1999)
        KEY_GENERATION_FAILED( 1001, "Failed to generate cryptographic key" ),
        KEY_NOT_FOUND( 1002, "Cryptographic key not found" ),
        KEY_ROTATION_FAILED( 1003, "Automatic key rotation failed" ),
        KEYSTORE_UNAVAILABLE( 1004, "Android KeyStore not available" ),
        
        // Encryption Errors (2000-2999)
        CIPHER_INIT_FAILED( 2001, "Failed to initialize cipher" ),
        ENCRYPTION_FAILED( 2002, "Data encryption failed" ),
        INVALID_INPUT_DATA( 2003, "Invalid input data for encryption" ),
        
        // Decryption Errors (3000-3999)
        INVALID_CIPHER_TYPE( 3001, "Unknown or invalid cipher type" ),
        TAMPERED_DATA_DETECTED( 3002, "Data tampering detected" ),
        DECRYPTION_FAILED( 3003, "Data decryption failed" ),
        AUTHENTICATION_FAILED( 3004, "Data authentication failed" ),
        
        // Migration Errors (4000-4999)
        LEGACY_DATABASE_ERROR( 4001, "Error accessing legacy database" ),
        DATA_MIGRATION_FAILED( 4002, "Database migration failed" ),
        MIGRATION_VERIFICATION_FAILED( 4003, "Migration verification failed" ),
        
        // General Errors (5000-5999)
        UNKNOWN_ERROR( 5000, "Unknown cryptographic error" ),
        CONFIGURATION_ERROR( 5001, "Cryptographic configuration error" ),
        SYSTEM_ERROR( 5002, "System-level cryptographic error" );
        
        private final int code;
        private final String defaultMessage;
        
        CryptoErrorCode( int code, String defaultMessage ) {
            this.code = code;
            this.defaultMessage = defaultMessage;
        }
        
        public int getCode() {
            return code;
        }
        
        public String getDefaultMessage() {
            return defaultMessage;
        }
    }
    
    /**
     * Create CryptoException with error code
     */
    public CryptoException( CryptoErrorCode errorCode, String message ) {
        super( message );
        this.errorCode = errorCode;
        this.userFriendlyMessage = createUserFriendlyMessage( errorCode, message );
    }
    
    /**
     * Create CryptoException with error code and cause
     */
    public CryptoException( CryptoErrorCode errorCode, String message, Throwable cause ) {
        super( message, cause );
        this.errorCode = errorCode;
        this.userFriendlyMessage = createUserFriendlyMessage( errorCode, message );
    }
    
    public CryptoErrorCode getErrorCode() {
        return errorCode;
    }
    
    public String getUserFriendlyMessage() {
        return userFriendlyMessage;
    }
    
    private String createUserFriendlyMessage( CryptoErrorCode errorCode, String technicalMessage ) {
        switch ( errorCode.getCode() / 1000 ) {
            case 1: // Key errors
                return "Security key issue: " + errorCode.getDefaultMessage();
            case 2: // Encryption errors  
                return "Data protection failed: " + errorCode.getDefaultMessage();
            case 3: // Decryption errors
                return "Data access failed: " + errorCode.getDefaultMessage();
            case 4: // Migration errors
                return "Database upgrade issue: " + errorCode.getDefaultMessage();
            default: // General errors
                return "Security system error: " + errorCode.getDefaultMessage();
        }
    }
    
    // --- Specific Exception Types ---
    
    /**
     * Key Management Exceptions
     */
    public static class KeyException extends CryptoException {
        public KeyException( CryptoErrorCode errorCode, String message ) {
            super( errorCode, message );
        }
        
        public KeyException( CryptoErrorCode errorCode, String message, Throwable cause ) {
            super( errorCode, message, cause );
        }
    }
    
    public static class KeyGenerationException extends KeyException {
        public KeyGenerationException( String message ) {
            super( CryptoErrorCode.KEY_GENERATION_FAILED, message );
        }
        
        public KeyGenerationException( String message, Throwable cause ) {
            super( CryptoErrorCode.KEY_GENERATION_FAILED, message, cause );
        }
    }
    
    public static class KeyNotFoundException extends KeyException {
        public KeyNotFoundException( String message ) {
            super( CryptoErrorCode.KEY_NOT_FOUND, message );
        }
        
        public KeyNotFoundException( String message, Throwable cause ) {
            super( CryptoErrorCode.KEY_NOT_FOUND, message, cause );
        }
    }
    
    public static class KeyRotationException extends KeyException {
        public KeyRotationException( String message ) {
            super( CryptoErrorCode.KEY_ROTATION_FAILED, message );
        }
        
        public KeyRotationException( String message, Throwable cause ) {
            super( CryptoErrorCode.KEY_ROTATION_FAILED, message, cause );
        }
    }
    
    /**
     * Encryption Exceptions
     */
    public static class EncryptionException extends CryptoException {
        public EncryptionException( CryptoErrorCode errorCode, String message ) {
            super( errorCode, message );
        }
        
        public EncryptionException( CryptoErrorCode errorCode, String message, Throwable cause ) {
            super( errorCode, message, cause );
        }
    }
    
    public static class CipherInitializationException extends EncryptionException {
        public CipherInitializationException( String message ) {
            super( CryptoErrorCode.CIPHER_INIT_FAILED, message );
        }
        
        public CipherInitializationException( String message, Throwable cause ) {
            super( CryptoErrorCode.CIPHER_INIT_FAILED, message, cause );
        }
    }
    
    public static class EncryptionFailedException extends EncryptionException {
        public EncryptionFailedException( String message ) {
            super( CryptoErrorCode.ENCRYPTION_FAILED, message );
        }
        
        public EncryptionFailedException( String message, Throwable cause ) {
            super( CryptoErrorCode.ENCRYPTION_FAILED, message, cause );
        }
    }
    
    /**
     * Decryption Exceptions
     */
    public static class DecryptionException extends CryptoException {
        public DecryptionException( CryptoErrorCode errorCode, String message ) {
            super( errorCode, message );
        }
        
        public DecryptionException( CryptoErrorCode errorCode, String message, Throwable cause ) {
            super( errorCode, message, cause );
        }
    }
    
    public static class InvalidCipherException extends DecryptionException {
        public InvalidCipherException( String message ) {
            super( CryptoErrorCode.INVALID_CIPHER_TYPE, message );
        }
        
        public InvalidCipherException( String message, Throwable cause ) {
            super( CryptoErrorCode.INVALID_CIPHER_TYPE, message, cause );
        }
    }
    
    public static class TamperedDataException extends DecryptionException {
        public TamperedDataException( String message ) {
            super( CryptoErrorCode.TAMPERED_DATA_DETECTED, message );
        }
        
        public TamperedDataException( String message, Throwable cause ) {
            super( CryptoErrorCode.TAMPERED_DATA_DETECTED, message, cause );
        }
    }
    
    public static class DecryptionFailedException extends DecryptionException {
        public DecryptionFailedException( String message ) {
            super( CryptoErrorCode.DECRYPTION_FAILED, message );
        }
        
        public DecryptionFailedException( String message, Throwable cause ) {
            super( CryptoErrorCode.DECRYPTION_FAILED, message, cause );
        }
    }
    
    /**
     * Migration Exceptions
     */
    public static class MigrationException extends CryptoException {
        public MigrationException( CryptoErrorCode errorCode, String message ) {
            super( errorCode, message );
        }
        
        public MigrationException( CryptoErrorCode errorCode, String message, Throwable cause ) {
            super( errorCode, message, cause );
        }
    }
    
    public static class LegacyDatabaseException extends MigrationException {
        public LegacyDatabaseException( String message ) {
            super( CryptoErrorCode.LEGACY_DATABASE_ERROR, message );
        }
        
        public LegacyDatabaseException( String message, Throwable cause ) {
            super( CryptoErrorCode.LEGACY_DATABASE_ERROR, message, cause );
        }
    }
    
    public static class DataMigrationException extends MigrationException {
        public DataMigrationException( String message ) {
            super( CryptoErrorCode.DATA_MIGRATION_FAILED, message );
        }
        
        public DataMigrationException( String message, Throwable cause ) {
            super( CryptoErrorCode.DATA_MIGRATION_FAILED, message, cause );
        }
    }
    
    /**
     * Get formatted error message for logging
     * Format: [ERROR_CODE] Technical message (User message)
     */
    public String getFormattedLogMessage() {
        return String.format( "[%d] %s (%s)", 
                             errorCode.getCode(), 
                             getMessage(), 
                             userFriendlyMessage );
    }
    
    /**
     * Check if error is recoverable
     */
    public boolean isRecoverable() {
        switch ( errorCode ) {
            case KEY_NOT_FOUND:
            case KEY_ROTATION_FAILED:
            case KEYSTORE_UNAVAILABLE:
            case MIGRATION_VERIFICATION_FAILED:
                return true; // These can often be retried or worked around
            
            case TAMPERED_DATA_DETECTED:
            case AUTHENTICATION_FAILED:
            case INVALID_CIPHER_TYPE:
                return false; // These indicate serious security issues
                
            default:
                return false; // Conservative default
        }
    }
    
    /**
     * Check if error requires immediate attention
     */
    public boolean isSecurityCritical() {
        switch ( errorCode ) {
            case TAMPERED_DATA_DETECTED:
            case AUTHENTICATION_FAILED:
            case INVALID_CIPHER_TYPE:
                return true; // These indicate potential security breaches
                
            default:
                return false;
        }
    }
}