package io.github.snd_r.komelia.ui.dialogs.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockPerson
import androidx.compose.material.icons.filled.RecentActors
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dokar.chiptextfield.Chip
import com.dokar.chiptextfield.m3.ChipTextField
import com.dokar.chiptextfield.rememberChipTextFieldState
import io.github.snd_r.komelia.ui.LocalStrings
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.common.CheckboxWithLabel
import io.github.snd_r.komelia.ui.common.DropdownChoiceMenu
import io.github.snd_r.komelia.ui.common.LabeledEntry
import io.github.snd_r.komelia.ui.common.OptionsStateHolder
import io.github.snd_r.komelia.ui.common.StateHolder
import io.github.snd_r.komelia.ui.dialogs.tabs.DialogTab
import io.github.snd_r.komelia.ui.dialogs.tabs.TabDialog
import io.github.snd_r.komelia.ui.dialogs.tabs.TabItem
import io.github.snd_r.komelia.ui.dialogs.user.UserEditDialogViewModel.AgeRestriction
import io.github.snd_r.komga.library.KomgaLibrary
import io.github.snd_r.komga.library.KomgaLibraryId
import io.github.snd_r.komga.user.KomgaUser
import kotlinx.coroutines.launch

@Composable
fun UserEditDialog(
    user: KomgaUser,
    afterConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val viewModelFactory = LocalViewModelFactory.current
    val vm = remember { viewModelFactory.getUserEditDialogViewModel(user) }
    val coroutineScope = rememberCoroutineScope()

    TabDialog(
        title = "Edit User",
        currentTab = vm.currentTab,
        tabs = vm.tabs(),
        confirmationText = "Save Changes",
        onConfirm = {
            coroutineScope.launch {
                vm.saveChanges()
                afterConfirm()
                onDismiss()
            }
        },
        onTabChange = { vm.currentTab = it },
        onDismissRequest = onDismiss
    )
}

class UserRolesTab(private val vm: UserEditDialogViewModel) : DialogTab {
    override fun options() = TabItem(
        title = "Roles",
        icon = Icons.Default.RecentActors
    )

    @Composable
    override fun Content() {
        UserRolesContent(
            user = vm.user,
            administrator = StateHolder(vm.administratorRole, vm::administratorRole::set),
            pageStreaming = StateHolder(vm.pageStreamingRole, vm::pageStreamingRole::set),
            fileDownload = StateHolder(vm.fileDownloadRole, vm::fileDownloadRole::set),
        )
    }

    @Composable
    private fun UserRolesContent(
        user: KomgaUser,
        administrator: StateHolder<Boolean>,
        pageStreaming: StateHolder<Boolean>,
        fileDownload: StateHolder<Boolean>
    ) {
        Column {
            Text("Roles for ${user.email}")
            Spacer(Modifier.height(20.dp))
            CheckboxWithLabel(
                checked = administrator.value,
                onCheckedChange = { administrator.setValue(it) },
                label = { Text("Administrator") }
            )
            CheckboxWithLabel(
                checked = pageStreaming.value,
                onCheckedChange = { pageStreaming.setValue(it) },
                label = { Text("Page Streaming") }
            )
            CheckboxWithLabel(
                checked = fileDownload.value,
                onCheckedChange = { fileDownload.setValue(it) },
                label = { Text("File Download") }
            )
        }
    }
}

class UserSharedLibrariesTab(private val vm: UserEditDialogViewModel) : DialogTab {

    override fun options() = TabItem(
        title = "Shared Libraries",
        icon = Icons.Default.Share
    )

    @Composable
    override fun Content() {
        UserSharedLibrariesContent(
            shareAll = vm.shareAllLibraries,
            onShareAllChange = vm::shareAllLibraries::set,
            allLibraries = vm.libraries,
            sharedLibraries = vm.sharedLibraries,
            onLibraryCheck = vm::addSharedLibrary,
            onLibraryUncheck = vm::removeSharedLibrary
        )
    }

    @Composable
    private fun UserSharedLibrariesContent(
        shareAll: Boolean,
        onShareAllChange: (Boolean) -> Unit,
        allLibraries: List<KomgaLibrary>,
        sharedLibraries: Set<KomgaLibraryId>,
        onLibraryCheck: (KomgaLibraryId) -> Unit,
        onLibraryUncheck: (KomgaLibraryId) -> Unit,
    ) {
        Column {
            Text("Share Libraries")
            Spacer(Modifier.height(20.dp))
            CheckboxWithLabel(
                checked = shareAll,
                onCheckedChange = onShareAllChange,
                label = { Text("All Libraries") }
            )

            Divider()

            allLibraries.forEach { library ->

                CheckboxWithLabel(
                    checked = sharedLibraries.contains(library.id),
                    onCheckedChange = { isChecked ->
                        if (!shareAll) {
                            if (isChecked) onLibraryCheck(library.id) else onLibraryUncheck(library.id)
                        }
                    },
                    label = {
                        Text(
                            library.name,
                            color = if (shareAll) MaterialTheme.colorScheme.surfaceVariant else Color.Unspecified
                        )
                    },
                    enabled = !shareAll
                )

            }

        }
    }
}

class UserContentRestrictionTab(private val vm: UserEditDialogViewModel) : DialogTab {

    override fun options() = TabItem(
        title = "Content Restriction",
        icon = Icons.Default.LockPerson
    )

    @Composable
    override fun Content() {
        UserContentRestrictionContent(
            restriction = OptionsStateHolder(vm.ageRestriction, AgeRestriction.entries, vm::ageRestriction::set),
            age = StateHolder(vm.ageRating, vm::ageRating::set),
            labelsAllow = StateHolder(vm.labelsAllow, vm::labelsAllow::set),
            labelsExclude = StateHolder(vm.labelsExclude, vm::labelsExclude::set)

        )

    }

    @Composable
    private fun UserContentRestrictionContent(
        restriction: OptionsStateHolder<AgeRestriction>,
        age: StateHolder<Int>,
        labelsAllow: StateHolder<Set<String>>,
        labelsExclude: StateHolder<Set<String>>,
    ) {
        val strings = LocalStrings.current.userEdit

        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text(strings.contentRestrictions)
            Column(verticalArrangement = Arrangement.spacedBy(40.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    DropdownChoiceMenu(
                        selectedOption = LabeledEntry(restriction.value, strings.forAgeRestriction(restriction.value)),
                        options = restriction.options.map { LabeledEntry(it, strings.forAgeRestriction(it)) },
                        onOptionChange = { restriction.onValueChange(it.value) },
                        label = { Text(strings.ageRestriction) },
                        textFieldModifier = Modifier.weight(1f)
                    )
                    TextField(
                        value = age.value.toString(),
                        onValueChange = {
                            val newValue = it.toIntOrNull()
                            if (newValue != null) age.setValue(newValue)
                        },
                        label = { Text(strings.age) },
                        modifier = Modifier.weight(1f),
                        enabled = restriction.value != AgeRestriction.NONE
                    )
                }

                val labelsAllowState = rememberChipTextFieldState(labelsAllow.value.map { Chip(it) })
                LaunchedEffect(labelsAllowState, labelsAllow.value) {
                    snapshotFlow { labelsAllowState.chips.map { it.text }.toSet() }
                        .collect { labelsAllow.setValue(it) }
                }
                ChipTextField(
                    state = labelsAllowState,
                    label = { Text(strings.labelsAllow) },
                    onSubmit = { text -> Chip(text) },
                    readOnlyChips = true,
                    modifier = Modifier.fillMaxWidth()
                )

                val labelsExcludeState = rememberChipTextFieldState(labelsExclude.value.map { Chip(it) })
                LaunchedEffect(labelsExcludeState, labelsExclude.value) {
                    snapshotFlow { labelsExcludeState.chips.map { it.text }.toSet() }
                        .collect { labelsExclude.setValue(it) }
                }
                ChipTextField(
                    state = labelsExcludeState,
                    label = { Text(strings.labelsExclude) },
                    onSubmit = { text -> Chip(text) },
                    readOnlyChips = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

