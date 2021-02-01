package world.gregs.voidps.cache.format.definition.internal

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import world.gregs.voidps.cache.format.definition.Operation

@ExperimentalSerializationApi
fun SerialDescriptor.getOperationOrNull(index: Int) = getElementAnnotations(index).filterIsInstance<Operation>().firstOrNull()?.code