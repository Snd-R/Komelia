package snd.komelia.updates

data class UpdateProgress(
    val total: Long,
    val completed: Long,
    val description: String? = null,
)
