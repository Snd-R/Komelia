package snd.komelia.ui.platform

import androidx.compose.runtime.Composable

@Composable
expect fun BackPressHandler(onBackPressed: () -> Unit)

