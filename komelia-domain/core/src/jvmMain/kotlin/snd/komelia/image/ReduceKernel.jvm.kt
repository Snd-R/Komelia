package snd.komelia.image

actual fun availableReduceKernels(): List<ReduceKernel> {
    return if (vipsThumbnailKernelIsSupported)
        listOf(
            ReduceKernel.LANCZOS3,
            ReduceKernel.LANCZOS2,
            ReduceKernel.MITCHELL,
//            ReduceKernel.LINEAR,
//            ReduceKernel.CUBIC,
//            ReduceKernel.MKS2013,
//            ReduceKernel.MKS2021,
        )
    else emptyList()
}