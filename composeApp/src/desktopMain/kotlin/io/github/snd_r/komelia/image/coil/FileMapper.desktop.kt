package io.github.snd_r.komelia.image.coil

import coil3.map.Mapper
import coil3.request.Options
import com.darkrockstudios.libraries.mpfilepicker.PlatformFile

class FileMapper : Mapper<PlatformFile, String> {
    override fun map(data: PlatformFile, options: Options): String {
        return data.file.path
    }
}
