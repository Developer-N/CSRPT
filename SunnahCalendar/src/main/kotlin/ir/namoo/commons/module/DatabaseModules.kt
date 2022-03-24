package ir.namoo.commons.module

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ir.namoo.commons.model.AthanDB
import ir.namoo.commons.model.AthanSettingsDB
import ir.namoo.commons.model.LocationsDB
import ir.namoo.religiousprayers.praytimeprovider.DownloadedPrayTimesDAO
import ir.namoo.religiousprayers.praytimeprovider.DownloadedPrayTimesDB
import ir.namoo.religiousprayers.praytimeprovider.PrayTimesDAO
import ir.namoo.religiousprayers.praytimeprovider.PrayTimesDB
import ir.namoo.religiousprayers.ui.azkar.AzkarDB
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModules {

    @Provides
    @Singleton
    fun provideDownloadedPrayTimeDB(@ApplicationContext context: Context): DownloadedPrayTimesDB =
        DownloadedPrayTimesDB.getInstance(context)

    @Provides
    fun provideDownloadedPrayTimeDAO(downloadedPrayTimesDB: DownloadedPrayTimesDB): DownloadedPrayTimesDAO =
        downloadedPrayTimesDB.downloadedPrayTimes()

    @Provides
    @Singleton
    fun providePrayTimeDB(@ApplicationContext context: Context): PrayTimesDB =
        PrayTimesDB.getInstance(context)

    @Provides
    fun providePrayTimeDAO(prayTimesDB: PrayTimesDB): PrayTimesDAO =
        prayTimesDB.prayTimes()

    @Provides
    @Singleton
    fun provideAzkarDB(@ApplicationContext context: Context): AzkarDB =
        Room.databaseBuilder(context, AzkarDB::class.java, "azkar.db")
            .fallbackToDestructiveMigration().allowMainThreadQueries().build()

    @Provides
    @Singleton
    fun provideLocationsDB(@ApplicationContext context: Context): LocationsDB =
        LocationsDB.getInstance(context)

    @Provides
    @Singleton
    fun provideAthanLocationDB(@ApplicationContext context: Context): AthanDB =
        AthanDB.getInstance(context)

    @Provides
    @Singleton
    fun provideAthanSettingsDB(@ApplicationContext context: Context): AthanSettingsDB =
        AthanSettingsDB.getInstance(context)

}
