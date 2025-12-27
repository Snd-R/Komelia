package snd.komelia.ui.login.offline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import snd.komelia.offline.server.model.OfflineMediaServer
import snd.komelia.offline.server.model.OfflineMediaServerId
import snd.komelia.offline.user.model.OfflineUser
import snd.komelia.ui.settings.offline.users.RootUserCard
import snd.komelia.ui.settings.offline.users.ServerCard
import snd.komga.client.user.KomgaUserId

@Composable
fun OfflineLoginContent(
    serverUsers: Map<OfflineMediaServer, List<OfflineUser>>,
    loginAs: (KomgaUserId) -> Unit,
    onServerDelete: (OfflineMediaServerId) -> Unit,
    onUserDelete: (KomgaUserId) -> Unit,
    onReturnToLogin: () -> Unit
) {

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.widthIn(max = 600.dp)
    ) {
        Text("Offline mode", style = MaterialTheme.typography.titleLarge)

        for ((server, users) in serverUsers) {
            ServerCard(
                server = server,
                users = users,
                onServerDelete = onServerDelete,
                goOffline = loginAs,
                onUserDelete = onUserDelete,
                expandByDefault = serverUsers.size == 1
            )
        }

        if (serverUsers.size > 1) {
            RootUserCard({ loginAs(OfflineUser.ROOT) })
        }

        Button(onClick = onReturnToLogin, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Back to online")
        }
    }
}