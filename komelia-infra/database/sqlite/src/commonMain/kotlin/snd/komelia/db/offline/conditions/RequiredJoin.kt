package snd.komelia.db.offline.conditions

import snd.komga.client.collection.KomgaCollectionId
import snd.komga.client.readlist.KomgaReadListId
import snd.komga.client.user.KomgaUserId

/**
 * An indication that some tables need to be joined for query conditions to work
 */
sealed class RequiredJoin {
    data object BookMetadata : RequiredJoin()

    data object Media : RequiredJoin()

    data class ReadProgress(
        val userId: KomgaUserId,
    ) : RequiredJoin()

    data class ReadList(
        val readListId: KomgaReadListId,
    ) : RequiredJoin()

    data class Collection(
        val collectionId: KomgaCollectionId,
    ) : RequiredJoin()

    data object BookMetadataAggregation : RequiredJoin()

    data object SeriesMetadata : RequiredJoin()
}
