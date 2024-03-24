package io.github.snd_r.komelia.image.coil

import coil3.map.Mapper
import coil3.request.Options
import java.nio.file.Path

class PathMapper : Mapper<Path, String> {

    override fun map(data: Path, options: Options): String {
        return data.toUri().toString()
    }
}