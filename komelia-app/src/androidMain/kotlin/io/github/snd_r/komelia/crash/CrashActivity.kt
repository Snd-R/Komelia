package io.github.snd_r.komelia.crash

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import io.github.snd_r.komelia.MainActivity
import io.github.snd_r.komelia.ui.error.ErrorView

class CrashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val exceptionData = GlobalExceptionHandler.getExceptionDataFromIntent(intent)
        val exceptionMessage = if (exceptionData == null) "Unknown Error"
        else "${exceptionData.exceptionName}: ${exceptionData.message}"

        setContent {
            ErrorView(
                exceptionMessage = exceptionMessage,
                stacktrace = exceptionData?.stacktrace,
                isRestartable = true,
                onRestart = {
                    finishAffinity()
                    startActivity(Intent(this@CrashActivity, MainActivity::class.java))
                },
                onExit = { this.finishAndRemoveTask() }
            )
        }
    }
}