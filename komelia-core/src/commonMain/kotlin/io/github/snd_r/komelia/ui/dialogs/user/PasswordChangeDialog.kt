package io.github.snd_r.komelia.ui.dialogs.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.common.PasswordTextField
import io.github.snd_r.komelia.ui.dialogs.AppDialog
import kotlinx.coroutines.launch
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

    AppDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 600.dp),
        header = {
            Text(
                text = "Change Password",
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
                    label = { Text("New password") },
                    isError = passwordError != null,
                    supportingText = { passwordError?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                        .focusRequester(first)
                        .focusProperties { next = second }
                )

                PasswordTextField(
                    value = repeatPassword,
                    onValueChange = { repeatPassword = it },
                    label = { Text("Repeat new password") },
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
                    shape = RoundedCornerShape(5.dp),
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                ) {
                    Text("Cancel")
                }

                FilledTonalButton(
                    onClick = {
                        when {
                            password.isBlank() -> passwordError = "New password is required"
                            password != repeatPassword -> repeatPasswordError = "Passwords must be identical"
                            else -> coroutineScope.launch {
                                onPasswordChange(password)
                                onDismiss()
                            }
                        }
                    },
                    shape = RoundedCornerShape(5.dp),
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                ) {
                    Text("Change Password")
                }
            }
        }
    )
}
