package world.gregs.voidps.engine.data

import com.fasterxml.jackson.databind.JsonNode
import world.gregs.voidps.engine.contain.ContainerData
import world.gregs.voidps.engine.contain.Containers
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.clan.ClanRank
import world.gregs.voidps.engine.entity.character.player.equip.BodyParts
import world.gregs.voidps.engine.entity.character.player.skill.exp.Experience
import world.gregs.voidps.engine.entity.character.player.skill.level.Levels
import world.gregs.voidps.engine.map.Tile

internal class PlayerBuilder(
    var tile: JsonNode,
    val containers: MutableMap<String, ContainerData>,
    val experience: Experience,
    val variables: MutableMap<String, Any>,
    val levels: IntArray,
    val accountName: String,
    val passwordHash: String,
    val friends: MutableMap<String, ClanRank>,
    val ignores: MutableList<String>,
    val male: Boolean,
    val looks: IntArray,
    val colours: IntArray
) {

    fun build() = Player(
        tile = Tile(tile["x"].asInt(), tile["y"].asInt(), tile["plane"]?.asInt() ?: 0),
        containers = Containers(containers),
        experience = experience,
        variables = variables,
        levels = Levels(levels),
        accountName = accountName,
        friends = friends,
        ignores = ignores,
        passwordHash = passwordHash,
        body = BodyParts(male, looks, colours)
    )
}