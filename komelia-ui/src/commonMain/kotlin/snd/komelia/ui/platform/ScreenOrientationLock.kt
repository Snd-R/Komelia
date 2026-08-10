package snd.komelia.ui.platform

import androidx.compose.runtime.Composable

/**
 * Locks the app to portrait orientation while [locked] is true, restoring whatever
 * orientation setting was in effect before once [locked] goes back to false (or the
 * composable leaves composition). No-op on platforms without an OS-level concept of
 * screen orientation (desktop, web).
 */
@Composable
expect fun LockScreenOrientation(locked: Boolean)
