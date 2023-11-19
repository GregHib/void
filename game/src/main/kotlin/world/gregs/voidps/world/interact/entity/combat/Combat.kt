package world.gregs.voidps.world.interact.entity.combat

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player

var Character.attackers: MutableList<Character>
    get() = getOrPut("attackers") { ObjectArrayList() }
    set(value) = set("attackers", value)

var Character.damageDealers: MutableMap<Character, Int>
    get() = getOrPut("damage_dealers") { Object2IntOpenHashMap() }
    set(value) = set("damage_dealers", value)

// E.g "accurate"
val Character.attackStyle: String
    get() = get("attack_style", "")

// E.g "flick"
val Character.attackType: String
    get() = get("attack_type", "")

// E.g "crush"
val Character.combatStyle: String
    get() = get("combat_style", "")

var Character.spell: String
    get() = get("spell", get("autocast_spell", ""))
    set(value) = set("spell", value)

var Character.dead: Boolean
    get() = get("dead", false)
    set(value) {
        if (value) {
            set("dead", true)
        } else {
            clear("dead")
        }
    }

var Character.attackRange: Int
    get() = get("attack_range", if (this is NPC) def["attack_range", 1] else 1)
    set(value) = set("attack_range", value)

val Player.spellBook: String
    get() = interfaces.get("spellbook_tab") ?: "unknown_spellbook"