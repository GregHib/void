package world.gregs.voidps.engine.data.serializer

import world.gregs.voidps.engine.client.variable.Variables
import world.gregs.voidps.engine.entity.Values
import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Experience
import world.gregs.voidps.engine.entity.character.player.skill.Levels
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.map.Tile

internal data class PlayerBuilder(
    var tile: Int,
    val containers: MutableMap<String, Container>,
    val experience: Experience,
    val variables: MutableMap<String, Any>,
    val levelOffsets: MutableMap<Skill, Int>,
    val name: String,
    val passwordHash: String,
    val values: MutableMap<String, Any>,
) {

    fun build() = Player(
        tile = Tile(tile),
        containers = containers,
        experience = experience,
        variables = Variables(variables),
        levels = Levels(levelOffsets),
        name = name,
        values = Values(values),
        passwordHash = passwordHash
    )
}