package ir.namoo.commons.koin

import android.content.Context
import com.google.android.gms.location.LocationServices
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import ir.namoo.commons.locationtracker.LocationTracker
import ir.namoo.commons.model.AthanDB
import ir.namoo.commons.model.AthanSettingsDB
import ir.namoo.commons.model.LocationsDB
import ir.namoo.commons.repository.LocalPrayTimeRepository
import ir.namoo.commons.repository.PrayTimeRepository
import ir.namoo.commons.repository.RemoteRepository
import ir.namoo.commons.utils.appPrefsLite
import ir.namoo.hadeeth.repository.HadeethDB
import ir.namoo.hadeeth.repository.HadeethLocalRepository
import ir.namoo.hadeeth.repository.HadeethOnlineRepository
import ir.namoo.hadeeth.repository.HadeethRepository
import ir.namoo.hadeeth.ui.chapter.HadeethChapterViewModel
import ir.namoo.hadeeth.ui.hadeeth.HadeethViewModel
import ir.namoo.quran.QuranActivityViewModel
import ir.namoo.quran.TawhidDB
import ir.namoo.quran.bookmarks.BookmarkViewModel
import ir.namoo.quran.chapters.ChapterViewModel
import ir.namoo.quran.chapters.data.ChapterRepository
import ir.namoo.quran.db.FileDownloadDB
import ir.namoo.quran.db.FileDownloadRepository
import ir.namoo.quran.db.LastVisitedDB
import ir.namoo.quran.db.LastVisitedPageDB
import ir.namoo.quran.db.LastVisitedRepository
import ir.namoo.quran.db.QCFDatabase
import ir.namoo.quran.db.QPCDatabase
import ir.namoo.quran.db.QuranDB
import ir.namoo.quran.download.DownloadQuranAudioViewModel
import ir.namoo.quran.download.QuranDownloader
import ir.namoo.quran.home.QuranDownloadViewModel
import ir.namoo.quran.mushaf.MushafFileDownloaderViewModel
import ir.namoo.quran.mushaf.MushafPageViewModel
import ir.namoo.quran.mushaf.MushafViewModel
import ir.namoo.quran.notes.NotesViewModel
import ir.namoo.quran.qari.QariDB
import ir.namoo.quran.qari.QariRepository
import ir.namoo.quran.qari.RemoteQariRepository
import ir.namoo.quran.search.SearchViewModel
import ir.namoo.quran.settings.ReorderTranslatesViewModel
import ir.namoo.quran.settings.SettingViewModel
import ir.namoo.quran.settings.data.QuranSettingDB
import ir.namoo.quran.settings.data.QuranSettingRepository
import ir.namoo.quran.sura.SuraViewModel
import ir.namoo.quran.sura.data.QuranRepository
import ir.namoo.religiousprayers.praytimeprovider.DownloadedPrayTimesDB
import ir.namoo.religiousprayers.praytimeprovider.PrayTimeProvider
import ir.namoo.religiousprayers.praytimeprovider.PrayTimesDB
import ir.namoo.religiousprayers.ui.azkar.AzkarActivityViewModel
import ir.namoo.religiousprayers.ui.azkar.AzkarViewModel
import ir.namoo.religiousprayers.ui.azkar.data.AzkarDB
import ir.namoo.religiousprayers.ui.azkar.data.AzkarRepository
import ir.namoo.religiousprayers.ui.calendar.NTimesViewModel
import ir.namoo.religiousprayers.ui.downloadtimes.DownloadPrayTimesViewModel
import ir.namoo.religiousprayers.ui.edit.EditPrayTimeViewModel
import ir.namoo.religiousprayers.ui.intro.IntroCustomLocationViewModel
import ir.namoo.religiousprayers.ui.intro.IntroDownloadViewModel
import ir.namoo.religiousprayers.ui.settings.athan.AthanDownloadDialogViewModel
import ir.namoo.religiousprayers.ui.settings.athan.AthanSettingsViewModel
import ir.namoo.religiousprayers.ui.settings.location.LocationSettingViewModel
import kotlinx.serialization.json.Json
import okhttp3.Cache
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val koinModule = module {
    // Quran
    single { ChapterRepository(get(), get()) }
    single { QuranDB.getInstance(get()) }
    single { QCFDatabase.getInstance(get()) }
    single { QPCDatabase.getInstance(get()) }
    single { QuranRepository(get(), get(), get()) }
    single { QuranSettingDB.getInstance(get()) }
    single { QuranSettingRepository(get()) }
    single { QariDB.getInstance(get()) }
    single { RemoteQariRepository(get()) }
    single { QariRepository(get(), get(), get()) }
    single { QuranDownloader(get()) }
    single { FileDownloadDB.getInstance(get()) }
    single { LastVisitedDB.getInstance(get()) }
    single { LastVisitedPageDB.getInstance(get()) }
    single { LastVisitedRepository(get(), get()) }

    single { TawhidDB.getInstance(get()) }

    factory { get<Context>().appPrefsLite }

    // DatabaseModules
    single { DownloadedPrayTimesDB.getInstance(get()) }
    single { get<DownloadedPrayTimesDB>().downloadedPrayTimes() }
    single { PrayTimesDB.getInstance(get()) }
    single { get<PrayTimesDB>().prayTimes() }
    single { AzkarDB.getInstance(get()) }
    single { AzkarRepository(get()) }
    single { LocationsDB.getInstance(get()) }
    single { PrayTimeRepository(get(), get()) }
    single { AthanDB.getInstance(get()) }
    single { AthanSettingsDB.getInstance(get()) }
    single { RemoteRepository(get()) }
    single { LocalPrayTimeRepository(get(), get(), get()) }

    single { PrayTimeProvider(get(), get(), get()) }

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
            install(HttpTimeout) {
                requestTimeoutMillis = 30 * 1000
                connectTimeoutMillis = 30 * 1000
                socketTimeoutMillis = 30 * 1000
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
    }

    //Location
    single { LocationServices.getFusedLocationProviderClient(get<Context>().applicationContext) }
    single { LocationTracker(get(), get()) }

    // viewModels
    //quran
    viewModel { QuranActivityViewModel(get(), get()) }
    viewModel { QuranDownloadViewModel(get()) }
    viewModel { ChapterViewModel(get(), get(), get()) }
    viewModel { SuraViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { SettingViewModel(get(), get(), get()) }
    viewModel { BookmarkViewModel(get(), get()) }
    viewModel { NotesViewModel(get(), get()) }
    viewModel { SearchViewModel(get(), get(), get()) }
    viewModel { DownloadQuranAudioViewModel(get(), get(), get(), get()) }
    viewModel { ReorderTranslatesViewModel(get()) }
    viewModel { MushafViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { MushafPageViewModel(get(), get()) }
    viewModel { MushafFileDownloaderViewModel() }
    factory { FileDownloadRepository(get<FileDownloadDB>().getFileDownloadDao()) }

    //calendar
    viewModel { AzkarViewModel(get(), get()) }
    viewModel { AzkarActivityViewModel(get(), get()) }
    viewModel { DownloadPrayTimesViewModel(get(), get(), get()) }
    viewModel { EditPrayTimeViewModel(get()) }
    viewModel { IntroDownloadViewModel(get()) }
    viewModel { IntroCustomLocationViewModel(get(), get()) }

    viewModel { LocationSettingViewModel(get(), get()) }

    viewModel { AthanDownloadDialogViewModel(get(), get()) }
    viewModel { AthanSettingsViewModel(get(), get()) }

    viewModel { NTimesViewModel(get(), get()) }


    // Hadeeth
    single { HadeethDB.getInstance(get()) }
    single { HadeethLocalRepository(get()) }
    single { HadeethOnlineRepository(get()) }
    single { HadeethRepository(get(), get()) }
    viewModel { HadeethChapterViewModel(get()) }
    viewModel { HadeethViewModel(get()) }

}//end of module
