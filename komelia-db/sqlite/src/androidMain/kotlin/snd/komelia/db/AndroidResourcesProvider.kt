package snd.komelia.db

import io.github.snd_r.sqlite.generated.resources.Res
import kotlinx.coroutines.runBlocking
import org.flywaydb.core.api.ClassProvider
import org.flywaydb.core.api.ResourceProvider
import org.flywaydb.core.api.migration.JavaMigration
import org.flywaydb.core.api.resource.LoadableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import java.io.InputStreamReader
import java.io.Reader

@OptIn(ExperimentalResourceApi::class)
class AndroidResourcesProvider : ResourceProvider, ClassProvider<JavaMigration> {
    private val migrations = listOf(
        "V1__initial_migration.sql",
    )
    private val resources: Map<String, AndroidLoadableResource> = runBlocking {
        migrations.associateWith {
            AndroidLoadableResource(
                name = it,
                content = Res.readBytes("files/migrations/$it")
            )
        }
    }

    override fun getResource(name: String): LoadableResource? {
        return resources[name]
    }

    override fun getResources(prefix: String, suffixes: Array<String>): Collection<LoadableResource> {
        return if (prefix == "V" && suffixes.contains(".sql"))
            resources.values
        else emptyList()
    }

    override fun getClasses(): Collection<Class<JavaMigration>> {
        return emptyList()
    }

    private class AndroidLoadableResource(
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