package ir.namoo.quran.module
//
//import android.content.Context
//import android.content.SharedPreferences
//import androidx.room.Room
//import dagger.Module
//import dagger.Provides
//import dagger.hilt.InstallIn
//import dagger.hilt.android.qualifiers.ApplicationContext
//import dagger.hilt.components.SingletonComponent
//import ir.namoo.commons.utils.appPrefsLite
//import ir.namoo.quran.db.FileDownloadDAO
//import ir.namoo.quran.db.FileDownloadDB
//import ir.namoo.quran.db.QuranDB
//import javax.inject.Singleton
//
//@Module
//@InstallIn(SingletonComponent::class)
//object QuranModule {
//
//    @Provides
//    @Singleton
//    fun provideQuranDB(@ApplicationContext context: Context): QuranDB {
//        return Room.databaseBuilder(context, QuranDB::class.java, "quran.db")
//            .fallbackToDestructiveMigration().allowMainThreadQueries().build()
//    }
//
//    @Provides
//    @Singleton
//    fun provideFileDownloadDAO(@ApplicationContext context: Context): FileDownloadDAO {
//        return Room.databaseBuilder(context, FileDownloadDB::class.java, "fileDownload.db")
//            .fallbackToDestructiveMigration().allowMainThreadQueries().build().getFileDownloadDao()
//    }
//
//    @Provides
//    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
//        return context.appPrefsLite
//    }
//}
