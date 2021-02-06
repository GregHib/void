package world.gregs.voidps.cache.format.definition.internal

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import world.gregs.voidps.cache.format.definition.Indexed
import world.gregs.voidps.cache.format.definition.Operation
import world.gregs.voidps.cache.format.definition.Setter

@ExperimentalSerializationApi
fun SerialDescriptor.getOperationOrNull(index: Int) = getElementAnnotations(index).filterIsInstance<Operation>().firstOrNull()?.code

@ExperimentalSerializationApi
fun SerialDescriptor.getOperation(index: Int) = getElementAnnotations(index).filterIsInstance<Operation>().first().code

@ExperimentalSerializationApi
fun SerialDescriptor.getSetterOrNull(index: Int) = getElementAnnotations(index).filterIsInstance<Setter>().firstOrNull()?.value

@ExperimentalSerializationApi
fun SerialDescriptor.getIndexedOrNull(index: Int) = getElementAnnotations(index).filterIsInstance<Indexed>().firstOrNull()