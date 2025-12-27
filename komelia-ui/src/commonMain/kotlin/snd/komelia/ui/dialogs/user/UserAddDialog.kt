package snd.komelia.ui.dialogs.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import snd.komelia.ui.LoadState
import snd.komelia.ui.LocalViewModelFactory
import snd.komelia.ui.common.components.CheckboxWithLabel
import snd.komelia.ui.common.components.PasswordTextField
import snd.komelia.ui.common.components.withTextFieldNavigation
import snd.komelia.ui.dialogs.AppDialog

@Composable
fun UserAddDialog(
    onDismiss: () -> Unit,
    afterConfirm: () -> Unit
) {
    val viewModelFactory = LocalViewModelFactory.current
    val vm = remember { viewModelFactory.getUserAddDialogViewModel() }
    if (vm.state.collectAsState().value is LoadState.Success) {
        onDismiss()
    }
    UserAddDialog(
        email = vm.email,
        emailValidation = vm.emailValidationError,
        onEmailChange = vm::onEmailChange,
        password = vm.password,
        passwordValidation = vm.passwordValidationError,
        onPasswordChange = vm::onPasswordChange,
        administratorRole = vm.administratorRole,
        onAdministratorRoleChange = vm::administratorRole::set,
        pageStreamingRole = vm.pageStreamingRole,
        onPageStreamingRoleChange = vm::pageStreamingRole::set,
        fileDownloadRole = vm.fileDownloadRole,
        onFileDownloadRoleChange = vm::fileDownloadRole::set,

        isValid = vm.isValid,

        onUserAdd = vm::addUser,
        afterConfirm = afterConfirm,
        onDismissRequest = onDismiss,
    )
}

@Composable
fun UserAddDialog(
    email: String,
    emailValidation: String?,
    onEmailChange: (String) -> Unit,
    password: String,
    passwordValidation: String?,
    onPasswordChange: (String) -> Unit,

    administratorRole: Boolean,
    onAdministratorRoleChange: (Boolean) -> Unit,
    pageStreamingRole: Boolean,
    onPageStreamingRoleChange: (Boolean) -> Unit,
    fileDownloadRole: Boolean,
    onFileDownloadRoleChange: (Boolean) -> Unit,

    isValid: Boolean,

    onUserAdd: suspend () -> Unit,
    afterConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    AppDialog(
        onDismissRequest = onDismissRequest,
        modifier = Modifier.widthIn(max = 600.dp),
        header = {
            Text(
                text = "Add User",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp)
            )
        },
        content = {
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.padding(20.dp)
            ) {

                TextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text("Email") },
                    supportingText = {
                        if (emailValidation != null)
                            Text(text = emailValidation, color = MaterialTheme.colorScheme.error)
                    },
                    modifier = Modifier.fillMaxWidth().withTextFieldNavigation()
                )

                PasswordTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = { Text("Password") },
                    isError = passwordValidation != null,
                    supportingText = { passwordValidation?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )

                Column {
                    Text("Roles")

                    CheckboxWithLabel(
                        checked = administratorRole,
                        onCheckedChange = onAdministratorRoleChange,
                        label = { Text("Administrator") }
                    )

                    CheckboxWithLabel(
                        checked = pageStreamingRole,
                        onCheckedChange = onPageStreamingRoleChange,
                        label = { Text("Page Streaming") }
                    )

                    CheckboxWithLabel(
                        checked = fileDownloadRole,
                        onCheckedChange = onFileDownloadRoleChange,
                        label = { Text("File Download") }
                    )
                }
            }
        },

        controlButtons = {
            val coroutineScope = rememberCoroutineScope()
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.padding(10.dp),
            ) {
                ElevatedButton(
                    onClick = onDismissRequest,
                    shape = RoundedCornerShape(5.dp),
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                ) {
                    Text("Cancel")
                }

                FilledTonalButton(
                    onClick = {
                        coroutineScope.launch {
                            onUserAdd()
                            afterConfirm()
                        }
                    },
                    enabled = isValid,
                    shape = RoundedCornerShape(5.dp),
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                ) {
                    Text("Add")
                }
            }
        }
    )
}
