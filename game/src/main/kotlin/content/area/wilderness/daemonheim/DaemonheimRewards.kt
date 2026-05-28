package content.area.wilderness.daemonheim

import content.entity.player.dialogue.type.intEntry
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.StructDefinitions
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.charge
import world.gregs.voidps.engine.inv.charges
import world.gregs.voidps.engine.inv.inventory

class DaemonheimRewards : Script {
    init {
        variableSet("dungeoneering_tokens") { _, _, to ->
            if (interfaces.contains("daemonheim_rewards")) {
                interfaces.sendText("daemonheim_rewards", "tokens", to.toString())
            }
        }

        interfaceOpened("daemonheim_rewards") {
            interfaceOptions.unlockAll(it, "items", 0..205)
            interfaces.sendText(it, "tokens", get("dungeoneering_tokens", 0).toString())
        }

        interfaceOption("Select", "daemonheim_rewards:items") {
            val index = it.itemSlot / 5
            set("daemonheim_reward_slot", index)
        }

        interfaceOption("Buy", "daemonheim_rewards:buy") {
            val index: Int = get("daemonheim_reward_slot") ?: return@interfaceOption
            val value = EnumDefinitions.int(if (World.members) "dungeoneering_rewards_members" else "dungeoneering_rewards", index)
            if (value == -1) {
                return@interfaceOption
            }
            val struct = StructDefinitions.get(value)
            val item = ItemDefinitions.get(struct.get<Int>(Params.DUNGEONEERING_REWARD_ITEM)).stringId
            if (item == "dungeoneering_experience") {
                val count = intEntry("Please enter the number of tokens you wish to trade for XP.")
                if (count == 0) {
                    message("<red_orange>What's the point in that?")
                    return@interfaceOption
                }
                val tokens = get("dungeoneering_tokens", 0)
                if (count > tokens) {
                    message("You don't have that many tokens.")
                    return@interfaceOption
                }
                dec("dungeoneering_tokens", count)
                exp(Skill.Dungeoneering, count.toDouble())
                message("You trade in your tokens and gain $count Dungeoneering XP.")
                return@interfaceOption
            }
            interfaces.sendVisibility(it.id, "confirm_panel", true)
        }

        interfaceOption("Confirm", "daemonheim_rewards:confirm") {
            interfaces.sendVisibility(it.id, "confirm_panel", false)
            interfaces.sendVisibility(it.id, "buy_panel", false)
            val slot: Int = get("daemonheim_reward_slot") ?: return@interfaceOption
            val value = EnumDefinitions.int(if (World.members) "dungeoneering_rewards_members" else "dungeoneering_rewards", slot)
            if (value == -1) {
                return@interfaceOption
            }
            val struct = StructDefinitions.get(value)
            val cost: Int = struct[Params.DUNGEONEERING_REWARD_TOKENS]
            val tokens = get("dungeoneering_tokens", 0)
            if (tokens < cost) {
                message("You don't have enough tokens to buy that. You need $cost tokens.")
                return@interfaceOption
            }
            val level: Int = struct[Params.DUNGEONEERING_REWARD_LEVEL, 1]
            if (!has(Skill.Dungeoneering, level)) {
                message("You need a Dungeoneering level of $level to access this item.")
                return@interfaceOption
            }
            for (i in 1..2) {
                val key = "dungeoneering_reward_skill_$i"
                if (struct.contains(key)) {
                    val skill: Int = struct[key]
                    val level: Int = struct["dungeoneering_reward_level_$i"]
                    if (!has(Skill.all[skill], level)) {
                        val text: String = struct[Params.DUNGEONEERING_REWARD_LEVEL_REQ_TEXT]
                        message(text)
                        return@interfaceOption
                    }
                }
            }
            val id: Int = struct[Params.DUNGEONEERING_REWARD_ITEM]
            val item = ItemDefinitions.get(id)
            if (item.stringId == "dungeoneering_experience") {
                val count = intEntry("Please enter the number of tokens you wish to trade for XP.")
                if (count == 0) {
                    message("<red>What's the point in that?")
                    return@interfaceOption
                }
                val tokens = get("dungeoneering_tokens", 0)
                if (count > tokens) {
                    message("You don't have that many tokens.")
                    return@interfaceOption
                }
                dec("dungeoneering_tokens", count)
                exp(Skill.Dungeoneering, count.toDouble())
                message("You trade in your tokens and gain $count Dungeoneering XP.")
                return@interfaceOption
            }
            val index = inventory.freeIndex()
            if (!inventory.add(item.stringId)) {
                inventoryFull("to buy that")
                return@interfaceOption
            }
            val amount = item.getOrNull<Int>(Params.CHARGES)
            if (amount != null) {
                inventory.charge(this, index, (amount * 0.2).toInt()) // 20% starting charge
            }
            dec("dungeoneering_tokens", cost)
        }

        interfaceOption("Cancel", "daemonheim_rewards:cancel") {
            interfaces.sendVisibility(it.id, "confirm_panel", false)
            interfaces.sendVisibility(it.id, "buy_panel", false)
        }

        itemOption("Check-charges") { (item) ->
            val total = item.def.getOrNull<Int>(Params.CHARGES) ?: return@itemOption
            val charges = item.charges(this)
            val percent = (charges / total.toDouble()) * 100.0
            message("Your ${item.def.name.lowercase()} has ${String.format("%.1f", percent)}% of its charges left.")
        }
    }
}
