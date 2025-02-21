package io.github.snd_r.komelia.image

import snd.komelia.image.ReduceKernel

actual fun availableReduceKernels() = listOf(
    ReduceKernel.LANCZOS3,
    ReduceKernel.LANCZOS2,
    ReduceKernel.MITCHELL,
//    ReduceKernel.LINEAR,
//    ReduceKernel.CUBIC,
//    ReduceKernel.MKS2013,
//    ReduceKernel.MKS2021,
)