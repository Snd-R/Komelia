package io.github.snd_r.komelia.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.Image
import coil3.annotation.ExperimentalCoilApi

@OptIn(ExperimentalCoilApi::class)
@Composable
expect fun ReaderImage(
    image: Image,
    contentScale: ContentScale = ContentScale.Fit,
    modifier: Modifier = Modifier
)
