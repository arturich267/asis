package com.asis.virtualcompanion.common

/**
 * App constants
 */
object Constants {
    // Database
    const val DATABASE_NAME = "asis_database"
    const val DATABASE_VERSION = 1
    
    // TFLite
    const val TFLITE_MODEL_PATH = "models/"
    const val DEFAULT_MODEL_NAME = "default_model.tflite"
    
    // File Provider
    const val FILE_PROVIDER_AUTHORITY = "com.asis.virtualcompanion.fileprovider"
    
    // Work Manager
    const val SYNC_WORK_NAME = "sync_work"
    const val PROCESSING_WORK_NAME = "processing_work"
    
    // SharedPreferences
    const val PREFS_NAME = "asis_prefs"
    const val KEY_FIRST_LAUNCH = "first_launch"
    const val KEY_USER_SETTINGS = "user_settings"
    
    // Navigation
    const val NAV_MAIN = "main"
    const val NAV_SETTINGS = "settings"
}