package com.hastakala.shop.ui.components

import android.speech.tts.TextToSpeech
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

data class TextSpeaker(
    val canSpeak: Boolean,
    val speak: (String) -> Unit
)

@Composable
fun rememberTextSpeaker(languageTag: String): TextSpeaker {
    val context = LocalContext.current
    var speaker by remember { mutableStateOf<TextToSpeech?>(null) }
    var canSpeak by remember { mutableStateOf(false) }

    DisposableEffect(context, languageTag) {
        var engine: TextToSpeech? = null
        engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                engine?.setSpeechRate(0.92f)
                val result = engine?.setLanguage(Locale.forLanguageTag(languageTag))
                canSpeak = result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED
            } else {
                canSpeak = false
            }
        }
        speaker = engine

        onDispose {
            canSpeak = false
            engine?.stop()
            engine?.shutdown()
            speaker = null
        }
    }

    return remember(speaker, canSpeak) {
        TextSpeaker(
            canSpeak = canSpeak,
            speak = { text ->
                speaker?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "hasta_kala_tts")
            }
        )
    }
}
