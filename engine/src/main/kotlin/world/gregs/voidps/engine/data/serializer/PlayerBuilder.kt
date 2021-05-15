package world.gregs.voidps.engine.data.serializer

import world.gregs.voidps.engine.entity.character.CharacterEffects
import world.gregs.voidps.engine.entity.character.CharacterValues
import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Experience
import world.gregs.voidps.engine.entity.character.player.skill.Levels
import world.gregs.voidps.engine.map.Tile

internal data class PlayerBuilder(
    var tile: Int,
    val containers: MutableMap<String, Container>,
    val experience: Experience,
    val variables: MutableMap<String, Any>,
    val levels: Levels,
    val effects: CharacterEffects,
    val name: String,
    val passwordHash: String,
    val values: MutableMap<String, Any>,
) {

    fun build() = Player(
        tile = Tile(tile),
        containers = containers,
        experience = experience,
        variables = variables,
        levels = levels,
        effects = effects,
        name = name,
        values = CharacterValues(values),
        passwordHash = passwordHash
    )
}