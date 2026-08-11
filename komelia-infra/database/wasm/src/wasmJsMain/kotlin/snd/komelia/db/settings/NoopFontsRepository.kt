package snd.komelia.db.settings

import snd.komelia.fonts.UserFont
import snd.komelia.fonts.UserFontsRepository

class NoopFontsRepository : UserFontsRepository {
    override suspend fun getAllFonts(): List<UserFont> {
        return emptyList()
    }

    override suspend fun getFont(name: String): UserFont? {
        return null
    }

    override suspend fun putFont(font: UserFont) {
    }

    override suspend fun deleteFont(font: UserFont) {
    }
}