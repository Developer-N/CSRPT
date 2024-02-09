package ir.namoo.religiousprayers.ui.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import ir.namoo.commons.FILE_PICKER_REQUEST_CODE

class PickSound(var fileType: Int = -1) : ActivityResultContract<Int, Intent?>() {
    override fun createIntent(context: Context, input: Int): Intent {
        fileType = input
        return Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "audio/mpeg"
            putExtra(FILE_PICKER_REQUEST_CODE, input)
        }
    }


    override fun parseResult(resultCode: Int, intent: Intent?): Intent? {
        if (resultCode != Activity.RESULT_OK) {
            return null
        }
        return intent?.apply {
            putExtra(FILE_PICKER_REQUEST_CODE, fileType)
        }
    }
}
