package content.entity.player.equip

import com.github.michaelbull.logging.InlineLogger
import content.entity.sound.sound
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.ItemOption
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.appearance
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.equip.EquipType
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.equip.has
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasRequirements
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.slot
import world.gregs.voidps.engine.entity.item.type
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.*
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class Equipping : Script {

    val areas: AreaDefinitions by inject()
    val logger = InlineLogger()

    init {
        playerSpawn(::updateWeaponEmote)

        itemOption("Wield", block = ::equip)
        itemOption("Wear", block = ::equip)
        itemOption("Hold", block = ::equip)
        itemOption("Equip", block = ::equip)

        itemOption("Remove", inventory = "worn_equipment") { (item, slot) ->
            if (item.id == "gas_mask" && tile in areas["west_ardougne"]) {
                message("You should leave your gas mask on while you're in West Ardougne.")
                return@itemOption
            }
            if (item.id == "rubber_chicken" || item.id == "easter_carrot") {
                options.remove("Whack")
            }
            equipment.move(slot, inventory)
            when (equipment.transaction.error) {
                TransactionError.None -> playEquipSound(this, item.def)
                is TransactionError.Full -> inventoryFull()
                else -> {}
            }
        }

        inventoryChanged("worn_equipment", EquipSlot.Weapon) { player ->
            updateWeaponEmote(player)
        }
    }

    fun equip(player: Player, it: ItemOption) {
        val (item, slot) = it
        val def = item.def
        if (!player.hasRequirements(item, true)) {
            return
        }
        if (item.id.contains("greegree") && player.tile !in areas["ape_atoll"] && player.tile !in areas["ape_atoll_agility_dungeon"]) {
            player.message("You attempt to use the Monkey Greegree but nothing happens.")
            return
        }
        if (item.slot == EquipSlot.Hat && player.tile in areas["west_ardougne"]) {
            player.message("You should leave your gas mask on while you're in West Ardougne.")
            return
        }
        if (item.id.endsWith("goblin_mail")) {
            player.message("That armour is too small for a human.")
            return
        }
        if (replaceWeaponShieldWith2h(player, def) && !player.equipment.move(EquipSlot.Shield.index, player.inventory)) {
            player.inventoryFull()
            return
        }
        if (replace2hWithShield(player, def) || replaceShieldWith2h(player, def)) {
            player.inventory.move(slot, player.equipment, item.slot.index)
            player.equipment.move(getOtherHandSlot(item.slot).index, player.inventory)
        } else {
            val target = player.equipment[item.slot.index]
            if (item.id == target.id && player.equipment.stackable(target.id)) {
                player.inventory.move(slot, player.equipment, item.slot.index)
            } else {
                player.inventory.swap(slot, player.equipment, item.slot.index)
            }
        }
        when (player.inventory.transaction.error) {
            TransactionError.None -> {
                player.flagAppearance()
                playEquipSound(player, def)
            }
            else -> logger.warn { "Failed to equip item $player $item ${item.slot.index}" }
        }
    }

    fun replaceWeaponShieldWith2h(player: Player, item: ItemDefinition) = player.has(EquipSlot.Shield) && player.has(EquipSlot.Weapon) && item.type == EquipType.TwoHanded

    fun replaceShieldWith2h(player: Player, item: ItemDefinition) = player.has(EquipSlot.Shield) && !player.has(EquipSlot.Weapon) && item.type == EquipType.TwoHanded

    fun replace2hWithShield(player: Player, item: ItemDefinition) = player.equipped(EquipSlot.Weapon).type == EquipType.TwoHanded && item.slot == EquipSlot.Shield

    fun getOtherHandSlot(slot: EquipSlot) = if (slot == EquipSlot.Shield) EquipSlot.Weapon else EquipSlot.Shield

    fun updateWeaponEmote(player: Player) {
        val weapon = player.equipped(EquipSlot.Weapon)
        val emote = weapon.def["render_emote", 1426]
        player.appearance.emote = emote
        player.flagAppearance()
    }

    fun playEquipSound(player: Player, item: ItemDefinition) {
        val name = item.name.lowercase()
        val material = item["material", "cloth"]
        var sound = when (item.slot) {
            EquipSlot.Weapon -> when {
                // Might be able to improve using attack strategies
                name.contains("mystic") && name.contains("staff") -> "equip_elemental_staff"
                name.contains("spear") || name.contains("staff") || name.contains("javelin") || name.contains("hasta") || name.contains("halberd") -> "equip_spear"
                name.contains("hammer") || name.contains("maul") -> "equip_hammer"
                name.contains("mace") || name.contains("flail") -> "equip_mace"
                name.contains("bow") || name.contains("knife") || name.contains("dart") || name.contains("thrownaxe") -> "equip_range"
                name.contains("whip") -> "equip_whip"
                name.contains("hatchet") -> "equip_hatchet"
                name.contains("axe") -> "equip_axe"
                name.contains("salamander") -> "equip_salamander"
                name.contains("banner") -> "equip_banner"
                name.contains("blunt") -> "equip_blunt"
                name.contains("ghost buster 500") -> "equip_halloween_spray"
                name == "sled" -> "equip_sled"
                name == "dark bow" -> "equip_darkbow"
                name == "silverlight" -> "equip_silverlight"
                else -> if (material == "metal") "equip_sword" else "equip_clothes"
            }

            EquipSlot.Hat -> when {
                name == "jack lantern mask" -> "equip_halloween_pumpkin"
                else -> if (material == "metal") "equip_helm" else "equip_clothes"
            }

            EquipSlot.Chest -> if (material == "metal") "equip_body" else "equip_clothes"
            EquipSlot.Shield -> if (material == "metal") "equip_shield" else "equip_clothes"
            EquipSlot.Legs -> if (material == "metal") "equip_legs" else "equip_clothes"
            EquipSlot.Feet -> if (material == "metal") "equip_feet" else "equip_clothes"
            EquipSlot.Hands -> if (material == "metal") "equip_hands" else "equip_clothes"
            EquipSlot.Ammo -> if (material == "metal") "equip_bolts" else "equip_range"
            EquipSlot.Ring -> if (name == "beacon ring") "equip_surok_ring" else "equip_clothes"
            else -> "equip_clothes"
        }
        if (name.contains("jester")) {
            sound = "equip_jester"
        }
        if (material == "leather") {
            sound = "equip_leather"
        }
        if (material == "wood") {
            sound = "equip_wood"
        }
        player.sound(sound)
        player.queue.clearWeak()
    }
}
