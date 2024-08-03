package io.github.snd_r.komelia.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.common.withTextFieldNavigation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun LoginContent(
    url: String,
    onUrlChange: (String) -> Unit,
    user: String,
    onUserChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    userLoginError: String?,
    autoLoginError: String?,
    onAutoLoginRetry: () -> Unit,
    onLogin: () -> Unit,
) {

    var showAutoLoginError by remember { mutableStateOf(true) }
    if (autoLoginError != null && showAutoLoginError) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                autoLoginError,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onAutoLoginRetry) { Text("Retry") }
                Button(onClick = { showAutoLoginError = false }) { Text("Login with another credentials") }
            }
        }
    } else {
        LoginForm(
            url = url,
            onUrlChange = onUrlChange,
            user = user,
            onUserChange = onUserChange,
            password = password,
            onPasswordChange = onPasswordChange,
            errorMessage = userLoginError,
            onLogin = onLogin
        )
    }

}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LoginForm(
    url: String,
    onUrlChange: (String) -> Unit,
    user: String,
    onUserChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    errorMessage: String?,
    onLogin: () -> Unit,
) {

    val coroutineScope = rememberCoroutineScope()
    Box(
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Komga Login")

            val (first, second, third) = remember { FocusRequester.createRefs() }

            OutlinedTextField(
                value = url,
                onValueChange = onUrlChange,
                label = { Text("Server Url") },
                modifier = Modifier
                    .withTextFieldNavigation()
                    .focusRequester(first)
                    .focusProperties { next = second },
                placeholder = { Text("http://localhost:25600") }
            )

            OutlinedTextField(
                value = user,
                onValueChange = onUserChange,
                label = { Text("Username") },
                modifier = Modifier
                    .withTextFieldNavigation()
                    .focusRequester(second)
                    .focusProperties { next = third }
            )

            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                visualTransformation = PasswordVisualTransformation(),
                label = { Text("Password") },
                modifier = Modifier
                    .withTextFieldNavigation(
                        onEnterPress = { coroutineScope.launch { onLogin() } }
                    )
                    .focusRequester(third)
            )

            if (errorMessage != null) {
                Text(errorMessage, style = TextStyle(color = MaterialTheme.colorScheme.error))
            }

            Button(onClick = { onLogin() }) {
                Text("Login")
            }


        }
    }

}


@Composable
fun LoginLoadingContent(onCancel: () -> Unit) {
    var showCancelButton by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(5000)
        showCancelButton = true
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        CircularProgressIndicator()
        if (showCancelButton) {
            Spacer(Modifier.height(100.dp))
            Button(onClick = onCancel) { Text("Cancel login attempt") }
        }

    }


}