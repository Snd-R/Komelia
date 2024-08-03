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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.common.PasswordTextField
import snd.komga.client.user.KomgaUser
import kotlinx.coroutines.launch

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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PasswordChangeDialog(
    onPasswordChange: suspend (String) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        val focusManager = LocalFocusManager.current
        Surface(
            Modifier
                .width(400.dp)
                .height(400.dp)
                .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) },
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(40.dp),
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.padding(20.dp)
            ) {
                val (first, second) = remember { FocusRequester.createRefs() }

                Text("Change password")

                var password by remember { mutableStateOf("") }
                var passwordError by remember { mutableStateOf<String?>(null) }
                PasswordTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "New password",
                    error = passwordError,
                    modifier = Modifier.fillMaxWidth()
                        .focusRequester(first)
                        .focusProperties { next = second }
                )

                var repeatPassword by remember { mutableStateOf("") }
                var repeatPasswordError by remember { mutableStateOf<String?>(null) }
                PasswordTextField(
                    value = repeatPassword,
                    onValueChange = { repeatPassword = it },
                    label = "Repeat new password",
                    error = repeatPasswordError,
                    modifier = Modifier.fillMaxWidth()
                        .focusRequester(second)
                )

                val coroutineScope = rememberCoroutineScope()

                Spacer(Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    Spacer(Modifier.weight(1f))

                    TextButton(onClick = onDismiss) {
                        Text("CANCEL")
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
                    ) {
                        Text("CHANGE PASSWORD")
                    }
                }
            }
        }
    }
}
