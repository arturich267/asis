package com.asis.virtualcompanion.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Simple dependency injection for coroutine dispatchers
 * In a real app, you might want to use Hilt or Dagger
 */
@Singleton
class CoroutineModule @Inject constructor() {
    
    val main: CoroutineDispatcher = Dispatchers.Main
    val io: CoroutineDispatcher = Dispatchers.IO
    val default: CoroutineDispatcher = Dispatchers.Default
    val unconfined: CoroutineDispatcher = Dispatchers.Unconfined
}