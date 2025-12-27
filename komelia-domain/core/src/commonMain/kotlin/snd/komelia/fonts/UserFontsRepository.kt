package snd.komelia.fonts

interface UserFontsRepository {
    suspend fun getAllFonts(): List<UserFont>
    suspend fun getFont(name: String): UserFont?
    suspend fun putFont(font: UserFont)
    suspend fun deleteFont(font: UserFont)
}