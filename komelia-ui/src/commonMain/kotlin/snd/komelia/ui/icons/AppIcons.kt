package snd.komelia.ui.icons

import androidx.compose.ui.graphics.vector.ImageVector
import snd.komelia.ui.icons.mangabaka.flags.FlagBr
import snd.komelia.ui.icons.mangabaka.flags.FlagCn
import snd.komelia.ui.icons.mangabaka.flags.FlagDe
import snd.komelia.ui.icons.mangabaka.flags.FlagFr
import snd.komelia.ui.icons.mangabaka.flags.FlagId
import snd.komelia.ui.icons.mangabaka.flags.FlagJp
import snd.komelia.ui.icons.mangabaka.flags.FlagKo
import snd.komelia.ui.icons.mangabaka.flags.FlagPt
import snd.komelia.ui.icons.mangabaka.flags.FlagRu
import snd.komelia.ui.icons.mangabaka.flags.FlagTh
import snd.komelia.ui.icons.mangabaka.flags.FlagUK
import snd.komelia.ui.icons.mangabaka.flags.FlagUS
import snd.komelia.ui.icons.mangabaka.flags.FlagVi

object AppIcons {
    object Outlined

    fun flagForLanguage(language: String, englishIsUsa: Boolean = false): ImageVector? {
        if (language.startsWith("zh")) return AppIcons.FlagCn
        return when (language) {
            "de" -> AppIcons.FlagDe
            "en" -> if (englishIsUsa) FlagUS else AppIcons.FlagUK
//            "es" -> AppIcons.FlagEs
            "fr" -> AppIcons.FlagFr
            "id" -> AppIcons.FlagId
            "ja", "ja-Latn" -> AppIcons.FlagJp
            "ko", "ko-Latn" -> AppIcons.FlagKo
            "pt" -> AppIcons.FlagPt
            "pt-br" -> AppIcons.FlagBr
            "ru" -> AppIcons.FlagRu
            "th" -> AppIcons.FlagTh
            "vi" -> AppIcons.FlagVi
            else -> null
        }
    }
}
