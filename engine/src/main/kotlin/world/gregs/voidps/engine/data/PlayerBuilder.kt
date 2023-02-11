package world.gregs.voidps.engine.data

import com.fasterxml.jackson.databind.JsonNode
import world.gregs.voidps.engine.client.variable.Variables
import world.gregs.voidps.engine.contain.ContainerData
import world.gregs.voidps.engine.contain.Containers
import world.gregs.voidps.engine.entity.Values
import world.gregs.voidps.engine.entity.character.Levels
import world.gregs.voidps.engine.entity.character.player.BodyParts
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.Rank
import world.gregs.voidps.engine.entity.character.player.skill.Experience
import world.gregs.voidps.engine.map.Tile

internal class PlayerBuilder(
    var tile: JsonNode,
    val containers: MutableMap<String, ContainerData>,
    val experience: Experience,
    val variables: MutableMap<String, Any>,
    val levels: IntArray,
    val accountName: String,
    val passwordHash: String,
    val values: MutableMap<String, Any>,
    val friends: MutableMap<String, Rank>,
    val ignores: MutableList<String>,
    val male: Boolean,
    val looks: IntArray,
    val colours: IntArray
) {

    fun build() = Player(
        tile = Tile(tile["x"].asInt(), tile["y"].asInt(), tile["plane"]?.asInt() ?: 0),
        containers = Containers(containers),
        experience = experience,
        variables = Variables(variables),
        levels = Levels(levels),
        accountName = accountName,
        values = Values(values),
        friends = friends,
        ignores = ignores,
        passwordHash = passwordHash,
        body = BodyParts(male, looks, colours)
    )
}