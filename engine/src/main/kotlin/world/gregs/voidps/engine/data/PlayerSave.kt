package world.gregs.voidps.engine.data

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.config.*
import world.gregs.voidps.engine.data.exchange.ExchangeHistory
import world.gregs.voidps.engine.data.exchange.ExchangeOffer
import world.gregs.voidps.engine.data.exchange.OfferState
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.clan.ClanRank
import world.gregs.voidps.engine.entity.character.player.equip.BodyParts
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.Experience
import world.gregs.voidps.engine.entity.character.player.skill.level.Levels
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.Inventories
import world.gregs.voidps.type.Tile
import java.io.File

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
    val ignores: List<String>,
    val offers: Array<ExchangeOffer>,
    val history: List<ExchangeHistory>,
) {

    fun toPlayer(): Player = Player(
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
        offers = offers,
        history = history.toMutableList(),
    )

    fun save(file: File) {
        Config.fileWriter(file) {
            writePair("accountName", name)
            writePair("passwordHash", password)
            writePair("experience", experience)
            writePair("blocked_skills", blocked)
            writePair("levels", levels)
            writePair("male", male)
            writePair("looks", looks)
            writePair("colours", colours)

            write("\n")

            writeSection("tile")
            writePair("x", tile.x)
            writePair("y", tile.y)
            if (tile.level > 0) {
                writePair("level", tile.level)
            }

            write("\n")
            writeSection("variables")
            for ((key, value) in variables) {
                writePair(key, value)
            }

            write("\n")
            writeSection("inventories")
            for ((key, value) in inventories) {
                writeKey(key)
                list(value.size) {
                    val item = value[it]
                    if (item.isEmpty()) {
                        write("{}")
                    } else {
                        write("{")
                        writeKey("id")
                        write("\"")
                        write(item.id)
                        write("\"")
                        if (item.amount > 1) {
                            write(",")
                            writeKey("amount")
                            write(item.amount.toString())
                        }
                        write("}")
                    }
                }
                write("\n")
            }

            write("\n")
            writeSection("social")
            writeKey("friends")
            writeValue(friends, escapeKey = true)
            write("\n")
            writePair("ignores", ignores)
            write("\n")
            writeSection("exchange")
            writeKey("offers")
            list(offers.size) { index ->
                val offer = offers[index]
                if (offer.isEmpty()) {
                    write("{}")
                } else {
                    write("{")
                    writeKey("id")
                    writeValue(offer.id)
                    write(",")
                    writeKey("item")
                    writeValue(offer.item)
                    write(",")
                    writeKey("amount")
                    writeValue(offer.amount)
                    write(",")
                    writeKey("price")
                    writeValue(offer.price)
                    write(",")
                    writeKey("state")
                    writeValue(offer.state.name)
                    if (offer.completed > 0) {
                        write(",")
                        writeKey("completed")
                        writeValue(offer.completed)
                    }
                    if (offer.coins > 0) {
                        write(",")
                        writeKey("coins")
                        writeValue(offer.coins)
                    }
                    write("}")
                }
            }
            write("\n")
            writeKey("history")
            list(history.size) { index ->
                val history = history[index]
                write("{")
                writeKey("item")
                writeValue(history.item)
                write(",")
                writeKey("price")
                writeValue(history.price)
                write(",")
                writeKey("amount")
                writeValue(history.amount)
                write("}")
            }
            write("\n")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlayerSave

        if (male != other.male) return false
        if (name != other.name) return false
        if (password != other.password) return false
        if (tile != other.tile) return false
        if (!experience.contentEquals(other.experience)) return false
        if (blocked != other.blocked) return false
        if (!levels.contentEquals(other.levels)) return false
        if (!looks.contentEquals(other.looks)) return false
        if (!colours.contentEquals(other.colours)) return false
        if (variables != other.variables) return false
        if (inventories != other.inventories) return false
        if (friends != other.friends) return false
        if (ignores != other.ignores) return false
        if (!offers.contentEquals(other.offers)) return false
        if (history != other.history) return false

        return true
    }

    override fun hashCode(): Int {
        var result = male.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + password.hashCode()
        result = 31 * result + tile.hashCode()
        result = 31 * result + experience.contentHashCode()
        result = 31 * result + blocked.hashCode()
        result = 31 * result + levels.contentHashCode()
        result = 31 * result + looks.contentHashCode()
        result = 31 * result + colours.contentHashCode()
        result = 31 * result + variables.hashCode()
        result = 31 * result + inventories.hashCode()
        result = 31 * result + friends.hashCode()
        result = 31 * result + ignores.hashCode()
        result = 31 * result + offers.contentHashCode()
        result = 31 * result + history.hashCode()
        return result
    }

    companion object {
        fun load(file: File): PlayerSave {
            var name = ""
            var password = ""
            var tile = Tile.EMPTY
            val experience = DoubleArray(25)
            val blocked = ObjectArrayList<Skill>(0)
            val levels = IntArray(25)
            var male = true
            val looks = IntArray(7)
            val colours = IntArray(5)
            val variables = Object2ObjectOpenHashMap<String, Any>(64)
            val inventories = Object2ObjectOpenHashMap<String, Array<Item>>(4)
            val friends = Object2ObjectOpenHashMap<String, ClanRank>()
            val ignores = ObjectArrayList<String>()
            val offers = Array(6) { ExchangeOffer.EMPTY }
            val history = ObjectArrayList<ExchangeHistory>()
            Config.fileReader(file) {
                while (nextPair()) {
                    when (val key = key()) {
                        "accountName" -> name = string()
                        "passwordHash" -> password = string()
                        "experience" -> {
                            var index = 0
                            while (nextElement()) {
                                experience[index++] = double()
                            }
                        }
                        "blocked_skills" -> while (nextElement()) {
                            val skill = Skill.of(string())
                            blocked.add(skill)
                        }
                        "levels" -> {
                            var index = 0
                            while (nextElement()) {
                                levels[index++] = int()
                            }
                        }
                        "male" -> male = boolean()
                        "looks" -> {
                            var index = 0
                            while (nextElement()) {
                                looks[index++] = int()
                            }
                        }
                        "colours" -> {
                            var index = 0
                            while (nextElement()) {
                                colours[index++] = int()
                            }
                        }
                        else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                    }
                }
                while (nextSection()) {
                    when (val section = section()) {
                        "tile" -> {
                            var x = 0
                            var y = 0
                            var level = 0
                            while (nextPair()) {
                                when (val k = key()) {
                                    "x" -> x = int()
                                    "y" -> y = int()
                                    "level" -> level = int()
                                    else -> throw IllegalArgumentException("Unexpected account tile key: '$k' ${exception()}")
                                }
                            }
                            tile = Tile(x, y, level)
                        }
                        "variables" -> {
                            while (nextPair()) {
                                variables[key()] = value()
                            }
                        }
                        "inventories" -> {
                            while (nextPair()) {
                                val inv = key()
                                val items = ObjectArrayList<Item>()
                                while (nextElement()) {
                                    var id = ""
                                    var amount = 0
                                    while (nextEntry()) {
                                        when (val itemKey = key()) {
                                            "id" -> {
                                                id = string()
                                                amount = 1
                                            }
                                            "amount" -> amount = int()
                                            else -> throw IllegalArgumentException("Unexpected account inv key: '$itemKey' ${exception()}")
                                        }
                                    }
                                    items.add(Item(id, amount))
                                }
                                inventories[inv] = items.toTypedArray()
                            }
                        }
                        "social" -> {
                            while (nextPair()) {
                                when (val socialKey = key()) {
                                    "friends" -> while (nextEntry()) {
                                        val friend = key()
                                        val rank = string()
                                        friends[friend] = ClanRank.by(rank)
                                    }
                                    "ignores" -> while (nextElement()) {
                                        ignores.add(string())
                                    }
                                    else -> throw IllegalArgumentException("Unexpected account social key: '$socialKey' ${exception()}")
                                }
                            }
                        }
                        "exchange" -> {
                            while (nextPair()) {
                                when (val exchangeKey = key()) {
                                    "offers" -> {
                                        var index = 0
                                        while (nextElement()) {
                                            while (nextEntry()) {
                                                var id = 0
                                                var item = ""
                                                var amount = 0
                                                var price = 0
                                                var state: OfferState = OfferState.PendingBuy
                                                var completed = 0
                                                var coins = 0
                                                when (val key = key()) {
                                                    "id" -> id = int()
                                                    "item" -> item = string()
                                                    "amount" -> amount = int()
                                                    "price" -> price = int()
                                                    "state" -> state = OfferState.valueOf(string())
                                                    "completed" -> completed = int()
                                                    "coins" -> coins = int()
                                                    else -> throw IllegalArgumentException("Unexpected exchange offer key: '$key' ${exception()}")
                                                }
                                                offers[index++] = ExchangeOffer(id = id, item = item, amount = amount, price = price, state = state, completed = completed, coins = coins)
                                            }
                                        }
                                    }
                                    "history" -> while (nextElement()) {
                                        while (nextPair()) {
                                            var item = ""
                                            var price = 0
                                            var amount = 0
                                            when (val key = key()) {
                                                "item" -> item = string()
                                                "price" -> price = int()
                                                "amount" -> amount = int()
                                                else -> throw IllegalArgumentException("Unexpected exchange history key: '$key' ${exception()}")
                                            }
                                            history.add(ExchangeHistory(item, price, amount))
                                        }
                                    }
                                    else -> throw IllegalArgumentException("Unexpected key: '$exchangeKey' ${exception()}")
                                }
                            }
                        }
                        else -> throw IllegalArgumentException("Unexpected section: '$section' ${exception()}")
                    }
                }
            }
            return PlayerSave(
                name = name,
                password = password,
                tile = tile,
                experience = experience,
                blocked = blocked,
                levels = levels,
                male = male,
                looks = looks,
                colours = colours,
                variables = variables,
                inventories = inventories,
                friends = friends,
                ignores = ignores,
                offers = offers,
                history = history,
            )
        }
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
    ignores = ignores.toList(),
    offers = offers.copyOf(),
    history = history.toList(),
)
