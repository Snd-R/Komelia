package io.github.snd_r.komelia.ui.settings.komf.notifications

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.snd_r.komelia.AppNotification
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.settings.komf.KomfConfigState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import snd.komf.api.PatchValue.Some
import snd.komf.api.config.DiscordConfigUpdateRequest
import snd.komf.api.config.KomfConfig
import snd.komf.api.config.KomfConfigUpdateRequest
import snd.komf.api.config.NotificationConfigUpdateRequest
import snd.komf.api.notifications.EmbedField
import snd.komf.api.notifications.EmbedFieldTemplate
import snd.komf.api.notifications.KomfAlternativeTitleContext
import snd.komf.api.notifications.KomfAuthorContext
import snd.komf.api.notifications.KomfBookContext
import snd.komf.api.notifications.KomfBookMetadataContext
import snd.komf.api.notifications.KomfDiscordTemplates
import snd.komf.api.notifications.KomfLibraryContext
import snd.komf.api.notifications.KomfNotificationContext
import snd.komf.api.notifications.KomfSeriesContext
import snd.komf.api.notifications.KomfSeriesMetadataContext
import snd.komf.api.notifications.KomfTemplateRequest
import snd.komf.api.notifications.KomfWebLinkContext
import snd.komf.client.KomfConfigClient
import snd.komf.client.KomfNotificationClient
import snd.komga.client.library.KomgaLibrary

class KomfNotificationSettingsViewModel(
    private val komfConfigClient: KomfConfigClient,
    private val komfNotificationClient: KomfNotificationClient,
    private val appNotifications: AppNotifications,
    val komfConfig: KomfConfigState,
    val libraries: StateFlow<List<KomgaLibrary>>,
) : StateScreenModel<LoadState<Unit>>(LoadState.Uninitialized) {

    var discordUploadSeriesCover by mutableStateOf(false)
        private set
    var discordWebhooks by mutableStateOf(emptyList<String>())
        private set

    var titleTemplate by mutableStateOf("")
    var titleUrlTemplate by mutableStateOf("")
    var descriptionTemplate by mutableStateOf("")
    var footerTemplate by mutableStateOf("")
    var fieldTemplates by mutableStateOf<List<EmbedFieldState>>(emptyList())

    var titlePreview by mutableStateOf("")
    var titleUrlPreview by mutableStateOf("")
    var descriptionPreview by mutableStateOf("")
    var fieldPreviews by mutableStateOf<List<EmbedField>>(emptyList())
    var footerPreview by mutableStateOf("")

    val notificationContext = NotificationContextState()

    suspend fun initialize() {
        appNotifications.runCatchingToNotifications {
            val config = komfConfig.getConfig()
            config.onEach { initFields(it) }.launchIn(screenModelScope)

            val templates = komfNotificationClient.getTemplates()
            titleTemplate = templates.titleTemplate ?: ""
            titleUrlTemplate = templates.titleUrlTemplate ?: ""
            descriptionTemplate = templates.descriptionTemplate ?: ""
            fieldTemplates = templates.fields.map { EmbedFieldState(it.nameTemplate, it.valueTemplate, it.inline) }
            footerTemplate = templates.footerTemplate ?: ""
        }
            .onFailure { mutableState.value = LoadState.Error(it) }
            .onSuccess { mutableState.value = LoadState.Success(Unit) }
    }

    private fun initFields(config: KomfConfig) {
        discordUploadSeriesCover = config.notifications.discord.seriesCover
        discordWebhooks = config.notifications.discord.webhooks ?: emptyList()
    }

    fun onSeriesCoverChange(seriesCover: Boolean) {
        discordUploadSeriesCover = seriesCover
        val discordUpdate = DiscordConfigUpdateRequest(seriesCover = Some(seriesCover))
        val notificationUpdate = NotificationConfigUpdateRequest(discord = Some(discordUpdate))
        onConfigUpdate(KomfConfigUpdateRequest(notifications = Some(notificationUpdate)))
    }

    fun onDiscordWebhookAdd(webhook: String) {
        val webhooks = discordWebhooks.plus(webhook)
        this.discordWebhooks = webhooks

        val discordUpdate = DiscordConfigUpdateRequest(webhooks = Some(webhooks))
        val notificationUpdate = NotificationConfigUpdateRequest(discord = Some(discordUpdate))
        onConfigUpdate(KomfConfigUpdateRequest(notifications = Some(notificationUpdate)))
    }

    fun onDiscordWebhookRemove(webhook: String) {
        val webhooks = discordWebhooks.minus(webhook)
        this.discordWebhooks = webhooks

        val discordUpdate = DiscordConfigUpdateRequest(webhooks = Some(webhooks))
        val notificationUpdate = NotificationConfigUpdateRequest(discord = Some(discordUpdate))
        onConfigUpdate(KomfConfigUpdateRequest(notifications = Some(notificationUpdate)))
    }

    fun onTemplatesSend() {
        appNotifications.runCatchingToNotifications(screenModelScope) {
            komfNotificationClient.send(
                KomfTemplateRequest(
                    context = getKomfNotificationContext(),
                    templates = getKomfDiscordTemplates()
                )
            )
        }
    }

    fun onTemplatesSave() {
        appNotifications.runCatchingToNotifications(screenModelScope) {
            komfNotificationClient.updateTemplates(getKomfDiscordTemplates())
            appNotifications.add(AppNotification.Success("Templates Saved"))
        }

    }

    private fun onConfigUpdate(request: KomfConfigUpdateRequest) {
        screenModelScope.launch {
            appNotifications.runCatchingToNotifications { komfConfigClient.updateConfig(request) }
                .onFailure { initFields(komfConfig.getConfig().first()) }
        }
    }

    fun onTitleUrlTemplateChange(titleUrl: String) {
        titleUrlTemplate = if (titleUrl == "http://" || titleUrl == "https://") ""
        else titleUrl
    }

    fun onTemplateRender() {
        appNotifications.runCatchingToNotifications(screenModelScope) {
            val rendered = komfNotificationClient.render(
                KomfTemplateRequest(
                    context = getKomfNotificationContext(),
                    templates = getKomfDiscordTemplates()
                )
            )

            titlePreview = rendered.title ?: ""
            titleUrlPreview = rendered.titleUrl ?: ""
            descriptionPreview = rendered.description ?: ""
            fieldPreviews = rendered.fields
            footerPreview = rendered.footer ?: ""

        }
    }

    private fun getKomfDiscordTemplates() = KomfDiscordTemplates(
        titleTemplate = titleTemplate,
        titleUrlTemplate = titleUrlTemplate,
        fields = fieldTemplates.map {
            EmbedFieldTemplate(
                nameTemplate = it.nameTemplate,
                valueTemplate = it.valueTemplate,
                inline = it.inline
            )
        },
        descriptionTemplate = descriptionTemplate,
        footerTemplate = footerTemplate
    )

    private fun getKomfNotificationContext() = KomfNotificationContext(
        library = KomfLibraryContext(
            id = notificationContext.libraryId,
            name = notificationContext.libraryName
        ),
        series = KomfSeriesContext(
            id = notificationContext.seriesId,
            name = notificationContext.seriesName,
            bookCount = notificationContext.seriesBookCount ?: 0,
            metadata = KomfSeriesMetadataContext(
                status = notificationContext.seriesStatus,
                title = notificationContext.seriesTitle,
                titleSort = notificationContext.seriesTitleSort,
                alternativeTitles = notificationContext.seriesAlternativeTitles.map {
                    KomfAlternativeTitleContext(it.label, it.title)
                },
                summary = notificationContext.seriesSummary,
                readingDirection = notificationContext.seriesReadingDirection,
                publisher = notificationContext.seriesPublisher,
                alternativePublishers = notificationContext.seriesAlternativePublishers.toSet(),
                ageRating = notificationContext.seriesAgeRating,
                language = notificationContext.seriesLanguage,
                genres = notificationContext.seriesGenres,
                tags = notificationContext.seriesTags,
                totalBookCount = notificationContext.seriesTotalBookCount,
                authors = notificationContext.seriesAuthors.map { KomfAuthorContext(it.name, it.role) },
                releaseYear = notificationContext.seriesReleaseYer,
                links = notificationContext.seriesLinks.map { KomfWebLinkContext(it.label, it.url) }
            )
        ),
        books = notificationContext.books.map { book ->
            KomfBookContext(
                id = book.id,
                name = book.name,
                number = book.number,
                metadata = KomfBookMetadataContext(
                    title = book.title,
                    summary = book.summary,
                    number = book.metadataNumber,
                    numberSort = book.metadataNumberSort,
                    releaseDate = book.releaseDate,
                    authors = book.authors.map { KomfAuthorContext(it.name, it.role) },
                    tags = book.tags,
                    isbn = book.isbn,
                    links = book.links.map { KomfWebLinkContext(it.label, it.url) }
                )
            )
        }
    )

    fun onFieldAdd() {
        fieldTemplates = fieldTemplates + EmbedFieldState()
    }

    fun onFieldDelete(field: EmbedFieldState) {
        fieldTemplates = fieldTemplates - field
    }

    class EmbedFieldState(
        nameTemplate: String = "",
        valueTemplate: String = "",
        inline: Boolean = false
    ) {
        var nameTemplate by mutableStateOf(nameTemplate)
        var valueTemplate by mutableStateOf(valueTemplate)
        var inline by mutableStateOf(inline)
    }

    class NotificationContextState {
        var libraryId by mutableStateOf("1")
        var libraryName by mutableStateOf("Test library")

        var seriesId by mutableStateOf("2")
        var seriesName by mutableStateOf("TestSeries")
        var seriesBookCount by mutableStateOf<Int?>(1)
        var seriesStatus by mutableStateOf("ONGOING")
        var seriesTitle by mutableStateOf("Series Title")
        var seriesTitleSort by mutableStateOf("Series Title")
        var seriesAlternativeTitles by mutableStateOf<List<AlternativeTitleContext>>(emptyList())
        var seriesSummary by mutableStateOf("Series Summary")
        var seriesReadingDirection by mutableStateOf("LEFT_TO_RIGHT")
        var seriesPublisher by mutableStateOf("Series Publisher")
        var seriesAlternativePublishers by mutableStateOf<List<String>>(emptyList())
        var seriesAgeRating by mutableStateOf<Int?>(18)
        var seriesLanguage by mutableStateOf("")
        var seriesGenres by mutableStateOf(listOf("genre1", "genre2"))
        var seriesTags by mutableStateOf(listOf("tag1", "tag2"))
        var seriesTotalBookCount by mutableStateOf<Int?>(2)
        var seriesAuthors by mutableStateOf(
            listOf(
                AuthorContext("Author1", "Writer"),
                AuthorContext("Author2", "Artist")
            )
        )
        var seriesReleaseYer by mutableStateOf<Int?>(1970)
        var seriesLinks by mutableStateOf(listOf(WebLinkContext("Example link", "http://example.com")))
        var books by mutableStateOf(listOf(BookContextState()))

        fun onSeriesAlternativeTitleAdd() {
            seriesAlternativeTitles += AlternativeTitleContext()

        }

        fun onSeriesAlternativeTitleDelete(title: AlternativeTitleContext) {
            seriesAlternativeTitles -= title
        }

        fun onSeriesAuthorAdd() {
            seriesAuthors += AuthorContext()

        }

        fun onSeriesAuthorDelete(author: AuthorContext) {
            seriesAuthors -= author
        }

        fun onSeriesLinkAdd() {
            seriesLinks += WebLinkContext()

        }

        fun onSeriesLinkDelete(link: WebLinkContext) {
            seriesLinks -= link
        }

        fun onBookAdd() {
            books += BookContextState()
        }

        fun onBookDelete(book: BookContextState) {
            books -= book
        }

        class BookContextState {
            var id by mutableStateOf("3")
            var name by mutableStateOf("TestBook")
            var number by mutableStateOf(1)
            var title by mutableStateOf("Book Metadata Title")
            var summary by mutableStateOf("Book summary")
            var metadataNumber by mutableStateOf("1")
            var metadataNumberSort by mutableStateOf("1")
            var releaseDate by mutableStateOf("1970-01-01")
            var authors by mutableStateOf(
                listOf(
                    AuthorContext("Author1", "Writer"),
                    AuthorContext("Author2", "Artist")
                )
            )
            var tags by mutableStateOf(listOf("bookTag1", "bookTag2"))
            var isbn by mutableStateOf("9780000000000")
            var links by mutableStateOf<List<WebLinkContext>>(emptyList())

            fun onAuthorAdd() {
                authors += AuthorContext()

            }

            fun onAuthorDelete(author: AuthorContext) {
                authors -= author
            }

            fun onLinkAdd() {
                links += WebLinkContext()

            }

            fun onLinkDelete(link: WebLinkContext) {
                links -= link
            }
        }

        class AlternativeTitleContext(label: String = "En", title: String = "Alternative Title") {
            var label by mutableStateOf(label)
            var title by mutableStateOf(title)
        }

        class AuthorContext(name: String = "Author", role: String = "Author Role") {
            var name by mutableStateOf(name)
            var role by mutableStateOf(role)
        }

        class WebLinkContext(label: String = "Example Link", url: String = "http://example.com") {
            var label by mutableStateOf(label)
            var url by mutableStateOf(url)
        }
    }

}