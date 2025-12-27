package snd.komelia.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType.Companion.PrimaryNotEditable
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import snd.komelia.komga.api.model.KomeliaBook
import snd.komelia.ui.platform.cursorForHand
import snd.komga.client.book.MediaProfile.EPUB
import snd.webview.webviewIsAvailable

@Composable
fun readIsSupported(book: KomeliaBook) = book.media.mediaProfile != EPUB || webviewIsAvailable()

@Composable
fun BookReadButton(
    modifier: Modifier = Modifier,
    onRead: () -> Unit,
    onIncognitoRead: () -> Unit,
    onDropdownOpenChange: (Boolean) -> Unit = {}
) {
    val containerColor = MaterialTheme.colorScheme.tertiaryContainer
    val contentColor = MaterialTheme.colorScheme.onTertiary
    Surface(
        shape = CircleShape,
        modifier = modifier.semantics { role = Role.Button }.pointerHoverIcon(PointerIcon.Hand),
        color = containerColor,
        contentColor = contentColor,
    ) {
        Row(
            Modifier.height(40.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ReadButton(
                modifier = Modifier.padding(horizontal = 5.dp).fillMaxHeight(),
                onRead = onRead,
            )
            VerticalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            IncognitoDropDown(
                modifier = Modifier.padding(end = 5.dp).fillMaxHeight(),
                onIncognitoRead = onIncognitoRead,
                onDropdownOpenChange = onDropdownOpenChange
            )
        }
    }
}

@Composable
private fun ReadButton(
    modifier: Modifier,
    onRead: () -> Unit,
) {
    Row(
        modifier = Modifier.clickable { onRead() }.then(modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(5.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.MenuBook,
            contentDescription = null,
        )
        Spacer(Modifier.width(10.dp))
        Text("Read")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IncognitoDropDown(
    modifier: Modifier,
    onIncognitoRead: () -> Unit,
    onDropdownOpenChange: (Boolean) -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = {
            onDropdownOpenChange(it)
            isExpanded = it
        },
    ) {

        Box(
            modifier = Modifier
                .clickable { isExpanded = true }
                .menuAnchor(PrimaryNotEditable)
                .then(modifier),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.ExpandMore, null)
        }
        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = {
                onDropdownOpenChange(false)
                isExpanded = false
            },
            modifier = Modifier.width(150.dp)
        ) {
            DropdownMenuItem(
                text = { Text("Read incognito") },
                onClick = { onIncognitoRead() },
                modifier = Modifier.cursorForHand()
            )
        }
    }
}