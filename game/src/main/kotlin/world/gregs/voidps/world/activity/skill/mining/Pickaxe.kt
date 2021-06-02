package world.gregs.voidps.world.activity.skill.mining

import org.koin.core.context.startKoin
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.engine.client.cacheDefinitionModule
import world.gregs.voidps.engine.client.cacheModule
import world.gregs.voidps.engine.data.file.FileLoader
import world.gregs.voidps.engine.entity.character.contain.has
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.utility.get
import world.gregs.voidps.world.interact.entity.player.equip.requiredLevel

enum class Pickaxe(val delay: Int) {
    Bronze_Pickaxe(8),
    Iron_Pickaxe(7),
    Steel_Pickaxe(6),
    Mithril_Pickaxe(5),
    Adamant_Pickaxe(4),
    Rune_Pickaxe(3),
    Inferno_Adze(2),
    Sacred_Clay_Pickaxe(3),
    Volatile_Clay_Pickaxe(3),
    Dragon_Pickaxe(2),

    // Stealing creation
    Pickaxe_Class_1(6),
    Pickaxe_Class_2(5),
    Pickaxe_Class_3(4),
    Pickaxe_Class_4(3),
    Pickaxe_Class_5(2),

    // Dungeoneering
    Novite_Pickaxe(7),
    Bathus_Pickaxe(7),
    Marmaros_Pickaxe(6),
    Kratonite_Pickaxe(6),
    Fractite_Pickaxe(5),
    Zephyrium_Pickaxe(5),
    Argonite_Pickaxe(4),
    Katagon_Pickaxe(4),
    Gorgonite_Pickaxe(3),
    Promethium_Pickaxe(3),
    Primal_Pickaxe(2);

    val id: String = name.toLowerCase()

    val requiredLevel: Int
        get() = when (this) {
            Inferno_Adze -> 41
            Sacred_Clay_Pickaxe, Volatile_Clay_Pickaxe -> 50
            Pickaxe_Class_2 -> 20
            Pickaxe_Class_3 -> 40
            Pickaxe_Class_4 -> 60
            Pickaxe_Class_5 -> 80
            else -> get<ItemDefinitions>().get(id).requiredLevel()
        }

    companion object {

        fun hasRequirements(player: Player, pickaxe: Item?, message: Boolean = false): Boolean {
            return hasRequirements(player, get(pickaxe?.name ?: return false) ?: return false, message)
        }

        fun hasRequirements(player: Player, pickaxe: Pickaxe?, message: Boolean = false): Boolean {
            if (pickaxe == null) {
                if (message) {
                    player.message("You need a pickaxe to mine this rock.")
                    player.message("You do not have a pickaxe which you have the mining level to use.")
                }
                return false
            }
            if (pickaxe == Inferno_Adze && !player.has(Skill.Firemaking, 92, message)) {
                return false
            }
            if (!player.has(Skill.Mining, pickaxe.requiredLevel, message)) {
                return false
            }
            return true
        }

        fun get(player: Player): Pickaxe? {
            val list = values().filter { pickaxe -> hasRequirements(player, pickaxe, false) && player.has(pickaxe.id) }
            return list.maxByOrNull { it.ordinal }
        }

        fun get(name: String): Pickaxe? {
            val name = name.replace(" ", "_").toLowerCase()
            for (pickaxe in values()) {
                if (name == pickaxe.id) {
                    return pickaxe
                }
            }
            return null
        }

        @JvmStatic
        fun main(args: Array<String>) {
            val koin = startKoin {
                fileProperties("/game.properties")
                modules(cacheModule, cacheDefinitionModule)
            }.koin
            val decoder = ItemDefinitions(ItemDecoder(koin.get())).load(FileLoader())
            for (pick in values()) {
                val def = decoder.getOrNull(pick.id)
                println("${pick.id} ${def?.requiredLevel()}")
            }
        }

    }

}