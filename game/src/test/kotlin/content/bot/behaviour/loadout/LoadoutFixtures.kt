package content.bot.behaviour.loadout

import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.EquipType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.PlayerLevels
import world.gregs.voidps.engine.inv.restrict.ValidItemRestriction
import world.gregs.voidps.engine.inv.stack.ItemDependentStack
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

/**
 * Shared item definitions and player setup for loadout tests.
 */
object LoadoutFixtures {

    data class Spec(val id: String, val slot: EquipSlot, val skill: Skill? = null, val level: Int = 0, val twoHanded: Boolean = false, val useSkill: Skill? = null, val useLevel: Int = 0)

    val specs = listOf(
        Spec("rune_scimitar", EquipSlot.Weapon, Skill.Attack, 40),
        Spec("steel_scimitar", EquipSlot.Weapon, Skill.Attack, 5),
        Spec("bronze_sword", EquipSlot.Weapon),
        Spec("dragon_2h_sword", EquipSlot.Weapon, Skill.Attack, 60, twoHanded = true),
        Spec("iron_2h_sword", EquipSlot.Weapon, Skill.Attack, 1, twoHanded = true),
        Spec("rune_kiteshield", EquipSlot.Shield, Skill.Defence, 40),
        Spec("wooden_shield", EquipSlot.Shield),
        Spec("rune_full_helm", EquipSlot.Hat, Skill.Defence, 40),
        Spec("iron_full_helm", EquipSlot.Hat, Skill.Defence, 10),
        Spec("shark", EquipSlot.None),
        Spec("rune_pickaxe", EquipSlot.Weapon, Skill.Attack, 40, useSkill = Skill.Mining, useLevel = 41),
        Spec("iron_pickaxe", EquipSlot.Weapon, useSkill = Skill.Mining, useLevel = 1),
        Spec("bronze_pickaxe", EquipSlot.Weapon),
    )

    fun define(extra: List<Spec> = emptyList()) {
        val all = specs + extra
        val defs = all.mapIndexed { index, spec ->
            val params = mutableMapOf<Int, Any>()
            if (spec.slot != EquipSlot.None) {
                params[Params.SLOT] = spec.slot
            }
            if (spec.twoHanded) {
                params[Params.TYPE] = EquipType.TwoHanded
            }
            if (spec.skill != null) {
                params[Params.id("equip_skill_1")] = spec.skill.ordinal
                params[Params.id("equip_level_1")] = spec.level
            }
            if (spec.useSkill != null) {
                params[Params.id("use_skill_1")] = spec.useSkill.ordinal
                params[Params.id("use_level_1")] = spec.useLevel
            }
            ItemDefinition(id = index, stringId = spec.id, params = params.ifEmpty { null })
        }
        ItemDefinitions.set(defs.toTypedArray(), all.mapIndexed { index, spec -> spec.id to index }.toMap())
    }

    fun player(): Player {
        val player = Player()
        player.experience.player = player
        player.levels.link(player, PlayerLevels(player.experience))
        player.inventories.validItemRule = ValidItemRestriction()
        player.inventories.player = player
        player.inventories.normalStack = ItemDependentStack
        player.inventories.inventory(InventoryDefinition(stringId = "inventory", length = 28))
        player.inventories.inventory(InventoryDefinition(stringId = "worn_equipment", length = 14))
        player.inventories.inventory(InventoryDefinition(stringId = "bank", length = 100))
        player.inventories.inventory(InventoryDefinition(stringId = "beast_of_burden", length = 30))
        return player
    }

    fun level(player: Player, skill: Skill, level: Int) {
        player.experience.set(skill, Level.experience(skill, level))
        player.levels.set(skill, level)
    }

    fun gear(id: String, vararg items: String) = Gear(id, items.toList())
}
