package ir.namoo.religiousprayers.ui.settings.athan

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.byagowi.persiancalendar.R
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@Composable
fun AthanBackgroundSection(
    modifier: Modifier = Modifier,
    backgroundUri: String?,
    id: Int,
    onSelectBackground: (String?) -> Unit
) {
    val context = LocalContext.current
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let { u ->
                val newUri = copyPhotoToFolder(context, u, id) ?: return@let
                onSelectBackground(newUri.toString())
            }
        })

    Column(modifier = modifier) {
        Text(
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
            text = stringResource(id = R.string.athan_background),
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Row(modifier = Modifier.fillMaxWidth()) {

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                ElevatedButton(onClick = {
                    imagePicker.launch("image/*")
                })
                {
                    Text(
                        text = stringResource(id = R.string.select_from_gallery),
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = stringResource(R.string.select_from_gallery)
                    )
                }
                ElevatedButton(onClick = { onSelectBackground(null) }) {
                    Text(
                        text = stringResource(id = R.string.default_background),
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.default_background)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)

            ) {
                AnimatedContent(targetState = backgroundUri, label = "selectedBackground") {
                    AsyncImage(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(MaterialTheme.shapes.small),
                        model = ImageRequest.Builder(context).data(it)
                            .crossfade(true).build(),
                        placeholder = painterResource(R.drawable.adhan_background),
                        error = painterResource(R.drawable.adhan_background),
                        contentDescription = stringResource(R.string.athan_background),
                        contentScale = ContentScale.Fit
                    )
                }
            }

        }
    }
}

fun Context.getCarPhotosDirectory() = getExternalFilesDir("images")

fun copyPhotoToFolder(context: Context, selectedImageUri: Uri, id: Int): Uri? {
    val contentResolver: ContentResolver = context.contentResolver
    val inputStream: InputStream? = contentResolver.openInputStream(selectedImageUri)

    if (inputStream != null) {
        val outputFile =
            File(
                context.getCarPhotosDirectory(),
                "selectedBackground-$id-${System.currentTimeMillis()}.jpg"
            )
        context.getCarPhotosDirectory()?.listFiles()?.forEach { file ->
            if (file.name.startsWith("selectedBackground-$id")) file.delete()
        }
        val outputStream = FileOutputStream(outputFile)

        val buffer = ByteArray(1024)
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            outputStream.write(buffer, 0, bytesRead)
        }

        outputStream.close()
        inputStream.close()
        return Uri.fromFile(outputFile)
    }
    return null
}
