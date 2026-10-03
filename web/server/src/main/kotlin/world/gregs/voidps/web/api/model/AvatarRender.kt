package world.gregs.voidps.web.api.model

import kotlinx.serialization.Serializable

@Serializable
data class AvatarRender(
    val name: String,
    /** Whether a full body image was written. */
    val full: Boolean,
    /** Whether a chathead image was written. */
    val chat: Boolean,
    val snapshotAt: String,
    val renderedAt: String,
)
