package snd.komelia.image.coil

import coil3.map.Mapper
import coil3.request.Options
import io.github.vinceglb.filekit.PlatformFile

class FileMapper : Mapper<PlatformFile, String> {
    override fun map(data: PlatformFile, options: Options): String? {
        return data.toString()
    }
}
