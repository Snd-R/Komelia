package io.github.snd_r.komelia.ui.settings.decoder

import cafe.adriel.voyager.core.screen.Screen

interface DecoderSettingsScreen : Screen

expect fun getDecoderSettingsScreen(): DecoderSettingsScreen