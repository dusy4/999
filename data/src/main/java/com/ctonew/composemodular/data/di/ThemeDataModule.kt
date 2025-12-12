package com.ctonew.composemodular.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.ctonew.composemodular.data.theme.DataStoreThemeRepository
import com.ctonew.composemodular.data.theme.settingsDataStore
import com.ctonew.composemodular.domain.theme.ThemeRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ThemeDataModule {

    @Binds
    @Singleton
    abstract fun bindThemeRepository(impl: DataStoreThemeRepository): ThemeRepository

    companion object {

        @Provides
        @Singleton
        fun provideSettingsDataStore(
            @ApplicationContext context: Context,
        ): DataStore<Preferences> = context.settingsDataStore
    }
}
