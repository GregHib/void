package org.redrune.world.list

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 22, 2020
 */
data class WorldListEntry(
    /**
     * The activity taking place in the world
     */
    val activity: String,

    /**
     * The ip of the world
     */
    val ip: String,

    /**
     * The id of the world
     */
    val countryId: Int,

    /**
     * The flag representation of the world
     */
    val flag: Int,
    val countryName: String,
    val members: Boolean
) {
}