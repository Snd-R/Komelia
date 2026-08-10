package snd.komelia.ui.dialogs.user

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
import androidx.compose.material3.HorizontalDivider
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
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.user_edit_age
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.user_edit_age_restriction
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.user_edit_content_restrictions
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.user_edit_dialog_title
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.user_edit_labels_allow
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.user_edit_labels_exclude
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.user_edit_roles_for_user
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.user_edit_save
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.user_edit_share_libraries
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.user_edit_share_libraries_all
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.user_edit_tab_content_restriction
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.user_edit_tab_roles
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.user_edit_tab_shared_libraries
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.user_roles_admin
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.user_roles_file_download
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.user_roles_page_streaming
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import snd.komelia.ui.LocalViewModelFactory
import snd.komelia.ui.OptionsStateHolder
import snd.komelia.ui.StateHolder
import snd.komelia.ui.common.components.CheckboxWithLabel
import snd.komelia.ui.common.components.DropdownChoiceMenu
import snd.komelia.ui.common.components.LabeledEntry
import snd.komelia.ui.dialogs.tabs.DialogTab
import snd.komelia.ui.dialogs.tabs.TabDialog
import snd.komelia.ui.dialogs.tabs.TabItem
import snd.komelia.ui.dialogs.user.UserEditDialogViewModel.AgeRestriction
import snd.komelia.ui.strings.AppStrings
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.user.KomgaUser

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
        title = stringResource(Res.string.user_edit_dialog_title),
        currentTab = vm.currentTab,
        tabs = vm.tabs(),
        confirmationText = stringResource(Res.string.user_edit_save),
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
        title = Res.string.user_edit_tab_roles,
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
            Text(stringResource(Res.string.user_edit_roles_for_user, user.email))
            Spacer(Modifier.height(20.dp))
            CheckboxWithLabel(
                checked = administrator.value,
                onCheckedChange = { administrator.setValue(it) },
                label = { Text(stringResource(Res.string.user_roles_admin)) }
            )
            CheckboxWithLabel(
                checked = pageStreaming.value,
                onCheckedChange = { pageStreaming.setValue(it) },
                label = { Text(stringResource(Res.string.user_roles_page_streaming)) }
            )
            CheckboxWithLabel(
                checked = fileDownload.value,
                onCheckedChange = { fileDownload.setValue(it) },
                label = { Text(stringResource(Res.string.user_roles_file_download)) }
            )
        }
    }
}

class UserSharedLibrariesTab(private val vm: UserEditDialogViewModel) : DialogTab {

    override fun options() = TabItem(
        title = Res.string.user_edit_tab_shared_libraries,
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
            Text(stringResource(Res.string.user_edit_share_libraries))
            Spacer(Modifier.height(20.dp))
            CheckboxWithLabel(
                checked = shareAll,
                onCheckedChange = onShareAllChange,
                label = { Text(stringResource(Res.string.user_edit_share_libraries_all)) }
            )

            HorizontalDivider()

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
        title = Res.string.user_edit_tab_content_restriction,
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
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text(stringResource(Res.string.user_edit_content_restrictions))
            Column(verticalArrangement = Arrangement.spacedBy(40.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    DropdownChoiceMenu(
                        selectedOption = LabeledEntry(
                            restriction.value,
                            stringResource(AppStrings.forAgeRestriction(restriction.value))
                        ),
                        options = restriction.options.map {
                            LabeledEntry(
                                it,
                                stringResource(AppStrings.forAgeRestriction(restriction.value))
                            )
                        },
                        onOptionChange = { restriction.onValueChange(it.value) },
                        label = { Text(stringResource(Res.string.user_edit_age_restriction)) },
                        inputFieldModifier = Modifier.weight(1f)
                    )
                    TextField(
                        value = age.value.toString(),
                        onValueChange = {
                            val newValue = it.toIntOrNull()
                            if (newValue != null) age.setValue(newValue)
                        },
                        label = { Text(stringResource(Res.string.user_edit_age)) },
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
                    label = { Text(stringResource(Res.string.user_edit_labels_allow)) },
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
                    label = { Text(stringResource(Res.string.user_edit_labels_exclude)) },
                    onSubmit = { text -> Chip(text) },
                    readOnlyChips = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

