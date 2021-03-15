package world.gregs.voidps.engine.data.serializer

import world.gregs.voidps.engine.entity.character.CharacterEffects
import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Experience
import world.gregs.voidps.engine.entity.character.player.skill.Levels
import world.gregs.voidps.engine.map.Tile

internal data class PlayerBuilder(
    var tile: Int,
    val containers: MutableMap<Int, Container>,
    val experience: Experience,
    val levels: Levels,
    val effects: CharacterEffects,
    val name: String,
    val passwordHash: String,
) {
    data class WorldTile(val x: Int, val y: Int, val plane: Int = 0)

    fun build() = Player(
        tile = Tile(tile),
        containers = containers,
        experience = experience,
        levels = levels,
        effects = effects,
        name = name,
        passwordHash = passwordHash
    )
}