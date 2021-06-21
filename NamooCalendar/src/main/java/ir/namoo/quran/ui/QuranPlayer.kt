package ir.namoo.quran.ui

import android.app.Service
import android.content.*
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.net.toUri
import ir.namoo.quran.db.ChapterEntity
import ir.namoo.quran.db.QuranDB
import ir.namoo.quran.utils.*
import ir.namoo.religiousprayers.R
import ir.namoo.religiousprayers.utils.TAG
import ir.namoo.religiousprayers.utils.appPrefsLite
import ir.namoo.religiousprayers.utils.applyAppLanguage
import ir.namoo.religiousprayers.utils.logException
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
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            if (p1 != null) {
//                sura = p1.extras?.getInt("sura") ?: sura
//                chapter = QuranDB.getInstance(this@QuranPlayer).chaptersDao().getChapter(sura)
                if (p1.extras?.get("aya") != null)
                    aya = p1.extras?.getInt("aya") ?: aya
                Log.e(
                    TAG,
                    "onReceive: action=${p1.extras?.getString("action")} -->>  and -->> aya=$aya"
                )
                when (p1.extras?.get("action")) {
                    NOTIFY_QURAN_PLAY -> {//get Sura and Aya and play and notify notification and send broadcast to sura view
                        play(sura, aya)
                    }
                    NOTIFY_QURAN_NEXT -> {//play next and notify notification and send broadcast to sura view
                        play(sura, aya + 1)
                    }
                    NOTIFY_QURAN_PREVIOUS -> {//play previous and notify notification and send broadcast to sura view
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

        private fun play(sura: Int, aya: Int) {
            this@QuranPlayer.aya = aya
            mediaPlayer?.let {
                if (it.isPlaying)
                    it.stop()
                it.release()
            }
            if (!File((folderName + "/" + getAyaFileName(sura, aya))).exists())
                loadFolders(getAyaFileName(sura, aya))
            mediaPlayer = MediaPlayer().apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                } else {
                    @Suppress("DEPRECATION")
                    setAudioStreamType(AudioManager.STREAM_MUSIC)
                }
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
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                setAudioAttributes(
                                    AudioAttributes.Builder()
                                        .setUsage(AudioAttributes.USAGE_MEDIA)
                                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                        .build()
                                )
                            } else {
                                @Suppress("DEPRECATION")
                                setAudioStreamType(AudioManager.STREAM_MUSIC)
                            }
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
            if (!File((translateFolderName + "/" + getAyaFileName(sura, aya))).exists()) loadFolders(
                getAyaFileName(sura, aya)
            )
            mediaPlayer = MediaPlayer().apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                } else {
                    @Suppress("DEPRECATION")
                    setAudioStreamType(AudioManager.STREAM_MUSIC)
                }
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
            chapter = QuranDB.getInstance(this@QuranPlayer).chaptersDao().getChapter(sura)
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