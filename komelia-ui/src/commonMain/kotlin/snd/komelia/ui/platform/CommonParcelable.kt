package snd.komelia.ui.platform

annotation class CommonParcelize

@OptIn(ExperimentalMultiplatform::class)
@OptionalExpectation
@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.BINARY)
expect annotation class CommonParcelizeRawValue()

expect interface CommonParcelable
