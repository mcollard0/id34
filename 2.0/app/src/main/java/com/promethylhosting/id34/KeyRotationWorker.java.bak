package com.promethylhosting.id34;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import timber.log.Timber;

/**
 * KeyRotationWorker - Background worker for automatic cryptographic key rotation
 * 
 * Executes every 30 days to rotate master keys for Perfect Forward Secrecy.
 * Ensures that compromised old keys cannot decrypt new data.
 * 
 * Security Features:
 * - Automatic key generation and rotation
 * - Legacy key cleanup after successful rotation
 * - Failure handling with retry logic
 * - No sensitive data in logs
 * 
 * @author ID34 Security Team
 * @version 1.0
 * @since 2025-09-13
 */
public class KeyRotationWorker extends Worker {
    
    public KeyRotationWorker( @NonNull Context context, @NonNull WorkerParameters workerParams ) {
        super( context, workerParams );
    }
    
    @NonNull
    @Override
    public Result doWork() {
        Timber.i( "Starting scheduled key rotation..." );
        
        try {
            AdvancedCryptographyManager cryptoManager = AdvancedCryptographyManager.getInstance( getApplicationContext() );
            
            if ( cryptoManager.rotateKeys() ) {
                Timber.i( "Scheduled key rotation completed successfully" );
                return Result.success();
            } else {
                Timber.e( "Scheduled key rotation failed" );
                return Result.retry(); // Retry later
            }
            
        } catch ( Exception e ) {
            Timber.e( e, "Key rotation worker failed" );
            return Result.failure();
        }
    }
}