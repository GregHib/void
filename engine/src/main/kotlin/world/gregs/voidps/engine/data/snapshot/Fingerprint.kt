package world.gregs.voidps.engine.data.snapshot

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor

/**
 * A hash of the shape of a serialized type: names, kinds, element names (so enum entries too) and nullability, recursively.
 * Changes whenever the binary layout a [SnapshotFormat] would give it could change; stable between runs.
 */
@OptIn(ExperimentalSerializationApi::class)
fun SerialDescriptor.fingerprint(): Long = fingerprint(ArrayList())

@OptIn(ExperimentalSerializationApi::class)
private fun SerialDescriptor.fingerprint(path: MutableList<String>): Long {
    var hash = serialName.hashCode().toLong()
    hash = hash * 31 + kind.toString().hashCode()
    hash = hash * 31 + if (isNullable) 1 else 0
    // Recursive types only need hashing once
    if (path.contains(serialName)) {
        return hash
    }
    path.add(serialName)
    for (i in 0 until elementsCount) {
        val element = getElementDescriptor(i)
        hash = hash * 31 + getElementName(i).hashCode()
        hash = hash * 31 + if (element.isNullable) 1 else 0
        hash = hash * 31 + element.fingerprint(path)
    }
    path.removeAt(path.lastIndex)
    return hash
}
