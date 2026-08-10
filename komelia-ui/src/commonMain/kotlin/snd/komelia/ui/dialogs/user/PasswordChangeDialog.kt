package snd.komelia.ui.dialogs.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component1
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component2
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.change_password_dialog_cancel
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.change_password_dialog_change_password
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.change_password_dialog_new_password
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.change_password_dialog_new_password_repeat
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.change_password_dialog_new_password_repeat_error
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.change_password_dialog_new_password_required
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.change_password_dialog_title
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import snd.komelia.ui.LocalViewModelFactory
import snd.komelia.ui.common.components.PasswordTextField
import snd.komelia.ui.dialogs.AppDialog
import snd.komga.client.user.KomgaUser

@Composable
fun PasswordChangeDialog(
    user: KomgaUser?,
    onDismiss: () -> Unit,
) {
    val viewModelFactory = LocalViewModelFactory.current
    val vm = remember { viewModelFactory.getPasswordChangeDialogViewModel(user) }

    PasswordChangeDialog(
        onPasswordChange = vm::changePassword,
        onDismiss = onDismiss
    )
}

@Composable
fun PasswordChangeDialog(
    onPasswordChange: suspend (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var password by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }

    var repeatPassword by remember { mutableStateOf("") }
    var repeatPasswordError by remember { mutableStateOf<String?>(null) }

    val newPasswordRequiredString = stringResource(Res.string.change_password_dialog_new_password_required)
    val passwordsNotIdenticalString = stringResource(Res.string.change_password_dialog_new_password_repeat_error)
    AppDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 600.dp),
        header = {
            Text(
                text = stringResource(Res.string.change_password_dialog_title),
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
                val (first, second) = remember { FocusRequester.createRefs() }

                PasswordTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(Res.string.change_password_dialog_new_password)) },
                    isError = passwordError != null,
                    supportingText = { passwordError?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                        .focusRequester(first)
                        .focusProperties { next = second }
                )

                PasswordTextField(
                    value = repeatPassword,
                    onValueChange = { repeatPassword = it },
                    label = { Text(stringResource(Res.string.change_password_dialog_new_password_repeat)) },
                    isError = repeatPasswordError != null,
                    supportingText = { repeatPasswordError?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                        .focusRequester(second)
                )
                Spacer(Modifier.weight(1f))

            }
        },

        controlButtons = {
            val coroutineScope = rememberCoroutineScope()
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.padding(10.dp),
            ) {
                ElevatedButton(
                    onClick = onDismiss,
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                ) {
                    Text(stringResource(Res.string.change_password_dialog_cancel))
                }

                FilledTonalButton(
                    onClick = {
                        when {
                            password.isBlank() -> passwordError = newPasswordRequiredString
                            password != repeatPassword -> repeatPasswordError = passwordsNotIdenticalString
                            else -> coroutineScope.launch {
                                onPasswordChange(password)
                                onDismiss()
                            }
                        }
                    },
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                ) {
                    Text(stringResource(Res.string.change_password_dialog_change_password))
                }
            }
        }
    )
}
