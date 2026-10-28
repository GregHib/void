package world.gregs.voidps.engine.data.load

import world.gregs.voidps.engine.data.snapshot.SnapshotReader
import world.gregs.voidps.engine.data.snapshot.SnapshotWriter

interface SnapshotCodec<T> {
    fun write(writer: SnapshotWriter, value: T)

    fun read(reader: SnapshotReader): T

    /** Folded into the key so changing the shape of the data invalidates old snapshots. */
    val schema: Long
}
