package com.cdi.moonphase.di

import javax.inject.Qualifier

/** Marks the CPU-bound dispatcher used for phase math. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher

/** Marks the IO dispatcher used for DataStore / platform calls. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher
