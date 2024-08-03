package io.github.snd_r.komelia.platform

import androidx.compose.runtime.Composable

@Composable
expect fun BackPressHandler(onBackPressed: () -> Unit)

