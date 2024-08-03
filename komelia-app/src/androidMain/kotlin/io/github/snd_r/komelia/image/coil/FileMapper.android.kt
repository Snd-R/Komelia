package io.github.snd_r.komelia.image.coil

import android.net.Uri
import coil3.map.Mapper
import coil3.request.Options
import io.github.vinceglb.filekit.core.PlatformFile

class FileMapper : Mapper<PlatformFile, Uri> {
    override fun map(data: PlatformFile, options: Options): Uri {
        return data.uri
    }
}
