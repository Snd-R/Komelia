package snd.komelia.ui.series.mangabaka.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import snd.komf.api.mangabaka.KomfMangaBakaTag
import snd.komf.api.mangabaka.MangaBakaContentRating
import snd.komf.api.mangabaka.MangaBakaSeriesTag
import snd.komf.api.mangabaka.MangaBakaTagId
import snd.komf.api.mangabaka.MangaBakaTagWeight
import snd.komf.api.mangabaka.MangaBakaTagWeight.CORE
import snd.komf.api.mangabaka.MangaBakaTagWeight.DEFINING
import snd.komf.api.mangabaka.MangaBakaTagWeight.INCIDENTAL
import snd.komf.api.mangabaka.MangaBakaTagWeight.RECURRENT
import snd.komf.api.mangabaka.MangaBakaTagWeight.UNWEIGHTED

class MangaBakaTagsState(
    private val rawTags: List<MangaBakaSeriesTag>,
    allTags: StateFlow<List<KomfMangaBakaTag>?>,
    coroutineScope: CoroutineScope,
) {
    var weight by mutableStateOf(DEFINING)
    var showSpoiler by mutableStateOf(false)
    var showImplied by mutableStateOf(false)

    val hasCoreTags: Boolean
    val hasDefiningTags: Boolean
    val hasRecurrentTags: Boolean
    val hasIncidentalTags: Boolean
    val hasUnweightedTags: Boolean
    var displayTags = mutableStateListOf<BakaTagNode>()
    val tags: StateFlow<List<BakaTag>> = allTags.map { allTags ->
        if (allTags.isNullOrEmpty()) return@map emptyList()
        val seriesTags = rawTags.map { it.toBakaTag() }

        val allTagsById = allTags.map { it.toBakaTag() }.associateBy { it.id }
        val seriesTagsById = seriesTags.associateBy { it.id }.toMutableMap()

        val missingTags = HashSet<BakaTag>()
        var tagsToScan = seriesTags
        for (i in 0 until 5) {
            val orphans = tagsToScan.filter { tag -> tag.parentId != null && seriesTagsById[tag.parentId] == null }
            if (orphans.isEmpty()) break

            val missing = mutableListOf<BakaTag>()
            for (orphan in orphans) {
                val missingParent = allTagsById[orphan.parentId] ?: continue
                seriesTagsById[missingParent.id] = missingParent
                missingTags.add(missingParent)
                missing.add(missingParent)
            }
            if (missing.isEmpty()) break

            tagsToScan = missing
        }

        seriesTags + missingTags
    }.flowOn(Dispatchers.Default).stateIn(coroutineScope, SharingStarted.Eagerly, emptyList())

    var impliedCount by mutableStateOf(0)
    var spoilerCount by mutableStateOf(0)

    init {
        var hasCore = false
        var hasDefining = false
        var hasRecurrent = false
        var hasIncidental = false
        var hasUnweighted = false
        for (tag in rawTags) {
            when (tag.weight) {
                CORE -> hasCore = true
                DEFINING -> hasDefining = true
                RECURRENT -> hasRecurrent = true
                INCIDENTAL -> hasIncidental = true
                UNWEIGHTED -> hasUnweighted = true
            }
        }
        this.hasCoreTags = hasCore
        this.hasDefiningTags = hasDefining
        this.hasRecurrentTags = hasRecurrent
        this.hasIncidentalTags = hasIncidental
        this.hasUnweightedTags = hasUnweighted
        tags.onEach { applyFilters(it) }.flowOn(Dispatchers.Default).launchIn(coroutineScope)
    }

    private fun applyFilters(tags: List<BakaTag>) {
        spoilerCount = 0
        impliedCount = 0
        val (rootNodes, stats) = mapTreeRoots(
            tags = tags,
            weight = weight,
            includeSpoilers = showSpoiler,
            includeImplied = showImplied,
        )

        displayTags.clear()
        displayTags.addAll(rootNodes)
        spoilerCount = stats.spoilerCount
        impliedCount = stats.impliedCount
    }

    fun onFilterChange(filter: MangaBakaTagWeight) {
        this.weight = filter
        applyFilters(tags.value)
    }

    fun onSpoilerShowChange(show: Boolean) {
        showSpoiler = show
    }

    fun onImpliedShowChange(show: Boolean) {
        showImplied = show
        applyFilters(tags.value)
    }

    private fun mapTreeRoots(
        tags: List<BakaTag>,
        weight: MangaBakaTagWeight,
        includeSpoilers: Boolean,
        includeImplied: Boolean,
    ): Pair<List<BakaTagNode>, TagStats> {
        var spoilerCount = 0
        var impliedCount = 0

        val genresRoot = BakaTagNode(
            tag = BakaTag(
                isSeriesTag = false,
                id = MangaBakaTagId(-1),
                parentId = null,
                contentRating = MangaBakaContentRating.SAFE,
                description = null,
                isSpoiler = false,
                level = 1,
                name = "Genres",
                namePath = "Genres",
                seriesCount = 0,
                impliedByTagIds = emptyList(),
                isExplicit = false,
                isGenre = false,
                mergedWith = null,
                weight = UNWEIGHTED
            ),
            parent = null
        )

        val tagsByLevel = tags.groupBy { it.level }
        val toplevel = tagsByLevel[1] ?: return Pair(emptyList(), TagStats(0, 0))

        val roots = mutableMapOf<String, BakaTagNode>()
        for (root in toplevel) {
            val rootNode = BakaTagNode(
                tag = root,
                parent = null,
            )
            val stats = assignChildren(
                parent = rootNode,
                genresRoot = genresRoot,
                tagsByLevel = tagsByLevel,
                weightFilter = weight,
                includeSpoilers = includeSpoilers,
                includeImplied = includeImplied
            )
            if (rootNode.children.isEmpty()) continue
            spoilerCount += stats.spoilerCount
            impliedCount += stats.impliedCount
            roots[rootNode.tag.name] = rootNode
        }

        genresRoot.children.sortWith(BakaTagNode.leafComparator)
        val genres = if (genresRoot.children.isEmpty()) null else genresRoot
        val themes = roots.remove("Themes")
        val settings = roots.remove("Settings")
        val other = roots.values.sortedBy { it.tag.name }
        return Pair(listOfNotNull(genres, themes, settings) + other, TagStats(spoilerCount, impliedCount))
    }

    private fun assignChildren(
        parent: BakaTagNode,
        genresRoot: BakaTagNode,
        tagsByLevel: Map<Int, List<BakaTag>>,
        weightFilter: MangaBakaTagWeight,
        includeSpoilers: Boolean,
        includeImplied: Boolean,
    ): TagStats {
        val rawChildren = tagsByLevel[parent.tag.level + 1] ?: return TagStats(0, 0)

        val leafs = mutableListOf<BakaTagNode>()
        val branches = mutableListOf<BakaTagNode>()

        var spoilerCount = 0
        var impliedCount = 0

        fun addLeaf(node: BakaTagNode) {
            if (node.tag.weight.ordinal > weightFilter.ordinal)
                return

            if (node.tag.isSpoiler) spoilerCount += 1
            if (node.tag.isExplicit) {
                if (node.tag.isGenre) genresRoot.add(node) else leafs.add(node)
            } else {
                impliedCount += 1
                if (includeImplied) {
                    if (node.tag.isGenre) genresRoot.add(node) else leafs.add(node)
                }
            }
        }

        for (child in rawChildren) {
            if (child.parentId == parent.tag.id) {
                val node = BakaTagNode(child, parent)
                val grandChildrenStats = assignChildren(
                    parent = node,
                    genresRoot = genresRoot,
                    tagsByLevel = tagsByLevel,
                    weightFilter = weightFilter,
                    includeSpoilers = includeSpoilers,
                    includeImplied = includeImplied
                )

                val grandChildren = node.children
                if (grandChildren.isEmpty())
                    addLeaf(node)
                else {
                    if (child.isSeriesTag && grandChildren.isNotEmpty()) {
                        val newNode = BakaTagNode(node.tag, parent)
                        if (newNode.isValid(weightFilter)) {
                            leafs.add(BakaTagNode(node.tag, parent))
                            if (node.tag.isSpoiler) spoilerCount += 1
                            if (!node.tag.isExplicit) impliedCount += 1
                        }
                    }

                    if (grandChildren.size == 1) {
                        val reducedNode = reduceTag(node)
                        if (reducedNode.children.isEmpty()) addLeaf(reducedNode)
                        else {
                            branches.add(reducedNode)
                            spoilerCount += grandChildrenStats.spoilerCount
                            impliedCount += grandChildrenStats.impliedCount
                        }
                    } else {
                        branches.add(node)
                        spoilerCount += grandChildrenStats.spoilerCount
                        impliedCount += grandChildrenStats.impliedCount
                    }
                }
            }
        }

        parent.children.addAll(
            leafs.sortedWith(BakaTagNode.leafComparator) + branches.sortedBy
            { it.tag.name })

        return TagStats(spoilerCount, impliedCount)
    }

    private fun reduceTag(node: BakaTagNode): BakaTagNode {
        val prefixes = mutableListOf<BakaTag>()
        var nextChild = node
        while (true) {
            when {
                nextChild.children.isEmpty() -> break
                nextChild.children.size > 1 -> {
//                prefixes.add(nextChild.tag)
                    break
                }

                nextChild.children.size == 1 -> {
                    prefixes.add(nextChild.tag)
                    nextChild = nextChild.children.first()
                    prefixes.addAll(nextChild.reducedPrefixes)
                }
            }
        }

        val reducedNode = BakaTagNode(
            tag = nextChild.tag,
            parent = node.parent,
        )
        reducedNode.reducedPrefixes.addAll(prefixes)
        reducedNode.children.addAll(nextChild.children)
        return reducedNode
    }


    private fun MangaBakaSeriesTag.toBakaTag(): BakaTag {
        return BakaTag(
            isSeriesTag = true,
            id = this.id,
            parentId = this.parentId,
            contentRating = this.contentRating,
            description = this.description,
            isSpoiler = this.isSpoiler ?: false,
            level = this.level,
            name = this.name,
            namePath = this.namePath,
            seriesCount = this.seriesCount,
            impliedByTagIds = this.impliedByTagIds,
            isExplicit = this.isExplicit,
            isGenre = this.isGenre,
            mergedWith = this.mergedWith,
            weight = this.weight
        )
    }

    private fun KomfMangaBakaTag.toBakaTag(): BakaTag {
        return BakaTag(
            isSeriesTag = false,
            id = this.id,
            parentId = this.parentId,
            contentRating = this.contentRating,
            description = this.description,
            isSpoiler = this.isSpoiler ?: false,
            level = this.level,
            name = this.name,
            namePath = this.namePath,
            seriesCount = this.seriesCount,
            isGenre = this.isGenre,
            mergedWith = this.mergedWith,
            isExplicit = false,
            impliedByTagIds = emptyList(),
            weight = UNWEIGHTED
        )
    }

    class BakaTagNode(
        val tag: BakaTag,
        val parent: BakaTagNode?,
    ) {
        val reducedPrefixes: MutableList<BakaTag> = mutableListOf()
        val children: MutableList<BakaTagNode> = mutableListOf()


        fun add(child: BakaTagNode) {
            children.add(child)
        }

        fun isValid(
            weightFilter: MangaBakaTagWeight,
        ): Boolean {
            if (children.isNotEmpty()) return true
            if (tag.weight.ordinal > weightFilter.ordinal)
                return false

            return true
        }

        companion object {
            val leafComparator = Comparator<BakaTagNode> { a, b ->
                val weight = compareValues(a.tag.weight, b.tag.weight)
                if (weight != 0) return@Comparator weight
                compareValues(a.tag.name, b.tag.name)
            }
        }
    }

    data class TagStats(
        val spoilerCount: Int,
        val impliedCount: Int,
    ) {
        fun plus(other: TagStats): TagStats {
            return TagStats(
                this.spoilerCount + other.spoilerCount,
                this.impliedCount + other.impliedCount
            )
        }
    }

    data class BakaTag(
        val isSeriesTag: Boolean,
        val id: MangaBakaTagId,
        val parentId: MangaBakaTagId?,
        val contentRating: MangaBakaContentRating,
        val description: String?,
        val isSpoiler: Boolean,
        val level: Int,
        val name: String,
        val namePath: String,
        val seriesCount: Int,
        val impliedByTagIds: List<MangaBakaTagId>,
        val isExplicit: Boolean,
        val isGenre: Boolean,
        val mergedWith: Long?,
        val weight: MangaBakaTagWeight
    )

}