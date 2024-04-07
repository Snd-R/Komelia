package io.github.snd_r.komelia.ui.dialogs.user

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.common.CheckboxWithLabel
import io.github.snd_r.komelia.ui.common.PasswordTextField
import io.github.snd_r.komelia.ui.common.withTextFieldNavigation
import kotlinx.coroutines.launch

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
    Dialog(onDismissRequest = onDismissRequest) {
        val focusManager = LocalFocusManager.current
        val coroutineScope = rememberCoroutineScope()
        Surface(
            Modifier
                .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) },
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.padding(20.dp)
            ) {

                Text("Add User")

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
                    label = "password",
                    error = passwordValidation,
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

                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    Spacer(Modifier.weight(1f))

                    TextButton(onClick = onDismissRequest) {
                        Text("CANCEL")
                    }

                    FilledTonalButton(
                        onClick = {
                            coroutineScope.launch {
                                onUserAdd()
                                afterConfirm()
                            }
                        },
                        enabled = isValid
                    ) {
                        Text("ADD")
                    }
                }
            }
        }
    }

}
