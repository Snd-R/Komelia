package snd.komelia.db.migrations

import kotlinx.coroutines.runBlocking
import org.flywaydb.core.api.ClassProvider
import org.flywaydb.core.api.ResourceProvider
import org.flywaydb.core.api.migration.JavaMigration
import org.flywaydb.core.api.resource.LoadableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import java.io.InputStreamReader
import java.io.Reader

@OptIn(ExperimentalResourceApi::class)
abstract class MigrationResourcesProvider : ResourceProvider, ClassProvider<JavaMigration> {

    override fun getResource(name: String): LoadableResource? {
        return runBlocking {
            getMigration(name)?.let { MigrationResource(name, it) }
        }
    }

    override fun getResources(prefix: String, suffixes: Array<String>): Collection<LoadableResource> {
        if (prefix != "V" || !suffixes.contains(".sql")) {
            return emptyList()
        }

        return runBlocking {
            getMigrations().map { (name, content) -> MigrationResource(name, content) }
        }
    }

    protected abstract suspend fun getMigration(name: String): ByteArray?
    protected abstract suspend fun getMigrations(): Map<String, ByteArray>

    override fun getClasses(): Collection<Class<JavaMigration>> {
        return emptyList()
    }

    private class MigrationResource(
        private val name: String,
        private val content: ByteArray
    ) : LoadableResource() {

        override fun getAbsolutePath(): String {
            return name
        }

        override fun getAbsolutePathOnDisk(): String {
            return name
        }

        override fun getFilename(): String {
            return name
        }

        override fun getRelativePath(): String {
            return name
        }

        override fun read(): Reader {
            return InputStreamReader(content.inputStream())
        }
    }
}