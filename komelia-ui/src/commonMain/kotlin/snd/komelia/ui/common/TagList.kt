package snd.komelia.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import snd.komelia.ui.common.components.DescriptionChips
import snd.komelia.ui.common.components.LabeledEntry
import snd.komelia.ui.common.components.LabeledEntry.Companion.stringEntry

@Composable
fun TagList(
    tags: List<String>,
    secondaryTags: List<String>? = null,
    onTagClick: (String) -> Unit = {}
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val mutableTagList = remember(tags) { tags.toMutableList() }
        val mutableSecondaryTagList = remember(secondaryTags) { secondaryTags?.toMutableList() }

        val parodyTags = remember(tags) {
            extractTagListByPrefix(mutableTagList, "parody:")
        }
        val secondaryParodyTags = remember(secondaryTags) {
            mutableSecondaryTagList?.let {
                extractTagListByPrefix(it, "parody:")
            }
        }

        val characterTags = remember(tags) {
            extractTagListByPrefix(mutableTagList, "character:")
        }
        val secondaryCharacterTags = remember(secondaryTags) {
            mutableSecondaryTagList?.let { extractTagListByPrefix(it, "character:") }
        }

        val groupTags = remember(tags) {
            extractTagListByPrefix(mutableTagList, "group:")
        }
        val secondaryGroupTags = remember(secondaryTags) {
            mutableSecondaryTagList?.let { extractTagListByPrefix(it, "group:") }
        }

        val femaleTags = remember(tags) {
            extractTagListByPrefix(mutableTagList, "female:")
        }
        val secondaryFemaleTags = remember(secondaryTags) {
            mutableSecondaryTagList?.let {
                extractTagListByPrefix(it, "female:")
            }
        }
        val maleTags = remember(tags) {
            extractTagListByPrefix(mutableTagList, "male:")
        }
        val secondaryMaleTags = remember(secondaryTags) {
            mutableSecondaryTagList?.let {
                extractTagListByPrefix(it, "male:")
            }
        }
        val categoryTags = remember(tags) {
            extractTagListByPrefix(mutableTagList, "category:")
        }
        val secondaryCategoryTags = remember(secondaryTags) {
            mutableSecondaryTagList?.let {
                extractTagListByPrefix(it, "category:")
            }
        }

        val tagEntries = remember(tags) {
            mutableTagList.map { stringEntry(it) }
        }
        val secondaryTagEntries = remember(secondaryTags) {
            mutableSecondaryTagList?.map { stringEntry(it) }
        }

        if (tagEntries.size == tags.size && secondaryTags?.size == secondaryTagEntries?.size) {
            DescriptionChips(
                label = "Tags",
                chipValues = tagEntries,
                secondaryValues = secondaryTagEntries,
                onChipClick = onTagClick,
            )
        } else {
            if (parodyTags.isNotEmpty() || !secondaryParodyTags.isNullOrEmpty()) {
                DescriptionChips(
                    label = "Parody",
                    chipValues = parodyTags,
                    secondaryValues = secondaryParodyTags,
                    onChipClick = onTagClick,
                )
            }

            if (characterTags.isNotEmpty() || !secondaryCharacterTags.isNullOrEmpty()) {
                DescriptionChips(
                    label = "Character",
                    chipValues = characterTags,
                    secondaryValues = secondaryCharacterTags,
                    onChipClick = onTagClick,
                )
            }

            if (groupTags.isNotEmpty() || !secondaryGroupTags.isNullOrEmpty()) {
                DescriptionChips(
                    label = "Group",
                    chipValues = groupTags,
                    secondaryValues = secondaryGroupTags,
                    onChipClick = onTagClick,
                )
            }

            if (femaleTags.isNotEmpty() || !secondaryFemaleTags.isNullOrEmpty()) {
                DescriptionChips(
                    label = "Female",
                    chipValues = femaleTags,
                    secondaryValues = secondaryFemaleTags,
                    onChipClick = onTagClick,
                )
            }
            if (maleTags.isNotEmpty() || !secondaryMaleTags.isNullOrEmpty()) {
                DescriptionChips(
                    label = "Male",
                    chipValues = maleTags,
                    secondaryValues = secondaryMaleTags,
                    onChipClick = onTagClick,
                )
            }
            if (categoryTags.isNotEmpty() || !secondaryCategoryTags.isNullOrEmpty()) {
                DescriptionChips(
                    label = "Category",
                    chipValues = categoryTags,
                    secondaryValues = secondaryCategoryTags,
                    onChipClick = onTagClick,
                )
            }
            DescriptionChips(
                label = "Other tags",
                chipValues = tagEntries,
                secondaryValues = secondaryTagEntries,
                onChipClick = onTagClick,
            )

        }

    }
}

private fun extractTagListByPrefix(
    tags: MutableList<String>,
    prefix: String
): List<LabeledEntry<String>> {
    val results = mutableListOf<LabeledEntry<String>>()
    val iterator = tags.iterator()
    while (iterator.hasNext()) {
        val element = iterator.next()
        if (element.startsWith(prefix)) {
            results.add(LabeledEntry(element, element.removePrefix(prefix)))
            iterator.remove()
        }
    }
    return results
}

//    tags.filter { it.startsWith(prefix) }
//        .map { LabeledEntry(it, it.removePrefix(prefix)) }