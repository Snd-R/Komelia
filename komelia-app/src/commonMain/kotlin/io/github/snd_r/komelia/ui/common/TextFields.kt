package io.github.snd_r.komelia.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.cursorForHand

@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable () -> Unit,
    supportingText: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
    modifier: Modifier = Modifier
) {
    var passwordVisible by remember { mutableStateOf(false) }

    TextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        label = label,
        singleLine = true,
        supportingText = supportingText,
        isError = isError,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            val image = if (passwordVisible)
                Icons.Default.Visibility
            else Icons.Default.VisibilityOff

            val description = if (passwordVisible) "Hide password" else "Show password"

            IconButton(
                onClick = { passwordVisible = !passwordVisible },
                modifier = Modifier.cursorForHand()
            ) {
                Icon(imageVector = image, description)
            }
        },
        modifier = modifier.withTextFieldNavigation()
    )
}

@Composable
fun Modifier.withTextFieldNavigation(onEnterPress: (() -> Unit)? = null): Modifier {
    val focusManager = LocalFocusManager.current

    return this.then(
        Modifier.onPreviewKeyEvent { event ->
            if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false

            when (event.key) {
                Key.Enter -> {
                    onEnterPress?.let { it() }
                    true
                }

                Key.Tab -> {
                    focusManager.moveFocus(FocusDirection.Next)
                    true
                }

                else -> false
            }
        }
    )
}


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun NoPaddingTextField(
    text: String,
    placeholder: String,
    onTextChange: (String) -> Unit,
    shape: Shape = RoundedCornerShape(5.dp),
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
    trailingIcon: @Composable () -> Unit = {},
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val isFocused = interactionSource.collectIsFocusedAsState()
    val textColor =
        if (isFocused.value) MaterialTheme.colorScheme.onSurface
        else MaterialTheme.colorScheme.onPrimaryContainer

    val textStyle = MaterialTheme.typography.bodyLarge.copy(color = textColor)

    BasicTextField(
        value = text,
        onValueChange = onTextChange,
        modifier = modifier,
        singleLine = true,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        interactionSource = interactionSource,
        textStyle = textStyle,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions
    ) { innerTextField ->
        OutlinedTextFieldDefaults.DecorationBox(
            value = text,
            innerTextField = innerTextField,
            placeholder = { Text(placeholder, style = textStyle) },
            enabled = true,
            singleLine = true,
            visualTransformation = VisualTransformation.None,
            interactionSource = interactionSource,
            trailingIcon = trailingIcon,
            contentPadding = TextFieldDefaults.contentPaddingWithoutLabel(
                top = 0.dp,
                bottom = 0.dp
            ),
            container = {
                OutlinedTextFieldDefaults.ContainerBox(
                    enabled = true,
                    isError = false,
                    interactionSource = interactionSource,
                    colors = colors,
                    shape = shape
                )
            }
        )
    }
}

val httpRegex = "https?://".toRegex()

@Composable
fun HttpTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
    singleLine: Boolean = false,
) {
    val strippedValue by remember(value) { mutableStateOf(value.replace(httpRegex, "")) }
    var isHttps by remember(value) { mutableStateOf(value.startsWith("https://")) }
    val httpText = derivedStateOf { if (isHttps) "https://" else "http://" }
    TextField(
        value = strippedValue,
        onValueChange = { onValueChange(httpText.value + it) },
        modifier = modifier,
        prefix = {
            HttpPrefixButton(
                httpText = httpText.value,
                https = isHttps,
                onHttpsChange = {
                    isHttps = it
                    onValueChange(httpText.value + strippedValue)
                }
            )
        },
        label = label,
        placeholder = placeholder,
        isError = isError,
        supportingText = supportingText,
        singleLine = singleLine,
    )
}

@Composable
fun OutlinedHttpTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    singleLine: Boolean = false,
) {
    var isHttps by remember { mutableStateOf(false) }
    val httpText = remember(isHttps) { if (isHttps) "https://" else "http://" }
    val strippedValue by remember(value) { mutableStateOf(value.replace(httpRegex, "")) }

    OutlinedTextField(
        value = strippedValue,
        onValueChange = { onValueChange(httpText + it) },
        modifier = modifier,
        prefix = {
            HttpPrefixButton(
                httpText = httpText,
                https = isHttps,
                onHttpsChange = { isHttps = it }
            )
        },
        label = label,
        placeholder = placeholder,
        supportingText = supportingText,
        singleLine = singleLine,
    )
}

@Composable
private fun HttpPrefixButton(
    httpText: String,
    https: Boolean,
    onHttpsChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .clickable { onHttpsChange(!https) }
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .2f))
            .cursorForHand()
    ) {
        Icon(
            if (https) Icons.Default.Lock else Icons.Default.LockOpen,
            contentDescription = null,
            tint = if (https) MaterialTheme.colorScheme.tertiaryContainer else LocalContentColor.current
        )
        Text(httpText)
    }
}
