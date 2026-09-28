package content.entity.obj

import world.gregs.voidps.cache.definition.data.ObjectDefinition

/*
 * Objects which share a model with another only have one id so their counterparts can't always be
 * found by name, those that can't have their counterpart set explicitly in their config instead.
 */

/**
 * Id of the object this is replaced with when opened
 */
val ObjectDefinition.opened: String
    get() = getOrNull("opened") ?: stringId.replace("_closed", "_opened")

/**
 * Id of the object this is replaced with when closed
 */
val ObjectDefinition.closed: String
    get() = getOrNull("closed") ?: stringId.replace("_opened", "_closed")
