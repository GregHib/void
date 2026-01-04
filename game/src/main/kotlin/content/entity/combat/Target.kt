package content.entity.combat

import content.area.wilderness.Wilderness
import content.area.wilderness.inPvp
import content.area.wilderness.inSingleCombat
import content.area.wilderness.inWilderness
import content.entity.effect.transform
import content.entity.player.equip.Equipment
import content.skill.melee.weapon.fightStyle
import content.skill.slayer.categories
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.combatLevel
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.get
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

object Target {
    fun attackable(source: Character, target: Character): Boolean {
        if (target is NPC) {
            if (target.id.startsWith("door_support") && get<NPCDefinitions>().get(target.id).options[1] == "Destroy") {
                return true
            }
            if (target.transform != "") {
                if (get<NPCDefinitions>().get(target.transform).options[1] != "Attack") {
                    return false
                }
            } else if (target.def.options[1] != "Attack") {
                return false
            }
            if (target.index == -1) {
                return false
            }
            if (get<NPCs>().indexed(target.index) == null) {
                return false
            }
            if (source.fightStyle == "melee" && target.categories.contains("aviansie")) {
                source.message("The Aviansie is flying too high for you to attack using melee.")
                return false
            }
        }
        if (source.tile.level != target.tile.level) {
            return false
        }
        if (source.dead || target.dead || source["logged_out", false] || target["logged_out", false]) {
            return false
        }
        if (source is Player && target is Player) {
            if (!source.inPvp && !source.inWilderness) {
                source.message("You can only attack players in a player-vs-player area.")
                return false
            }
            if (!target.inPvp && !target.inWilderness) {
                source.message("That player is not in the wilderness.")
                return false
            }
            if (target.inWilderness) {
                val range = Wilderness.combatRange(source)
                if (target.combatLevel !in range) {
                    source.message("Your level difference is too great!")
                    source.message("You need to move deeper into the Wilderness.")
                    return false
                }
            }
        }
        if (source is NPC && source.id == "death_spawn") {
            return true
        }
        // If the target I'm trying to attack is already in combat and I am not the attacker
        if (target.inSingleCombat && target.inCombat && target.attacker != source) {
            if (target is NPC) {
                (source as? Player)?.message("Someone else is fighting that.")
            } else {
                (source as? Player)?.message("That player is already under attack.")
            }
            return false
        }
        // If I am already in combat and my attempted target is not my attacker
        if (source.inSingleCombat && source.inCombat && source.attacker != target) {
            (source as? Player)?.message("You are already in combat.")
            return false
        }
        // PVP area, slayer requirements, in combat etc..
        return true
    }

    fun isDemon(target: Character) = target is NPC && target.categories.contains("demons")

    fun isVampyre(target: Character) = target is NPC && target.categories.contains("vampyres")

    fun isShade(target: Character): Boolean = target is NPC && target.categories.contains("shades")

    fun isKalphite(target: Character): Boolean = target is NPC && target.categories.contains("kalphites")

    fun isDragon(target: Character): Boolean = target is NPC && target.categories.contains("dragons")

    fun isMetalDragon(target: Character): Boolean = target is NPC && (target.id == "bronze_dragon" || target.id == "iron_dragon" || target.id == "steel_dragon")

    // TODO other staves and npcs
    fun isFirey(target: Character): Boolean {
        if (target is Player) {
            return target.equipped(EquipSlot.Weapon).id == "staff_of_fire"
        } else if (target is NPC) {
            return target.categories.contains("dragons") || target.id.startsWith("fire_elemental") || target.id.startsWith("fire_giant") || target.id.startsWith("pyrefiend")
        }
        return false
    }

    /**
     * Damage caps which Guthans doesn't work on
     * E.g. Kurask & Turoth
     */
    fun damageModifiers(source: Character, target: Character, damage: Int): Int {
        if (source is NPC && target is Player) {
            val hat = target.equipped(EquipSlot.Hat).id
            if (source.id == "banshee" && !Equipment.isEarmuffs(hat)) {
                return 80
            }
            if (source.id == "aberrant_spectre" && !Equipment.isNosePeg(hat)) {
                return 160
            }
        }
        return damage
    }

    /**
     * Limits maximum amount of damage on NPCs (while still allowing Guthans to work)
     */
    fun damageLimitModifiers(target: Character, damage: Int): Int = when (target) {
        is NPC if target.def.contains("damage_cap") -> damage.coerceAtMost(target.def["damage_cap"])
        is NPC if target.def.contains("immune_death") -> damage.coerceAtMost(target.levels.get(Skill.Constitution) - 10)
        else -> damage
    }
}

internal var Character.target: Character?
    get() = get("target")
    set(value) {
        if (value != null) {
            set("target", value)
        } else {
            clear("target")
        }
    }

val Character.inCombat: Boolean
    get() = hasClock("in_combat")

var Character.attacker: Character?
    get() = get("attacker")
    set(value) {
        if (value == null) {
            clear("attacker")
        } else {
            set("attacker", value)
        }
    }

var Character.attackers: MutableList<Character>
    get() = getOrPut("attackers") { ObjectArrayList() }
    set(value) = set("attackers", value)

var Character.damageDealers: MutableMap<Character, Int>
    get() = getOrPut("damage_dealers") { Object2IntOpenHashMap() }
    set(value) = set("damage_dealers", value)

val Character.killer: Character?
    get() = damageDealers.maxByOrNull { it.value }?.key

var Character.dead: Boolean
    get() = get("dead", false)
    set(value) {
        if (value) {
            set("dead", true)
        } else {
            clear("dead")
        }
    }
