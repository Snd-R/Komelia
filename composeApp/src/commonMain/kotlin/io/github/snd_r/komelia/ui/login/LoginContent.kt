package io.github.snd_r.komelia.ui.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import io.github.snd_r.komelia.ui.common.withTextFieldKeyMapping
import kotlinx.coroutines.launch


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LoginContent(
    url: String,
    onUrlChange: (String) -> Unit,
    user: String,
    onUserChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    errorMessage: String?,
    onLogin: suspend () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    Box(
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Login")

            val (first, second, third) = remember { FocusRequester.createRefs() }

            OutlinedTextField(
                value = url,
                onValueChange = onUrlChange,
                label = { Text("Server Url") },
                modifier = Modifier
                    .withTextFieldKeyMapping()
                    .focusRequester(first)
                    .focusProperties { next = second }
            )

            OutlinedTextField(
                value = user,
                onValueChange = onUserChange,
                label = { Text("Username") },
                modifier = Modifier
                    .withTextFieldKeyMapping()
                    .focusRequester(second)
                    .focusProperties { next = third }
            )

            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                visualTransformation = PasswordVisualTransformation(),
                label = { Text("Password") },
                modifier = Modifier
                    .withTextFieldKeyMapping(
                        onEnterPress = { coroutineScope.launch { onLogin() } }
                    )
                    .focusRequester(third)
            )

            if (errorMessage != null) {
                Text(errorMessage, style = TextStyle(color = MaterialTheme.colorScheme.error))
            }

            Button(onClick = { coroutineScope.launch { onLogin() } }) {
                Text("Login")
            }

        }
    }
}
