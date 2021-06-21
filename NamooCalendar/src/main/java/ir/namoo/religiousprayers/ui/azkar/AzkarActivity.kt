package ir.namoo.religiousprayers.ui.azkar

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import ir.namoo.religiousprayers.R
import ir.namoo.religiousprayers.appLink
import ir.namoo.religiousprayers.databinding.ActivityAzkarBinding
import ir.namoo.religiousprayers.databinding.AzkarSubitemsBinding
import ir.namoo.religiousprayers.utils.*
import java.io.*
import java.net.HttpURLConnection
import java.net.URL


class AzkarActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAzkarBinding
    private lateinit var db: AzkarDB
    private var description = " \uD83E\uDD32\uD83C\uDFFB "
    private var mp: MediaPlayer? = null
    private lateinit var fileLocation: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fileLocation =
            getExternalFilesDir(null)?.absolutePath + File.separator + "azkar" + File.separator
        setTheme(getThemeFromName(getThemeFromPreference(this, appPrefs)))
        applyAppLanguage(this)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = ActivityAzkarBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            binding.azkarBarLayout.outlineProvider = null
        binding.txtAzkarTitle.typeface = getAppFont(binding.root.context)
        binding.azkarToolbar.title = getString(R.string.azkar)
        setSupportActionBar(binding.azkarToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        db = AzkarDB.getInstance(applicationContext)
        val id = intent.extras?.getInt("id")
        if (id != null) {
            binding.txtAzkarTitle.text = when (language) {
                "fa" -> db.azkarsDAO().getAzkarTitleFor(id.toInt()).title_fa
                "ckb" -> db.azkarsDAO().getAzkarTitleFor(id.toInt()).title_ku
                else -> db.azkarsDAO().getAzkarTitleFor(id.toInt()).title_en
            }
            binding.recyclerSubAzkars.layoutManager = LinearLayoutManager(this)
            binding.recyclerSubAzkars.adapter = AAdapter(db.azkarsDAO().getAzkarsFor(id))

//            binding.azkarActivityRoot.layoutAnimation =
//                AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_from_bottom)
            binding.recyclerSubAzkars.layoutAnimation =
                AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_from_bottom)
            //set description
            description += "${binding.txtAzkarTitle.text}\n"
            val azkars = db.azkarsDAO().getAzkarsFor(id)
            for (zkr in azkars) {
                description += when (language) {
                    "fa" -> "\n${zkr.title}\n" +
                            "${zkr.descryption_fa}\n" +
                            "------------------\n" +
                            "${zkr.info_fa}" +
                            "\n------------------\n"
                    "ckb" -> "${zkr.title}\n" +
                            "${zkr.descryption_ku}\n" +
                            "------------------\n" +
                            "${zkr.info_ku}" +
                            "\n------------------\n"
                    else -> "${zkr.title}\n" +
                            "${zkr.descryption_en}\n" +
                            "------------------\n" +
                            "${zkr.info_en}" +
                            "\n------------------\n"
                }
            }
            description += appLink
        }

    }//end of onCreate

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menu?.clear()
        menuInflater.inflate(R.menu.sub_azkar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sub_azkar_copy -> {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip: ClipData = ClipData.newPlainText(getString(R.string.azkar), description)
                clipboard.setPrimaryClip(clip)
                Snackbar.make(
                    binding.txtAzkarTitle,
                    getString(R.string.copied),
                    Snackbar.LENGTH_SHORT
                ).show()
                true
            }
            R.id.sub_azkar_share -> {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_TEXT, description)
                startActivity(Intent.createChooser(intent, resources.getString(R.string.share)))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private inner class AAdapter(val azkars: List<AzkarsEntity>) :
        RecyclerView.Adapter<AAdapter.AVH>() {
        private var lastPlay: AVH? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AVH {
            return AVH(AzkarSubitemsBinding.inflate(parent.context.layoutInflater, parent, false))
        }

        override fun getItemCount(): Int {
            return azkars.size
        }

        override fun getItemViewType(position: Int): Int {
            return position
        }

        override fun onBindViewHolder(holder: AVH, position: Int) {
            holder.bind(azkars[position])
        }

        //$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$444
        private inner class AVH(val binding: AzkarSubitemsBinding) :
            RecyclerView.ViewHolder(binding.root) {

            @SuppressLint("PrivateResource")
            fun bind(azkar: AzkarsEntity) {
                binding.title.text = azkar.title
                when (language) {
                    "fa" -> {
                        binding.description.text = azkar.descryption_fa
                        binding.info.text = azkar.info_fa
                    }
                    "ckb" -> {
                        binding.description.text = azkar.descryption_ku
                        binding.info.text = azkar.info_ku
                    }
                    else -> {
                        binding.description.text = azkar.descryption_en
                        binding.info.text = azkar.info_en
                    }
                }
                if (azkar.muzic == "ندارد")
                    binding.btnAzkarPlay.visibility = View.GONE
                val dir = File(fileLocation)
                if (!dir.exists())
                    dir.mkdirs()
                val fileName = azkar.muzic + ".mp3"
                val mp3File = File(fileLocation + fileName)
                if (!mp3File.exists())
                    binding.btnAzkarPlay.setImageResource(R.drawable.ic_download)
                binding.btnAzkarPlay.setOnClickListener {
                    it.startAnimation(
                        AnimationUtils.loadAnimation(
                            this@AzkarActivity,
                            androidx.appcompat.R.anim.abc_fade_in
                        )
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                        (ContextCompat.checkSelfPermission(
                            this@AzkarActivity,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                                != PackageManager.PERMISSION_GRANTED ||
                                ContextCompat.checkSelfPermission(
                                    this@AzkarActivity,
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                                )
                                != PackageManager.PERMISSION_GRANTED)
                    ) {
                        ActivityCompat.requestPermissions(
                            this@AzkarActivity, arrayOf(
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            ), 256
                        )
                    } else if (!mp3File.exists()) {
                        if (!isNetworkConnected(this@AzkarActivity)) {
                            val alert = AlertDialog.Builder(this@AzkarActivity)
                            alert.setTitle(resources.getString(R.string.network_error_title))
                            alert.setMessage(resources.getString(R.string.network_error_message))
                            alert.create().show()
                        } else {
                            val url = "https://archive.org/download/azkar_n/" + azkar.muzic + ".MP3"
                            DownloadTask(File(mp3File.absolutePath)).execute(url)
                        }
                    } else {
                        if (lastPlay != null) {
                            mp!!.stop()
                            mp!!.release()
                            mp = null
                            lastPlay!!.binding.btnAzkarPlay.setImageResource(R.drawable.ic_play)
                        }
                        if (lastPlay == null || (lastPlay != null && lastPlay != this)) {
                            lastPlay = this
                            play(mp3File)
                        } else {
                            lastPlay = null
                        }

                    }
                }
            }

            @SuppressLint("StaticFieldLeak")
            inner class DownloadTask(val file: File) : AsyncTask<String?, Int?, String?>() {

                private lateinit var mWakeLock: PowerManager.WakeLock

                override fun doInBackground(vararg sUrl: String?): String? {
                    var input: InputStream? = null
                    var output: OutputStream? = null
                    var connection: HttpURLConnection? = null
                    for (s in sUrl)
                        try {
                            val url = URL(s)
                            connection = url.openConnection() as HttpURLConnection
                            connection.connect()
                            // expect HTTP 200 OK, so we don't mistakenly save error report
                            // instead of the file
                            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                                return "Server returned HTTP ${connection.responseCode} ${connection.responseMessage}"
                            }
                            // this will be useful to display download percentage
                            // might be -1: server did not report the length
                            val fileLength = connection.contentLength
                            // download the file
                            input = connection.inputStream
                            output = FileOutputStream(file)
                            val data = ByteArray(4096)
                            var total = 0
                            var count: Int? = input.read(data)
                            while (count != -1) {
                                // allow canceling with back button
                                if (isCancelled) {
                                    input.close()
                                    return null
                                }
                                total += count!!
                                // publishing the progress....
                                if (fileLength > 0) // only if total length is known
                                    publishProgress(total)
                                output.write(data, 0, count)
                                count = input.read(data)
                            }
                        } catch (e: Exception) {
                            return e.toString()
                        } finally {
                            try {
                                output?.close()
                                input?.close()
                            } catch (ignored: IOException) {
                            }
                            connection?.disconnect()
                        }
                    return null
                }

                @SuppressLint("PrivateResource")
                override fun onPreExecute() {
                    super.onPreExecute()
                    // take CPU lock to prevent CPU from going off if the user
                    // presses the power button during download
                    val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
                    mWakeLock = pm.newWakeLock(
                        PowerManager.PARTIAL_WAKE_LOCK, AzkarActivity::class.java.name
                    )
                    mWakeLock.acquire(10 * 60 * 1000L /*10 minutes*/)
                    val animation = AnimationUtils.loadAnimation(
                        this@AzkarActivity,
                        androidx.appcompat.R.anim.abc_fade_in
                    )
                    animation.interpolator = LinearInterpolator()
                    animation.repeatCount = Animation.INFINITE
                    binding.btnAzkarPlay.startAnimation(animation)
                }

                override fun onPostExecute(result: String?) {
                    mWakeLock.release()
                    binding.btnAzkarPlay.clearAnimation()
                    binding.btnAzkarPlay.setImageResource(R.drawable.ic_play)
                    if (mp == null || !mp!!.isPlaying) {
                        lastPlay = this@AVH
                        play(file)
                    }
                }
            }//end of class TASK

            fun play(mp3File: File) {
                runCatching {
                    mp = MediaPlayer()
                    mp?.setDataSource(applicationContext, Uri.fromFile(mp3File))
                    mp?.prepare()
                    mp?.start()
                    binding.btnAzkarPlay.setImageResource(R.drawable.ic_stop)
                    mp?.setOnCompletionListener {
                        binding.btnAzkarPlay.setImageResource(R.drawable.ic_play)
                        lastPlay = null
                    }
                }.onFailure(logException)
            }
        }//end of view holder
    }//end of Adapter

    override fun onBackPressed() {
        runCatching {
            if (mp != null && mp!!.isPlaying) {
                mp!!.stop()
                mp!!.release()
                mp = null
            }
        }.onFailure(logException)
        super.onBackPressed()
    }

}//end of class