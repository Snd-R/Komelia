package io.github.snd_r.komelia.platform

import androidx.compose.runtime.Composable
import coil3.Image
import coil3.annotation.ExperimentalCoilApi


@OptIn(ExperimentalCoilApi::class)
@Composable
expect fun ReaderImage(image: Image)
