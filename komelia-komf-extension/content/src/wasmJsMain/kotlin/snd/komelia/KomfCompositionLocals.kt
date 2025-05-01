package snd.komelia

import androidx.compose.runtime.compositionLocalOf

val LocalKomfViewModelFactory = compositionLocalOf<KomfViewModelFactory> {
    error("composition local ViewModel factory is not set")
}
