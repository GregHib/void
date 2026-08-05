package content.skill.dungeoneering

import content.quest.smallInstance
import content.skill.magic.spell.SpellRunes
import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.intArg
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Interpolation
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.entity.item.drop.ItemDrop
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.type.Delta
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random
import kotlin.math.min

class Dungeoneering(val dropTables: DropTables) : Script {
    init {
        adminCommand("start_dungeon", intArg("floor", optional = true), stringArg("size", autofill = setOf("small", "medium", "large"), optional = true), intArg("complexity", optional = true)) { args ->
//            setRandom(Random(0L))
            println("Generating dungeon...")
            val floor = args.getOrNull(0)?.toInt() ?: 1
            val size = DungeonSize.valueOf((args.getOrNull(1) ?: "small").toTitleCase())
            val complexity = args.getOrNull(2)?.toInt() ?: 1
            val generator = DungeonGenerator(
                size = size,
                floor = floor,
                complexity = complexity,
            )
            set("show_daemonheim_map", true)
            val skills = Skill.all.associateWith { levels.getMax(it) }
            message("")
            message("- Welcome to Daemonheim -")
            message("Floor <purple>$floor</col>    Complexity <purple>$complexity")
            message("Dungeon Size: <purple>$size")
            message("Party Size:Difficulty <purple>1:1")
            message("<purple>Guide Mode OFF")
            message("")
            val start = System.currentTimeMillis()
            val dungeon = generator.generate(skills)
            println("Took ${System.currentTimeMillis() - start}ms")

            set("dungeon", dungeon)

            dungeon.prettyPrint()
            val instance = smallInstance(logout = false)
            dungeon.region = instance
            val startRoom = dungeon.start()
            startRoom.open(this, dungeon)
            dungeon.players.add(index)
            val center = instance.tile.add(startRoom.tile.x * 16 + 8, startRoom.tile.y * 16 + 8)
            NPCs.add("smuggler_dungeoneering", center)
            spawnTableItems(instance.tile, complexity, dungeon, skills)
            delay(1) // Delay to avoid ghosting # 1000
            tele(center.addY(-2))
        }

        adminCommand("dungeon") {
            val dungeon = dungeonMap ?: return@adminCommand
            dungeon.prettyPrint(6) { renderGrid ->
                for (row in renderGrid) {
                    message(
                        row.joinToString("")
                            .replace(" ", "  ")
                            .replace("|  ", "|    ")
                            .replace("  |", "    |")
                            .replace("Crit", "Crit ")
                            .replace("   Base ", "Base")
                            .replace(" Norm ", "Norm"),
                        ChatType.Console,
                    )
                }

                message("\nRoom Keys:", ChatType.Console)
                var keysFound = false
                for (y in dungeon.height - 1 downTo 0) {
                    for (x in 0 until dungeon.width) {
                        val room = dungeon.grid[y * dungeon.width + x] ?: continue
                        if (room.keys.isNotEmpty()) {
                            keysFound = true
                            val keysStr = room.keys.joinToString(", ") { DungeonMap.getKeyAbbrev(it).lowercase() }
                            val typeStr = when (room.type) {
                                DungeonRoomType.Base -> "Start Room"
                                DungeonRoomType.Boss -> "Boss Room"
                                DungeonRoomType.Puzzle -> "Puzzle Room"
                                DungeonRoomType.Normal -> if (room.isCritical) "Critical Normal" else "Bonus Normal"
                            }
                            message("  Room at ($x, $y) [$typeStr] contains key(s): [ $keysStr ]", ChatType.Console)
                        }
                    }
                }
                if (!keysFound) {
                    message("  No keys placed.", ChatType.Console)
                }
            }
        }

        adminCommand("unlock_dungeon") {
            val dungeon = dungeonMap ?: return@adminCommand
            for (room in dungeon.grid) {
                if (room == null || room.open) {
                    continue
                }
                room.open(this, dungeon)
            }
        }
    }

    private fun giveItems(instance: Tile, complexity: Int, dungeon: DungeonMap, skills: Map<Skill, Int>) {
        // Bound weapons - equip first weapon bound https://web.archive.org/web/20150406211914/http://www.xp-waste.com/weapon-wielded-at-the-start-of-a-floor-t2478.html
        // Equip type of ring of kinship
        if (complexity == 1) {
            // Add armour and spells of all styles to inventory
        }
    }

    private val fish = arrayOf("heim_crab", "red_eye", "dusk_eel", "giant_flatfish", "short_finned_eel", "web_snipper", "bouldabass", "salve_eel", "blue_crab", "cave_moray")

    private fun spawnTableItems(instance: Tile, complexity: Int, dungeon: DungeonMap, skills: Map<Skill, Int>) {
        val items = mutableListOf<ItemDrop>()
        val startRoom = dungeon.start()
        val tile = instance.add(startRoom.tile.x * 16, startRoom.tile.y * 16)
        val limit = (skills.getValue(Skill.Constitution) / 100).coerceAtMost(if (World.members) 10 else 5)
        repeat(7 + (dungeon.players.size * 2)) {
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

    companion object {
        fun rotateX(x: Int, y: Int, rotation: Int, size: Int) = when (rotation) {
            0 -> x
            1 -> size - y
            2 -> size - x
            3 -> y
            else -> x
        }

        fun rotateY(x: Int, y: Int, rotation: Int, size: Int) = when (rotation) {
            0 -> y
            1 -> x
            2 -> size - y
            3 -> size - x
            else -> y
        }
    }
}
