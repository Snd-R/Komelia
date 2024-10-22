package io.github.snd_r.komelia.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType.Companion.PrimaryNotEditable
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
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.PlatformType
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.ui.LocalPlatform
import snd.komga.client.book.KomgaBook
import snd.komga.client.book.MediaProfile.DIVINA
import snd.komga.client.book.MediaProfile.EPUB
import snd.komga.client.book.MediaProfile.PDF

@Composable
fun readIsSupported(book: KomgaBook) =
    LocalPlatform.current == PlatformType.DESKTOP || book.media.mediaProfile != EPUB

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
        shape = RoundedCornerShape(7.dp),
        modifier = modifier.cursorForHand(),
        color = containerColor,
        contentColor = contentColor,
    ) {
        Row(Modifier.height(40.dp)) {
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
        Icon(Icons.AutoMirrored.Rounded.MenuBook, null)
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