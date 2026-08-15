package content.skill.dungeoneering

import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.random
import kotlin.random.nextInt

object DungeonStartingItems {

    fun spawn(dungeon: DungeonMap, complexity: Int) {
        // Bound weapons - equip first weapon bound https://web.archive.org/web/20150406211914/http://www.xp-waste.com/weapon-wielded-at-the-start-of-a-floor-t2478.html
        for (member in dungeon.members) {
            // Equip type of ring of kinship
            val currentClass = member["kinship_class", "none"]
            val kinship = if (currentClass == "none") "ring_of_kinship" else "ring_of_kinship_$currentClass"
            member.equipment.transaction {
                set(EquipSlot.Ring.index, Item(kinship))
            }
            if (complexity == 1) {
                allocateGear(member)
            } else {
                // bound items
            }
        }
        // TODO group gatestone
    }

    private fun allocateGear(member: Player) {
        // Add armour and spells of all styles to inventory
        val defence = tierName(member, "melee", Skill.Defence)
        val attack = tierName(member, "melee", Skill.Attack)
        val rangedArmour = tierName(member, "ranged", Skill.Ranged)
        val rangedWeapon = tierName(member, "woodcutting", Skill.Ranged)
        val rangedAmmo = tierName(member, "melee", Skill.Ranged)
        val magicWeapon = tierName(member, "woodcutting", Skill.Magic)
        val magicArmour = tierName(member, "magic", Skill.Magic)
        member.inventory.transaction {
            add("${defence}_${if (random.nextBoolean()) "platebody" else "chainbody"}")
            add("${defence}_platelegs")
            add("${attack}_rapier")
            add("${attack}_battleaxe")
            add("${magicArmour}robe_top")
            add("${magicArmour}robe_bottom")
            val magic = member.levels.getMax(Skill.Magic)
            if (magic >= 5) {
                add("mind_rune_dungeoneering", random.nextInt(87..100))
            }
            if (magic >= 58 && !World.members) {
                add("chaos_rune_dungeoneering", random.nextInt(90..100))
            }
            if (magic >= 99 && !World.members) {
                add("fire_rune_dungeoneering", random.nextInt(90..98))
            }
            if (magic >= 99 && World.members) {
                add("blood_rune_dungeoneering", random.nextInt(96..119))
            }
            add("air_rune_dungeoneering", random.nextInt(86..165))
            if (random.nextBoolean()) {
                val staves = mutableListOf("water_staff_dungeoneering")
                if (magic >= 10) {
                    staves.add("earth_staff_dungeoneering")
                }
                if (magic >= 20) {
                    staves.add("fire_staff_dungeoneering")
                }
                if (magic >= 30) {
                    staves.add("air_staff_dungeoneering")
                }
                if (magic >= 50) {
                    staves.add("empowered_water_staff")
                }
                if (magic >= 60) {
                    staves.add("empowered_earth_staff")
                }
                if (magic >= 70) {
                    staves.add("empowered_fire_staff")
                }
                if (magic >= 80) {
                    staves.add("empowered_air_staff")
                }
                add(staves.random(random))
            } else {
                add("${magicWeapon}_staff")
            }
            add("${rangedArmour}_body")
            add("${rangedArmour}_chaps")
            add("${rangedWeapon}_shortbow")
            add("${rangedAmmo}_arrows", random.nextInt(100..110))
        }
    }

    private fun tierName(member: Player, style: String, skill: Skill): String = Tables.stringList("dungeoneering_tiers.$style.names")[((member.levels.getMax(skill) / 10) - 2).coerceAtMost(if (World.members) 8 else 7)]
}
