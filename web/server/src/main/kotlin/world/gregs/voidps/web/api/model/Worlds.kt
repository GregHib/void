package world.gregs.voidps.web.api.model

import kotlinx.serialization.Serializable

@Serializable
data class ServerInfo(
    val status: String,
    val members: Boolean,
    val players: Int,
    val capacity: Int,
    val xpRate: Double,
    val dropRate: Double,
    val uptimeSeconds: Long,
)

@Serializable
data class PlayerLocation(
    val name: String,
    val x: Int,
    val y: Int,
    val level: Int,
)
