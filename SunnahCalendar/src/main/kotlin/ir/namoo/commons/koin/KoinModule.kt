package ir.namoo.commons.koin

import android.content.Context
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import ir.namoo.commons.model.AthanDB
import ir.namoo.commons.model.AthanSettingsDB
import ir.namoo.commons.model.LocationsDB
import ir.namoo.commons.repository.PrayTimeRepository
import ir.namoo.commons.service.PrayTimesService
import ir.namoo.commons.utils.KtorUtils
import ir.namoo.commons.utils.appPrefsLite
import ir.namoo.quran.db.FileDownloadDB
import ir.namoo.quran.db.FileDownloadRepository
import ir.namoo.quran.db.QuranDB
import ir.namoo.quran.ui.SuraViewModel
import ir.namoo.quran.ui.fragments.BookmarkViewModel
import ir.namoo.quran.ui.fragments.NoteViewModel
import ir.namoo.quran.ui.fragments.QuranViewModel
import ir.namoo.quran.viewmodels.ChapterViewModel
import ir.namoo.quran.viewmodels.DownloadViewModel
import ir.namoo.religiousprayers.praytimeprovider.DownloadedPrayTimesDB
import ir.namoo.religiousprayers.praytimeprovider.PrayTimesDB
import ir.namoo.religiousprayers.ui.azkar.AzkarActivityViewModel
import ir.namoo.religiousprayers.ui.azkar.AzkarDB
import ir.namoo.religiousprayers.ui.azkar.AzkarRepository
import ir.namoo.religiousprayers.ui.azkar.AzkarViewModel
import ir.namoo.religiousprayers.ui.downloadtimes.DownloadPrayTimesViewModel
import ir.namoo.religiousprayers.ui.edit.EditViewModel
import ir.namoo.religiousprayers.ui.monthly.MonthlyViewModel
import okhttp3.Cache
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val koinModule = module {
    // Quran
    single { QuranDB.getInstance(get()) }
    single { FileDownloadDB.getInstance(get()) }
    factory { get<Context>().appPrefsLite }

    // DatabaseModules
    single { DownloadedPrayTimesDB.getInstance(get()) }
    single { get<DownloadedPrayTimesDB>().downloadedPrayTimes() }
    single { PrayTimesDB.getInstance(get()) }
    single { get<PrayTimesDB>().prayTimes() }
    single { AzkarDB.getInstance(get()) }
    single { AzkarRepository(get()) }
    single { LocationsDB.getInstance(get()) }
    single { AthanDB.getInstance(get()) }
    single { AthanSettingsDB.getInstance(get()) }
    single { PrayTimesService(get()) }
    single { PrayTimeRepository(get()) }

    // NetworkModule
    factory { Cache(get<Context>().cacheDir, 10L * 1024 * 1024) }
    single { CacheInterceptor() }
    single {
        HttpClient(OkHttp) {
            engine {
                config {
                    cache(get())
                }
                addNetworkInterceptor(get<CacheInterceptor>())
            }
            KtorUtils.configureHttpClient(this)
        }
    }

    // viewModels
    viewModel { SuraViewModel(get()) }
    viewModel { BookmarkViewModel(get()) }
    viewModel { ChapterViewModel(get()) }
    viewModel { NoteViewModel(get()) }
    viewModel { QuranViewModel(get()) }
    factory { FileDownloadRepository(get<FileDownloadDB>().getFileDownloadDao()) }
    viewModel { DownloadViewModel(get(), get()) }
    viewModel { AzkarViewModel(get()) }
    viewModel { AzkarActivityViewModel(get()) }
    viewModel { DownloadPrayTimesViewModel(get(), get(), get()) }
    viewModel { EditViewModel(get()) }
    viewModel { MonthlyViewModel() }

}//end of module
