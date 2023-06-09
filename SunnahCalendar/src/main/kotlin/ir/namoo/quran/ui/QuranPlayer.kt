package ir.namoo.quran.ui

import android.app.Service
import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.IBinder
import androidx.core.net.toUri
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.byagowi.persiancalendar.utils.logException
import ir.namoo.commons.utils.appPrefsLite
import ir.namoo.quran.db.ChapterEntity
import ir.namoo.quran.db.QuranDB
import ir.namoo.quran.utils.DEFAULT_PLAY_TYPE
import ir.namoo.quran.utils.DEFAULT_SELECTED_QARI
import ir.namoo.quran.utils.DEFAULT_TRANSLATE_TO_PLAY
import ir.namoo.quran.utils.NOTIFY_QURAN_NEXT
import ir.namoo.quran.utils.NOTIFY_QURAN_PAUSE
import ir.namoo.quran.utils.NOTIFY_QURAN_PLAY
import ir.namoo.quran.utils.NOTIFY_QURAN_PREVIOUS
import ir.namoo.quran.utils.NOTIFY_QURAN_RESUME
import ir.namoo.quran.utils.NOTIFY_QURAN_STOP
import ir.namoo.quran.utils.PREF_PLAY_TYPE
import ir.namoo.quran.utils.PREF_SELECTED_QARI
import ir.namoo.quran.utils.PREF_TRANSLATE_TO_PLAY
import ir.namoo.quran.utils.QURAN_NOTIFY_VIEW_PLAYER_PAUSE
import ir.namoo.quran.utils.QURAN_NOTIFY_VIEW_PLAYER_PLAY
import ir.namoo.quran.utils.QURAN_NOTIFY_VIEW_PLAYER_RESUME
import ir.namoo.quran.utils.QURAN_NOTIFY_VIEW_PLAYER_STOP
import ir.namoo.quran.utils.QURAN_PLAYER_ACTION
import ir.namoo.quran.utils.QURAN_VIEW_PLAYER_ACTION
import ir.namoo.quran.utils.getAyaFileName
import ir.namoo.quran.utils.getQuranDirectoryInInternal
import ir.namoo.quran.utils.getQuranDirectoryInSD
import org.koin.android.ext.android.inject
import java.io.File

//private const val NOTIFICATION_ID = 63
class QuranPlayer : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private var sura = 1
    private var aya = 1
    private var length = 0
    private var chapter: ChapterEntity? = null
    private lateinit var folderName: String
    private lateinit var translateFolderName: String
    private var playType = 1

    private val db: QuranDB by inject()
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
//                sura = p1.extras?.getInt("sura") ?: sura
//                chapter = QuranDB.getInstance(this@QuranPlayer).chaptersDao().getChapter(sura)
                aya = intent.getIntExtra("aya", aya)
                when (intent.getStringExtra("action")) {
                    NOTIFY_QURAN_PLAY -> {//get Sura and Aya and play and notify notification and send broadcast to sura view
                        play(sura, aya)
                    }

                    NOTIFY_QURAN_NEXT -> {//play next and notify notification and send broadcast to sura view
                        play(sura, aya + 1)
                    }

                    NOTIFY_QURAN_PREVIOUS -> {//play previous and notify notification and send broadcast to sura view
                        if (aya > 1)
                            play(sura, aya - 1)
                    }

                    NOTIFY_QURAN_PAUSE -> {//pause and notify notification and send broadcast to sura view
                        length = mediaPlayer?.currentPosition ?: 0
                        mediaPlayer?.pause()
                        sendBroadcast(Intent(QURAN_VIEW_PLAYER_ACTION).apply {
                            putExtra("action", QURAN_NOTIFY_VIEW_PLAYER_PAUSE)
                        })
                    }

                    NOTIFY_QURAN_RESUME -> {//pause and notify notification and send broadcast to sura view
                        mediaPlayer?.apply {
                            seekTo(length)
                            start()
                        }
                        sendBroadcast(Intent(QURAN_VIEW_PLAYER_ACTION).apply {
                            putExtra("action", QURAN_NOTIFY_VIEW_PLAYER_RESUME)
                        })
                    }

                    NOTIFY_QURAN_STOP -> {// stop and remove notification and broadcast to sura view
//                        clearNotification()
                        sendBroadcast(Intent(QURAN_VIEW_PLAYER_ACTION).apply {
                            putExtra("action", QURAN_NOTIFY_VIEW_PLAYER_STOP)
                        })
                        mediaPlayer?.stop()
                    }
                }
            }
        }

        private fun play(s: Int, a: Int) {
            aya = if (a < 1) 1 else a
            sura = s
            mediaPlayer?.let {
                if (it.isPlaying)
                    it.stop()
                it.release()
            }
            if (!File((folderName + "/" + getAyaFileName(sura, aya))).exists())
                loadFolders(getAyaFileName(sura, aya))
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
            }
            if (aya > chapter?.ayaCount!!) return
//            showNotification(sura, aya)
            sendBroadcast(Intent(QURAN_VIEW_PLAYER_ACTION).apply {
                putExtra("action", QURAN_NOTIFY_VIEW_PLAYER_PLAY)
                putExtra("sura", sura)
                putExtra("aya", aya)
            })
            if (playType == 3)
                playTranslate(sura, aya)
            else if ((sura != 1 && sura != 9) && aya == 1) {//first Play Bismillah
                mediaPlayer?.apply {
                    when {
                        File(folderName + "/" + getAyaFileName(sura, 0)).exists() ->
                            setDataSource(
                                this@QuranPlayer,
                                (folderName + "/" + getAyaFileName(sura, 0)).toUri()
                            )

                        File(folderName + "/" + getAyaFileName(1, 1)).exists() ->
                            setDataSource(
                                this@QuranPlayer,
                                (folderName + "/" + getAyaFileName(1, 1)).toUri()
                            )

                        else -> setDataSource(
                            this@QuranPlayer,
                            (ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                                    resources.getResourcePackageName(R.raw.bismillah) + "/" +
                                    resources.getResourceTypeName(R.raw.bismillah) + "/" +
                                    resources.getResourceEntryName(R.raw.bismillah)).toUri()
                        )
                    }
                    setOnCompletionListener {
                        mediaPlayer = MediaPlayer().apply {
                            setAudioAttributes(
                                AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_MEDIA)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .build()
                            )
                            setDataSource(
                                this@QuranPlayer,
                                (folderName + "/" + getAyaFileName(sura, aya)).toUri()
                            )
                            setOnCompletionListener {
                                if (playType != 2)
                                    playTranslate(sura, aya)
                                else
                                    chapter?.let {
                                        if (it.ayaCount!! > aya)
                                            play(sura, aya + 1)
                                        else
                                            sendBroadcast(Intent(QURAN_VIEW_PLAYER_ACTION).apply {
                                                putExtra(
                                                    "action",
                                                    QURAN_NOTIFY_VIEW_PLAYER_STOP
                                                )
                                            })
                                    }
                            }
                            prepareAsync()
                            setOnPreparedListener {
                                start()
                            }
                        }
                    }
                    prepareAsync()
                    setOnPreparedListener {
                        start()
                    }

                }
            } else
                mediaPlayer?.apply {
                    setDataSource(
                        this@QuranPlayer,
                        (folderName + "/" + getAyaFileName(sura, aya)).toUri()
                    )
                    setOnCompletionListener {
                        if (playType != 2)
                            playTranslate(sura, aya)
                        else
                            chapter?.let {
                                if (it.ayaCount!! > aya)
                                    play(sura, aya + 1)
                                else
                                    sendBroadcast(Intent(QURAN_VIEW_PLAYER_ACTION).apply {
                                        putExtra("action", QURAN_NOTIFY_VIEW_PLAYER_STOP)
                                    })
                            }
                    }
                    prepareAsync()
                    setOnPreparedListener {
                        start()
                    }
                }

        }

        private fun playTranslate(sura: Int, aya: Int) {
            if (playType == 2) return
            if (!File(("$translateFolderName/" + getAyaFileName(sura, aya))).exists())
                loadFolders(getAyaFileName(sura, aya))
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(
                    this@QuranPlayer,
                    (translateFolderName + "/" + getAyaFileName(sura, aya)).toUri()
                )
                setOnCompletionListener {
                    chapter?.let {
                        if (it.ayaCount!! > aya)
                            play(sura, aya + 1)
                        else
                            sendBroadcast(Intent(QURAN_VIEW_PLAYER_ACTION).apply {
                                putExtra("action", QURAN_NOTIFY_VIEW_PLAYER_STOP)
                            })
                    }
                }
                prepareAsync()
                setOnPreparedListener {
                    start()
                }
            }
        }
    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        applyAppLanguage(this)
        if (intent != null) {
            sura = intent.extras?.getInt("sura") ?: 1
            chapter = db.chaptersDao().getChapter(sura)
            aya = intent.extras?.getInt("aya") ?: 1
        }
        return START_STICKY
    }

//    private fun showNotification(sura: Int, aya: Int) {
//        applyAppLanguage(this)
//        val suraName =
//            QuranDB.getInstance(applicationContext).chaptersDao().getChapter(sura).nameArabic
//        var title = formatNumber(
//            getString(R.string.sura) + " " + suraName + " " + getString(R.string.aya) + " " + aya
//        )
//        if (isRTL(this))
//            title = RLM + title
//
//        val notificationManager = getSystemService<NotificationManager>()
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val importance = NotificationManager.IMPORTANCE_LOW
//            val channel = NotificationChannel(
//                NOTIFICATION_ID.toString(),
//                getString(R.string.app_name), importance
//            )
//            channel.setShowBadge(false)
//            notificationManager?.createNotificationChannel(channel)
//        }
//        val builder = NotificationCompat.Builder(this, NOTIFICATION_ID.toString()).apply {
//            priority = NotificationCompat.PRIORITY_LOW
//            setSmallIcon(R.drawable.icon_app)
//            setOngoing(true)
//            setWhen(0)
//            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
//            setColorized(true)
//            setContentTitle(title)
//
//        }
//        notificationManager?.notify(NOTIFICATION_ID, builder.build())
//    }//end of showNotification

//    private fun clearNotification() {
//        getSystemService<NotificationManager>()?.cancel(NOTIFICATION_ID)
//    }

    override fun onCreate() {
        super.onCreate()
        loadFolders(getAyaFileName(sura, aya))
        playType = appPrefsLite.getInt(PREF_PLAY_TYPE, DEFAULT_PLAY_TYPE)
        registerReceiver(receiver, IntentFilter(QURAN_PLAYER_ACTION))
    }

    private fun loadFolders(fileName: String) {
        folderName = if (File(
                getQuranDirectoryInInternal(this) + "/" + appPrefsLite.getString(
                    PREF_SELECTED_QARI,
                    DEFAULT_SELECTED_QARI
                ) + if (fileName.isNotEmpty()) "/$fileName" else ""
            ).exists()
        )
            getQuranDirectoryInInternal(this) + "/" + appPrefsLite.getString(
                PREF_SELECTED_QARI,
                DEFAULT_SELECTED_QARI
            )
        else
            getQuranDirectoryInSD(this) + "/" + appPrefsLite.getString(
                PREF_SELECTED_QARI,
                DEFAULT_SELECTED_QARI
            )

        translateFolderName = if (File(
                getQuranDirectoryInInternal(this) + "/" + appPrefsLite.getString(
                    PREF_TRANSLATE_TO_PLAY,
                    DEFAULT_TRANSLATE_TO_PLAY
                ) + if (fileName.isNotEmpty()) "/$fileName" else ""
            ).exists()
        )
            getQuranDirectoryInInternal(this) + "/" + appPrefsLite.getString(
                PREF_TRANSLATE_TO_PLAY,
                DEFAULT_TRANSLATE_TO_PLAY
            )
        else
            getQuranDirectoryInSD(this) + "/" + appPrefsLite.getString(
                PREF_TRANSLATE_TO_PLAY,
                DEFAULT_TRANSLATE_TO_PLAY
            )
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
//        clearNotification()
        runCatching {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.release()
                    it.stop()
                }
            }
        }.onFailure(logException)
    }

}//end of class
