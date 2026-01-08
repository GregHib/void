package content.skill.ranged.ammo

import content.entity.player.combat.special.specialAttack
import content.skill.melee.weapon.fightStyle
import content.skill.melee.weapon.weapon
import content.skill.ranged.Ammo
import content.skill.ranged.ammo
import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.AmmoDefinitions
import world.gregs.voidps.engine.data.definition.WeaponStyleDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasUseLevel
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inject
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class Ammo : Script {

    val ammoDefinitions: AmmoDefinitions by inject()
    val weaponStyles: WeaponStyleDefinitions by inject()

    init {
        combatPrepare {
            !(fightStyle != "range" && weapon.def["weapon_style", 0] == 21 && !checkAmmo(this)) // Salamanders
        }

        combatSwing(style = "blaze") { target ->
            if (weapon.def["weapon_style", 0] == 21) { // Salamanders
                Ammo.remove(this, target, ammo, Ammo.requiredAmount(weapon, false))
            }
        }

        combatSwing(style = "melee") { target ->
            if (weapon.def["weapon_style", 0] == 21) { // Salamanders
                Ammo.remove(this, target, ammo, Ammo.requiredAmount(weapon, false))
            }
        }

        combatPrepare("range") {
            if (!checkAmmo(this)) {
                sound("out_of_ammo")
                false
            } else {
                true
            }
        }
    }

    fun checkAmmo(player: Player): Boolean {
        player.ammo = ""
        if (!player.hasUseLevel(Skill.Ranged, player.weapon, message = true)) {
            player.specialAttack = false
            player.message("You are not high enough level to use this weapon.")
            player.message("You need to have a Ranged level of ${player.weapon.def.get<Int>("secondary_use_level")}.")
            return false
        }
        val style = weaponStyles.get(player.weapon.def["weapon_style", 0])
        val group = player.weapon.def["ammo_group", ""]
        val slot = when (style.stringId) {
            "pie", "chinchompa", "thrown" -> EquipSlot.Weapon
            "crossbow", "fixed_device", "salamander" -> EquipSlot.Ammo
            "bow" -> if (group == "none") EquipSlot.Weapon else EquipSlot.Ammo
            else -> EquipSlot.Weapon
        }
        if (slot == EquipSlot.Weapon) {
            val weapon = player.equipped(EquipSlot.Weapon)
            when {
                weapon.id == "zaryte_bow" -> player.ammo = "zaryte_arrow"
                weapon.id.endsWith("sling") -> player.ammo = "sling_rock"
                weapon.id.startsWith("crystal_bow") -> player.ammo = "special_arrow"
                else -> player.ammo = weapon.id
            }
            return true
        }
        val ammo = player.equipped(slot)
        if (!player.hasUseLevel(Skill.Ranged, ammo)) {
            player.message("You are not high enough level to use this item.")
            player.message("You need to have a Ranged level of ${ammo.def.get<Int>("secondary_use_level")}.")
            return false
        }
        val required = Ammo.requiredAmount(player.weapon, player.specialAttack)
        if (ammo.amount < required) {
            player.message("There is no ammo left in your quiver.")
            return false
        }
        if (!usable(group, ammo, player)) {
            player.message("You can't use that ammo with your ${style.stringId.toLowerSpaceCase()}.")
            return false
        }
        // Ammo is kept track of as EquipSlot.Ammo could've been used up
        if (ammo.id == "barbed_bolts" || ammo.id == "bone_bolts") {
            player.ammo = ammo.id
        } else if (group == "crossbow") {
            player.ammo = "crossbow_bolt"
        } else if (ammo.id.endsWith("fire_arrows_lit")) {
            player.ammo = "fire_arrows_lit"
        } else if (ammo.id.endsWith("fire_arrows_unlit")) {
            player.ammo = "fire_arrows_unlit"
        } else {
            player.ammo = ammo.id
        }
        return true
    }

    private fun usable(group: String, ammo: Item, player: Player): Boolean {
        val weapon = player.equipped(EquipSlot.Weapon)
        if (weapon.def["dungeoneering", 0] != ammo.def["dungeoneering", 0]) {
            return false
        }
        val weaponReq: Map<Skill, Int>? = weapon.def.getOrNull("equip_req")
        val weaponLevel = weaponReq?.get(Skill.Ranged) ?: 1
        val ammoReq: Map<Skill, Int>? = ammo.def.getOrNull("equip_req")
        val ammoLevel = ammoReq?.get(Skill.Ranged) ?: 1
        if (ammoLevel > weaponLevel) {
            return false
        }
        if (ammo.id == "bone_bolts" && (weapon.id == "dorgeshuun_crossbow" || weapon.id == "zaniks_crossbow")) {
            return true
        }
        if (weapon.id.startsWith("bow_class_") && ammo.id != weapon.id.replace("bow_", "arrows_")) {
            return false
        }
        val definition = ammoDefinitions.get(group)
        return definition.items.contains(ammo.id)
    }

}
