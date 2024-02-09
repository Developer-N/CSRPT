package ir.namoo.quran.player

import android.content.ContentResolver
import android.content.Context
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.formatNumber
import ir.namoo.commons.utils.appPrefsLite
import ir.namoo.quran.chapters.data.ChapterEntity
import ir.namoo.quran.qari.QariEntity
import ir.namoo.quran.qari.getQariLocalPhotoFile
import ir.namoo.quran.utils.DEFAULT_PLAY_TYPE
import ir.namoo.quran.utils.DEFAULT_SELECTED_QARI
import ir.namoo.quran.utils.DEFAULT_TRANSLATE_TO_PLAY
import ir.namoo.quran.utils.PREF_PLAY_TYPE
import ir.namoo.quran.utils.PREF_SELECTED_QARI
import ir.namoo.quran.utils.PREF_TRANSLATE_TO_PLAY
import ir.namoo.quran.utils.getAyaFileName
import ir.namoo.quran.utils.getQuranDirectoryInInternal
import ir.namoo.quran.utils.getQuranDirectoryInSD
import ir.namoo.quran.utils.getSuraFileName
import net.lingala.zip4j.ZipFile
import java.io.File

fun getPlayList(
    context: Context,
    sura: Int,
    aya: Int,
    folderName: String,
    translateFolderName: String,
    qariList: List<QariEntity>,
    chapter: ChapterEntity?
): List<MediaItem> {
    val result = mutableListOf<MediaItem>()
    val playType = context.appPrefsLite.getInt(PREF_PLAY_TYPE, DEFAULT_PLAY_TYPE)
    val q = qariList.find { folderName.contains(it.folderName) }
    val t = qariList.find { translateFolderName.contains(it.folderName) }
    if ((sura != 1 && sura != 9) && aya == 1 && context.appPrefsLite.getInt(
            PREF_PLAY_TYPE, DEFAULT_PLAY_TYPE
        ) != 3
    ) {
        result.add(
            MediaItem.Builder().setMediaMetadata(
                MediaMetadata.Builder().setTitle(
                    " 📖 " + context.getString(R.string.str_bismillah) + " 📖 " + formatNumber(
                        sura
                    ) + "|" + formatNumber(1)
                ).setArtist(q?.name)
                    .setArtworkUri(context.getQariLocalPhotoFile(q?.photoLink?.trim())?.toUri())
                    .build()
            ).setUri(
                when {
                    File(
                        "$folderName/" + getAyaFileName(
                            sura, 0
                        )
                    ).exists() -> ("$folderName/" + getAyaFileName(sura, 0)).toUri()

                    File(
                        "$folderName/" + getAyaFileName(
                            1, 1
                        )
                    ).exists() -> ("$folderName/" + getAyaFileName(1, 1)).toUri()

                    else -> (ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.resources.getResourcePackageName(
                        R.raw.bismillah
                    ) + "/" + context.resources.getResourceTypeName(R.raw.bismillah) + "/" + context.resources.getResourceEntryName(
                        R.raw.bismillah
                    )).toUri()
                }
            ).build()
        )
    }

    for (i in aya..(chapter?.ayaCount ?: aya)) {
        val title = " 📖 " + chapter?.nameArabic + " 📖 " + formatNumber(sura) + " | " + formatNumber(
            i
        )
        when (playType) {
            1 -> {
                result.add(
                    MediaItem.Builder().setMediaMetadata(
                        MediaMetadata.Builder().setTitle(title).setArtist(q?.name).setArtworkUri(
                            context.getQariLocalPhotoFile(q?.photoLink?.trim())?.toUri()
                        ).build()
                    ).setUri(("$folderName/" + getAyaFileName(sura, i)).toUri()).build()
                )
                result.add(
                    MediaItem.Builder().setMediaMetadata(
                        MediaMetadata.Builder().setTitle(title).setArtist(t?.name).setArtworkUri(
                            context.getQariLocalPhotoFile(t?.photoLink?.trim())?.toUri()
                        ).build()
                    ).setUri(("$translateFolderName/" + getAyaFileName(sura, i)).toUri()).build()
                )

            }

            2 -> {
                result.add(
                    MediaItem.Builder().setMediaMetadata(
                        MediaMetadata.Builder().setTitle(title).setArtist(q?.name).setArtworkUri(
                            context.getQariLocalPhotoFile(q?.photoLink?.trim())?.toUri()
                        ).build()
                    ).setUri(("$folderName/" + getAyaFileName(sura, i)).toUri()).build()
                )
            }

            else -> {
                result.add(
                    MediaItem.Builder().setMediaMetadata(
                        MediaMetadata.Builder().setTitle(title).setArtist(t?.name).setArtworkUri(
                            context.getQariLocalPhotoFile(t?.photoLink?.trim())?.toUri()
                        ).build()
                    ).setUri(("$translateFolderName/" + getAyaFileName(sura, i)).toUri()).build()
                )

            }
        }
    }
    return result
}

fun isQuranDownloaded(context: Context, sura: Int): Boolean {
    if (context.appPrefsLite.getInt(PREF_PLAY_TYPE, DEFAULT_PLAY_TYPE) == 3) return true
    if (File(
            getQuranDirectoryInInternal(context) + File.separator + context.appPrefsLite.getString(
                PREF_SELECTED_QARI, DEFAULT_SELECTED_QARI
            ) + "/" + getAyaFileName(sura, 1)
        ).exists()
    ) return true
    if (File(
            getQuranDirectoryInSD(context) + File.separator + context.appPrefsLite.getString(
                PREF_SELECTED_QARI, DEFAULT_SELECTED_QARI
            ) + "/" + getAyaFileName(sura, 1)
        ).exists()
    ) return true
    //if zip file is exist extract it and then recheck
    val internalZip =
        getQuranDirectoryInInternal(context) + File.separator + context.appPrefsLite.getString(
            PREF_SELECTED_QARI, DEFAULT_SELECTED_QARI
        ) + File.separator + getSuraFileName(
            sura
        )
    File(internalZip).let {
        if (it.exists()) {
            ZipFile(it).extractAll(it.parent)
            it.delete()
        }
    }

    if (File(
            getQuranDirectoryInInternal(context) + File.separator + context.appPrefsLite.getString(
                PREF_SELECTED_QARI, DEFAULT_SELECTED_QARI
            ) + "/" + getAyaFileName(sura, 1)
        ).exists()
    ) return true

    val externalZip =
        getQuranDirectoryInSD(context) + File.separator + context.appPrefsLite.getString(
            PREF_SELECTED_QARI, DEFAULT_SELECTED_QARI
        ) + File.separator + getSuraFileName(
            sura
        )
    File(externalZip).let {
        if (it.exists()) {
            ZipFile(it).extractAll(it.parent)
            it.delete()
        }
    }

    return File(
        getQuranDirectoryInSD(context) + File.separator + context.appPrefsLite.getString(
            PREF_SELECTED_QARI, DEFAULT_SELECTED_QARI
        ) + "/" + getAyaFileName(sura, 1)
    ).exists()
}

fun isTranslateDownloaded(context: Context, sura: Int): Boolean {
    if (context.appPrefsLite.getInt(PREF_PLAY_TYPE, DEFAULT_PLAY_TYPE) == 2) return true
    if (File(
            getQuranDirectoryInInternal(context) + "/" + context.appPrefsLite.getString(
                PREF_TRANSLATE_TO_PLAY, DEFAULT_TRANSLATE_TO_PLAY
            ) + "/" + getAyaFileName(sura, 1)
        ).exists()
    ) return true

    if (File(
            getQuranDirectoryInSD(context) + "/" + context.appPrefsLite.getString(
                PREF_TRANSLATE_TO_PLAY, DEFAULT_TRANSLATE_TO_PLAY
            ) + "/" + getAyaFileName(sura, 1)
        ).exists()
    ) return true
    //if zip file is exist extract it and then recheck
    val internalZip =
        getQuranDirectoryInInternal(context) + File.separator + context.appPrefsLite.getString(
            PREF_TRANSLATE_TO_PLAY, DEFAULT_TRANSLATE_TO_PLAY
        ) + File.separator + getSuraFileName(
            sura
        )
    File(internalZip).let {
        if (it.exists()) {
            ZipFile(it).extractAll(it.parent)
            it.delete()
        }
    }
    if (File(
            getQuranDirectoryInInternal(context) + "/" + context.appPrefsLite.getString(
                PREF_TRANSLATE_TO_PLAY, DEFAULT_TRANSLATE_TO_PLAY
            ) + "/" + getAyaFileName(sura, 1)
        ).exists()
    ) return true

    val externalZip =
        getQuranDirectoryInSD(context) + File.separator + context.appPrefsLite.getString(
            PREF_TRANSLATE_TO_PLAY, DEFAULT_TRANSLATE_TO_PLAY
        ) + File.separator + getSuraFileName(
            sura
        )
    File(externalZip).let {
        if (it.exists()) {
            ZipFile(it).extractAll(it.parent)
            it.delete()
        }
    }
    return File(
        getQuranDirectoryInSD(context) + "/" + context.appPrefsLite.getString(
            PREF_TRANSLATE_TO_PLAY, DEFAULT_TRANSLATE_TO_PLAY
        ) + "/" + getAyaFileName(sura, 1)
    ).exists()
}
