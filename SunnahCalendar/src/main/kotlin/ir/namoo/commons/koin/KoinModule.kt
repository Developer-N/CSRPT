package ir.namoo.commons.koin

import android.content.Context
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import ir.namoo.commons.model.AthanDB
import ir.namoo.commons.model.AthanSettingsDB
import ir.namoo.commons.model.LocationsDB
import ir.namoo.commons.repository.PrayTimeRepository
import ir.namoo.commons.service.PrayTimesService
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
import kotlinx.serialization.json.Json
import okhttp3.Cache
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import timber.log.Timber

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
            defaultRequest {
//                header("APIKEY", API_KEY)
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
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Timber.d(message)
                    }
                }
            }
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

}//end of module
