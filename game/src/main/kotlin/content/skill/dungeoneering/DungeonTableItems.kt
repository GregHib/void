package content.skill.dungeoneering

import content.skill.dungeoneering.Dungeoneering.Companion.rotateX
import content.skill.dungeoneering.Dungeoneering.Companion.rotateY
import content.skill.magic.spell.SpellRunes
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Interpolation
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.entity.item.drop.ItemDrop
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.get
import world.gregs.voidps.type.Delta
import world.gregs.voidps.type.random
import kotlin.collections.iterator
import kotlin.math.min

object DungeonTableItems {

    private val fish = arrayOf("heim_crab", "red_eye", "dusk_eel", "giant_flatfish", "short_finned_eel", "web_snipper", "bouldabass", "salve_eel", "blue_crab", "cave_moray")

    fun spawn(complexity: Int, dungeon: DungeonMap, skills: Map<Skill, Int>, partySize: Int) {
        val items = mutableListOf<ItemDrop>()
        val startRoom = dungeon.start()
        val tile = dungeon.tile(startRoom)
        val limit = (skills.getValue(Skill.Constitution) / 100).coerceAtMost(if (World.members) 10 else 5)
        repeat(7 + (partySize * 2)) {
            val index = random.nextInt(0, limit)
            val fish = fish[index]
            items.add(ItemDrop(fish))
        }
        if (complexity > 1) {
            addCombatGear(items, skills)
            // TODO knife, etc.. for higher complexities?
            // TODO dragonfire shield if dragon spawn
            items.add(ItemDrop("rusty_coins", 490..510))
        }
        if (dungeon.traverse { _, door, _ -> door is DungeonDoor.Blocked && door.skill == Skill.Mining }.isNotEmpty()) { // TODO mining puzzles + boss?
            val tier = Tables.stringList("dungeoneering_tiers.melee.names")[Interpolation.lerp(skills.getValue(Skill.Mining), 1..99, 0..2)]
            items.add(ItemDrop("${tier}_pickaxe"))
        }
        if (dungeon.traverse { _, door, _ -> door is DungeonDoor.Blocked && door.skill == Skill.Woodcutting }.isNotEmpty()) {
            val tier = Tables.stringList("dungeoneering_tiers.melee.names")[Interpolation.lerp(skills.getValue(Skill.Woodcutting), 1..99, 0..2)]
            items.add(ItemDrop("${tier}_hatchet"))
        }
        val tiles = listOf(
            Delta(x = 10, y = 14),
            Delta(x = 11, y = 14),
            Delta(x = 14, y = 11),
            Delta(x = 14, y = 10),
            Delta(x = 9, y = 11),
            Delta(x = 10, y = 11),
            Delta(x = 11, y = 11),
            Delta(x = 9, y = 10),
            Delta(x = 10, y = 10),
            Delta(x = 11, y = 10),
        )
        println("Table items:")
        for (drop in items) {
            val delta = tiles.random(random)
            val tx = rotateX(delta.x, delta.y, startRoom.rotation, 15)
            val ty = rotateY(delta.x, delta.y, startRoom.rotation, 15)
            val item = drop.toItem()
            println("  $item")
            FloorItems.add(tile.add(tx, ty), item.id, item.amount)
        }
    }

    private fun addCombatGear(items: MutableList<ItemDrop>, skills: Map<Skill, Int>) {
        var style = "melee"
        repeat(random.nextInt(2)) {
            style = addWeapon(items, skills)
        }
        repeat(random.nextInt(2)) {
            addArmour(items, skills, style)
        }
    }

    private fun addArmour(items: MutableList<ItemDrop>, skills: Map<Skill, Int>, style: String) {
        val dropTables = get<DropTables>()
        for (armour in dropTables.getValue("dungeoneering_${style}_armour").roll()) {
            var id = armour.id
            when (style) {
                "melee" -> id = getTier(id, skills.getValue(Skill.Defence), "novite", style)
                "ranged" -> id = getTier(id, min(skills.getValue(Skill.Defence), skills.getValue(Skill.Ranged)), "protoleather", style)
                "magic" -> id = getTier(id, min(skills.getValue(Skill.Defence), skills.getValue(Skill.Magic)), "salve_", style)
            }
            items.add(ItemDrop(id, armour.amount))
        }
    }

    private fun addWeapon(items: MutableList<ItemDrop>, skills: Map<Skill, Int>): String {
        var style = "melee"
        val dropTables = get<DropTables>()
        for (weapon in dropTables.getValue("dungeoneering_weapons").roll()) {
            var id = weapon.id
            when (weapon.id) {
                "tangle_gum_shortbow" -> {
                    style = "ranged"
                    id = getTier(id, skills.getValue(Skill.Ranged), "tangle_gum", "woodcutting")
                }
                "novite_arrows" -> id = getTier(id, skills.getValue(Skill.Ranged), "novite", "melee")
                "tangle_gum_staff" -> {
                    style = "magic"
                    id = getTier(id, skills.getValue(Skill.Magic), "tangle_gum", "woodcutting")
                }
                "rune_essence_dungeoneering" -> addSpell(items, skills)
                else -> id = getTier(id, skills.getValue(Skill.Defence), "novite", "melee")
            }
            items.add(ItemDrop(id, weapon.amount))
        }
        return style
    }

    private fun getTier(id: String, level: Int, key: String, type: String): String {
        var updated = id
        val tier = Interpolation.lerp(level, 1..99, 0..maxTier())
        val tierName = Tables.stringList("dungeoneering_tiers.$type.names")[tier]
        updated = updated.replace(key, tierName)
        return updated
    }

    private fun maxTier(): Int = if (World.members) 6 else 4

    private fun addSpell(items: MutableList<ItemDrop>, skills: Map<Skill, Int>) {
        val spells = mutableSetOf<String>()
        val magicLevel = skills.getValue(Skill.Magic).coerceAtMost(if (World.members) 99 else 49)
        val book = "modern_spellbook"
        for (spell in Tables.stringList("dungeoneering_spells.list.components")) {
            val level = SpellRunes.magicLevel(book, spell) ?: break
            if (level > magicLevel) {
                break
            }
            spells.add(spell)
        }
        val spell = spells.random(random)
        for (item in SpellRunes.requiredItems(book, spell) ?: return) {
            items.add(ItemDrop("${item.key}_dungeoneering", item.value..item.value * 10))
        }
    }
}
