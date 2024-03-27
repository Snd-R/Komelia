package io.github.snd_r.komelia.ui.common

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun ExpandableText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current
) {
    var isExpanded by remember { mutableStateOf(false) }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val isExpandable by remember { derivedStateOf { textLayoutResult?.didOverflowHeight ?: false } }
    val isButtonShown by remember { derivedStateOf { isExpandable || isExpanded } }


    Column(
        modifier = Modifier.animateContentSize(spring(stiffness = Spring.StiffnessLow))
            .then(modifier)
    ) {
        Text(
            text = text,
            maxLines = if (isExpanded) Int.MAX_VALUE else 10,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { textLayoutResult = it },
            style = style
        )

        if (isButtonShown) {

            TextButton(
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier.fillMaxWidth(),
                shape = RectangleShape,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
            ) {
                Text(
                    text = (if (isExpanded) "Collapse" else "Expand").uppercase()
                )
            }
        }
    }
}
