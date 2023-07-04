package world.gregs.voidps.engine.data

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.map.Tile

internal class PlayerSave(
    val name: String,
    val password: String,
    val tile: Tile,
    val experience: DoubleArray,
    val blocked: List<String>,
    val levels: IntArray,
    val male: Boolean,
    val looks: IntArray,
    val colours: IntArray,
    val variables: Map<String, Any>,
    val containers: Map<String, Array<Item>>,
    val friends: Map<String, String>,
    val ignores: List<String>
)

internal fun Player.copy() = PlayerSave(
    name = accountName,
    password = passwordHash,
    tile = tile,
    experience = experience.experience.copyOf(),
    blocked = experience.blocked.map { it.name },
    levels = levels.levels.copyOf(),
    male = body.male,
    looks = body.looks.copyOf(),
    colours = body.colours.copyOf(),
    variables = variables.data.toMap(),
    containers = containers.containers.mapValues { it.value.copyOf() },
    friends = friends.mapValues { it.value.name },
    ignores = ignores.toList()
)