package io.github.snd_r.komelia.ui.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.PlatformType
import io.github.snd_r.komelia.platform.PlatformType.DESKTOP
import io.github.snd_r.komelia.platform.PlatformType.MOBILE
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.ui.LocalPlatform
import io.github.snd_r.komelia.ui.common.OutlinedHttpTextField
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
                Button(onClick = { showAutoLoginError = false }) { Text("Login with another account") }
            }
        }
    } else {
        val platform = LocalPlatform.current
        when (platform) {
            MOBILE, DESKTOP -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Komga Login")
                LoginForm(
                    url = url,
                    onUrlChange = onUrlChange,
                    user = user,
                    onUserChange = onUserChange,
                    password = password,
                    onPasswordChange = onPasswordChange,
                    errorMessage = userLoginError,
                    onLogin = onLogin,
                    textFieldsModifier = Modifier
                )
            }

            PlatformType.WEB_KOMF -> Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val uriHandler = LocalUriHandler.current
                Column {
                    Text("Requires access from the same host as komga")
                    Text(
                        "Requires adding this host and port to Komga CORS configuration",
                        color = MaterialTheme.colorScheme.secondary,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable {
                            uriHandler.openUri("https://komga.org/docs/installation/configuration/#komga_cors_allowed_origins--komgacorsallowed-origins-origins")
                        }.padding(2.dp).cursorForHand()
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LoginForm(
                        url = url,
                        onUrlChange = onUrlChange,
                        user = user,
                        onUserChange = onUserChange,
                        password = password,
                        onPasswordChange = onPasswordChange,
                        errorMessage = userLoginError,
                        onLogin = onLogin,
                        textFieldsModifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

    }

}

@Composable
fun ColumnScope.LoginForm(
    url: String,
    onUrlChange: (String) -> Unit,
    user: String,
    onUserChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    errorMessage: String?,
    onLogin: () -> Unit,
    textFieldsModifier: Modifier
) {

    val coroutineScope = rememberCoroutineScope()
    val (first, second, third) = remember { FocusRequester.createRefs() }

    OutlinedHttpTextField(
        value = url,
        onValueChange = onUrlChange,
        label = { Text("Server Url") },
        modifier = textFieldsModifier
            .withTextFieldNavigation()
            .focusRequester(first)
            .focusProperties { next = second },
        placeholder = { Text("localhost:25600") }
    )

    OutlinedTextField(
        value = user,
        onValueChange = onUserChange,
        label = { Text("Username") },
        modifier = textFieldsModifier
            .withTextFieldNavigation()
            .focusRequester(second)
            .focusProperties { next = third }
    )

    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        visualTransformation = PasswordVisualTransformation(),
        label = { Text("Password") },
        modifier = textFieldsModifier
            .withTextFieldNavigation(
                onEnterPress = { coroutineScope.launch { onLogin() } }
            )
            .focusRequester(third),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
    )

    if (errorMessage != null) {
        Text(errorMessage, style = TextStyle(color = MaterialTheme.colorScheme.error))
    }

    Button(onClick = { onLogin() }) {
        Text("Login")
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