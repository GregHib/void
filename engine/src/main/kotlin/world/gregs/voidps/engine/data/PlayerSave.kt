package world.gregs.voidps.engine.data

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.clan.ClanRank
import world.gregs.voidps.engine.entity.character.player.equip.BodyParts
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.Experience
import world.gregs.voidps.engine.entity.character.player.skill.level.Levels
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.Inventories
import world.gregs.voidps.type.Tile

data class PlayerSave(
    val name: String,
    val password: String,
    val tile: Tile,
    val experience: DoubleArray,
    val blocked: List<Skill>,
    val levels: IntArray,
    val male: Boolean,
    val looks: IntArray,
    val colours: IntArray,
    val variables: Map<String, Any>,
    val inventories: Map<String, Array<Item>>,
    val friends: Map<String, ClanRank>,
    val ignores: List<String>
) {

    fun toPlayer(): Player {
        return Player(
            accountName = name,
            passwordHash = password,
            tile = tile,
            experience = Experience(experience, blocked.toMutableSet()),
            levels = Levels(levels),
            body = BodyParts(male, looks, colours),
            variables = variables.toMutableMap(),
            inventories = Inventories(inventories),
            friends = friends.toMutableMap(),
            ignores = ignores.toMutableList(),
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlayerSave

        if (name != other.name) return false
        if (password != other.password) return false
        if (tile != other.tile) return false
        if (!experience.contentEquals(other.experience)) return false
        if (blocked != other.blocked) return false
        if (!levels.contentEquals(other.levels)) return false
        if (male != other.male) return false
        if (!looks.contentEquals(other.looks)) return false
        if (!colours.contentEquals(other.colours)) return false
        if (variables != other.variables) return false
        if (inventories != other.inventories) return false
        if (friends != other.friends) return false
        if (ignores != other.ignores) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + password.hashCode()
        result = 31 * result + tile.hashCode()
        result = 31 * result + experience.contentHashCode()
        result = 31 * result + blocked.hashCode()
        result = 31 * result + levels.contentHashCode()
        result = 31 * result + male.hashCode()
        result = 31 * result + looks.contentHashCode()
        result = 31 * result + colours.contentHashCode()
        result = 31 * result + variables.hashCode()
        result = 31 * result + inventories.hashCode()
        result = 31 * result + friends.hashCode()
        result = 31 * result + ignores.hashCode()
        return result
    }
}

internal fun Player.copy() = PlayerSave(
    name = accountName,
    password = passwordHash,
    tile = tile,
    experience = experience.experience.copyOf(),
    blocked = experience.blocked.toList(),
    levels = levels.levels.copyOf(),
    male = body.male,
    looks = body.looks.copyOf(),
    colours = body.colours.copyOf(),
    variables = variables.data.toMap(),
    inventories = inventories.instances.mapValues { it.value.items.map { itm -> itm.copy() }.toTypedArray() },
    friends = friends,
    ignores = ignores.toList()
)