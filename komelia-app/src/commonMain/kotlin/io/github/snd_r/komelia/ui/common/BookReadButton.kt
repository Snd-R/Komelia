package io.github.snd_r.komelia.ui.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.cursorForHand

@Composable
 fun BookReadButton(onClick: () -> Unit) {
    FilledTonalButton(
        modifier = Modifier.padding(horizontal = 5.dp).cursorForHand(),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiary
        ),
        onClick = onClick,
        contentPadding = PaddingValues(vertical = 5.dp, horizontal = 15.dp)

    ) {
        Icon(Icons.AutoMirrored.Rounded.MenuBook, null)
        Spacer(Modifier.width(10.dp))

        Text("Read")
    }
}
