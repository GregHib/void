package world.gregs.voidps.world.activity.skill.mining

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.contain.hasItem
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.toTitleCase
import world.gregs.voidps.engine.utility.toUnderscoreCase
import world.gregs.voidps.world.interact.entity.player.equip.requiredEquipLevel

enum class Pickaxe(val delay: Int) {
    BronzePickaxe(8),
    IronPickaxe(7),
    SteelPickaxe(6),
    MithrilPickaxe(5),
    AdamantPickaxe(4),
    RunePickaxe(3),
    InfernoAdze(2),
    SacredClayPickaxe(3),
    VolatileClayPickaxe(3),
    DragonPickaxe(3),

    // Stealing creation
    PickaxeClass1(6),
    PickaxeClass2(5),
    PickaxeClass3(4),
    PickaxeClass4(3),
    PickaxeClass5(2),

    // Dungeoneering
    NovitePickaxe(7),
    BathusPickaxe(7),
    MarmarosPickaxe(6),
    KratonitePickaxe(6),
    FractitePickaxe(5),
    ZephyriumPickaxe(5),
    ArgonitePickaxe(4),
    KatagonPickaxe(4),
    GorgonitePickaxe(3),
    PromethiumPickaxe(3),
    PrimalPickaxe(2);

    val id: String = name.toTitleCase().toUnderscoreCase()

    val requiredLevel: Int
        get() = when (this) {
            InfernoAdze -> 41
            SacredClayPickaxe, VolatileClayPickaxe -> 50
            PickaxeClass2 -> 20
            PickaxeClass3 -> 40
            PickaxeClass4 -> 60
            PickaxeClass5 -> 80
            else -> get<ItemDefinitions>().get(id).requiredEquipLevel()
        }

    companion object {

        fun hasRequirements(player: Player, pickaxe: Pickaxe?, message: Boolean = false): Boolean {
            if (pickaxe == null) {
                if (message) {
                    player.message("You need a pickaxe to mine this rock.")
                    player.message("You do not have a pickaxe which you have the mining level to use.")
                }
                return false
            }
            if (pickaxe == InfernoAdze && !player.has(Skill.Firemaking, 92, message)) {
                return false
            }
            if (!player.has(Skill.Mining, pickaxe.requiredLevel, message)) {
                return false
            }
            return true
        }

        fun get(player: Player): Pickaxe? {
            val list = values().filter { pickaxe -> hasRequirements(player, pickaxe, false) && player.hasItem(pickaxe.id) }
            return list.maxByOrNull { it.ordinal }
        }

        fun get(id: String): Pickaxe? {
            if (id.isBlank()) {
                return null
            }
            val id = id.toUnderscoreCase()
            for (pickaxe in values()) {
                if (id == pickaxe.id) {
                    return pickaxe
                }
            }
            return null
        }

    }

}