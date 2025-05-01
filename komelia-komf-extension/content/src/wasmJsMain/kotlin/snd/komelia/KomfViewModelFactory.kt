package snd.komelia

import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.settings.KomfSettingsRepository
import io.github.snd_r.komelia.ui.dialogs.komf.identify.KomfIdentifyDialogViewModel
import io.github.snd_r.komelia.ui.dialogs.komf.identify.KomfLibraryIdentifyViewmodel
import io.github.snd_r.komelia.ui.dialogs.komf.reset.KomfResetMetadataDialogViewModel
import io.github.snd_r.komelia.ui.settings.komf.KomfSharedState
import io.github.snd_r.komelia.ui.settings.komf.general.KomfSettingsViewModel
import io.github.snd_r.komelia.ui.settings.komf.jobs.KomfJobsViewModel
import io.github.snd_r.komelia.ui.settings.komf.notifications.KomfNotificationSettingsViewModel
import io.github.snd_r.komelia.ui.settings.komf.processing.KomfProcessingSettingsViewModel
import io.github.snd_r.komelia.ui.settings.komf.providers.KomfProvidersSettingsViewModel
import snd.komf.api.KomfServerLibraryId
import snd.komf.api.KomfServerSeriesId
import snd.komf.api.MediaServer
import snd.komf.api.MediaServer.KAVITA
import snd.komf.api.MediaServer.KOMGA
import snd.komf.client.KomfClientFactory

class KomfViewModelFactory(
    val komfClientFactory: KomfClientFactory,
    val appNotifications: AppNotifications,
    val settingsRepository: KomfSettingsRepository
) {
    private val komfSharedState = KomfSharedState(
        komfConfigClient = komfClientFactory.configClient(),
        komgaServerClient = komfClientFactory.mediaServerClient(KOMGA),
        kavitaServerClient = komfClientFactory.mediaServerClient(KAVITA),
        notifications = appNotifications,
    )

    fun getKomfSettingsViewModel(
        mediaServer: MediaServer
    ): KomfSettingsViewModel {
        return KomfSettingsViewModel(
            komfConfigClient = komfClientFactory.configClient(),
            komgaMediaServerClient = komfClientFactory.mediaServerClient(KOMGA),
            kavitaMediaServerClient = if (mediaServer == KAVITA) komfClientFactory.mediaServerClient(KAVITA) else null,
            appNotifications = appNotifications,
            settingsRepository = settingsRepository,
            integrationToggleEnabled = false,
            komfSharedState = komfSharedState,
        )
    }

    fun getKomfNotificationViewModel(): KomfNotificationSettingsViewModel {
        return KomfNotificationSettingsViewModel(
            komfConfigClient = komfClientFactory.configClient(),
            komfNotificationClient = komfClientFactory.notificationClient(),
            appNotifications = appNotifications,
            komfConfig = komfSharedState
        )
    }

    fun getKomfProcessingViewModel(serverType: MediaServer): KomfProcessingSettingsViewModel {
        return KomfProcessingSettingsViewModel(
            komfConfigClient = komfClientFactory.configClient(),
            appNotifications = appNotifications,
            serverType = serverType,
            komfSharedState = komfSharedState
        )
    }

    fun getKomfProvidersViewModel(): KomfProvidersSettingsViewModel {
        return KomfProvidersSettingsViewModel(
            komfConfigClient = komfClientFactory.configClient(),
            appNotifications = appNotifications,
            komfSharedState = komfSharedState
        )
    }

    fun getKomfJobsViewModel(): KomfJobsViewModel {
        return KomfJobsViewModel(
            jobClient = komfClientFactory.jobClient(),
            seriesClient = null,
            appNotifications = appNotifications
        )
    }

    fun getKomfIdentifyDialogViewModel(
        seriesId: KomfServerSeriesId,
        libraryId: KomfServerLibraryId,
        seriesName: String,
        onDismissRequest: () -> Unit
    ): KomfIdentifyDialogViewModel {
        return KomfIdentifyDialogViewModel(
            seriesId = seriesId,
            libraryId = libraryId,
            seriesName = seriesName,
            komfConfig = komfSharedState,
            komfMetadataClient = komfClientFactory.metadataClient(KOMGA),
            komfJobClient = komfClientFactory.jobClient(),
            appNotifications = appNotifications,
            onDismiss = onDismissRequest,
        )
    }

    fun getKomfResetMetadataDialogViewModel(
        onDismissRequest: () -> Unit
    ): KomfResetMetadataDialogViewModel {
        return KomfResetMetadataDialogViewModel(
            komfMetadataClient = komfClientFactory.metadataClient(KOMGA),
            appNotifications = appNotifications,
            onDismiss = onDismissRequest,
        )
    }

    fun getKomfLibraryIdentifyViewModel(
        libraryId: KomfServerLibraryId
    ): KomfLibraryIdentifyViewmodel {
        return KomfLibraryIdentifyViewmodel(
            libraryId = libraryId,
            komfMetadataClient = komfClientFactory.metadataClient(KOMGA),
            appNotifications = appNotifications,
        )
    }
}