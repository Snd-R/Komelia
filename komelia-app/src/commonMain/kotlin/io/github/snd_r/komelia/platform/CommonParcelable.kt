package io.github.snd_r.komelia.platform

annotation class CommonParcelize

@OptIn(ExperimentalMultiplatform::class)
@OptionalExpectation
@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.BINARY)
expect annotation class CommonParcelizeRawValue()

expect interface CommonParcelable
