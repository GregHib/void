package content.skill.hunter

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.proj.shoot
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.clearHint
import world.gregs.voidps.engine.client.hint
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class Falconry : Script {
    init {
        npcOperate("Talk-to", "matthias") {
            if (hasFalcon()) {
                npc<Happy>("How are you finding Valor? Remember to bring him back once you're done.")
                choice {
                    option<Neutral>("I'd like to hand him back, thanks.") {
                        equipment.transaction {
                            set(EquipSlot.Weapon.index, null)
                        }
                        npc<Happy>("I hope you enjoyed the experience.")
                    }
                    option<Happy>("I'll hang on to him a while longer.")
                }
                return@npcOperate
            }
            npc<Happy>("Greetings. Can I help you at all? Perhaps you'd be interested in trying your hand at falconry?")
            choice {
                option<Quiz>("Could you tell me more about that?") {
                    npc<Neutral>("For 500 coins you can borrow one of my birds and try to catch some of the kebbits around here. I'll want the bird back when you're done, of course.")
                    choice {
                        option<Happy>("Sounds good to me.") {
                            startFalconry()
                        }
                        option<Neutral>("Maybe some other time.")
                    }
                }
                option<Neutral>("No thanks, I'm fine.")
            }
        }

        npcOperate("Falconry", "matthias") {
            startFalconry()
        }

        npcApproach("Catch", "spotted_kebbit,dark_kebbit,dashing_kebbit") { (target) ->
            approachRange(6, update = false)
            catch(target)
        }

        npcOperate("Retrieve", "spotted_kebbit_caught,dark_kebbit_caught,dashing_kebbit_caught") { (target) ->
            retrieve(target)
        }

        npcDespawn("*_kebbit_caught") {
            val player = owner ?: return@npcDespawn
            val hint = get("hint", -1)
            if (hint != -1) {
                player.clearHint(hint)
            }
            if (lifecycle != 0) {
                return@npcDespawn
            }
            if (player.equipped(EquipSlot.Weapon).id == "falconers_glove") {
                player.equipment.replace("falconers_glove", "falconers_glove_2")
                player.message("Your falcon gives up on its catch and returns to your glove.")
            }
        }

        exited("piscatoris_falconry_area") {
            if (hasFalcon()) {
                equipment.transaction {
                    set(EquipSlot.Weapon.index, null)
                }
                message("You hand the falcon back to Matthias before leaving.")
            }
            val caught = NPCs.firstOrNull { it.id.endsWith("_kebbit_caught") && it.owner == this }
            if (caught != null) {
                NPCs.remove(caught)
            }
        }
    }

    private fun Player.hasFalcon(): Boolean {
        val weapon = equipped(EquipSlot.Weapon).id
        return weapon == "falconers_glove" || weapon == "falconers_glove_2"
    }

    private suspend fun Player.startFalconry() {
        if (hasFalcon()) {
            npc<Neutral>("You've already got one of my birds.")
            return
        }
        if (!has(Skill.Hunter, 43, message = false)) {
            npc<Neutral>("I'm afraid my birds are hard to handle. You'll need a Hunter level of at least 43 before I can let you use one.")
            return
        }
        if (equipped(EquipSlot.Weapon).isNotEmpty() || equipped(EquipSlot.Shield).isNotEmpty()) {
            npc<Neutral>("You'll need both hands free to handle the bird. Come back once you've put away what you're holding.")
            return
        }
        if (!inventory.remove("coins", 500)) {
            npc<Sad>("I'm afraid it's 500 coins if you want to borrow a bird, and it looks like you don't have enough on you.")
            return
        }
        equipment.transaction {
            set(EquipSlot.Weapon.index, Item("falconers_glove_2"))
        }
        npc<Happy>("Here you go then. Treat Valor well, and bring him back when you're done.")
    }

    private suspend fun Player.catch(target: NPC) {
        val row = Rows.getOrNull("falconry.${target.id}") ?: return
        val weapon = equipped(EquipSlot.Weapon).id
        if (weapon == "falconers_glove") {
            message("Your falcon is already off chasing prey.")
            return
        }
        if (weapon != "falconers_glove_2") {
            message("You need a trained falcon to catch this kebbit.")
            return
        }
        if (!has(Skill.Hunter, row.int("level"), message = true)) {
            return
        }
        face(target)
        sound("falcon_swoop")
        shoot("gyr_falcon", target)
        delay(2)
        if (target.tile.distanceTo(tile) > 8) {
            return
        }
        val success = Level.success(levels.get(Skill.Hunter), row.intRange("chance"))
        if (!success) {
            target.shoot("gyr_falcon", this)
            sound("falcon_return", delay = 20)
            message("The kebbit is too quick for your falcon.")
            return
        }
        equipment.replace("falconers_glove_2", "falconers_glove")
        target.levels.set(Skill.Constitution, 0)
        val caught = NPCs.add(row.npc("caught"), target.tile, ticks = 100, owner = this)
        caught["hint"] = hint(caught)
        message("Your falcon successfully swoops down on the kebbit.")
    }

    private suspend fun Player.retrieve(target: NPC) {
        val id = target.id.removeSuffix("_caught")
        val row = Rows.getOrNull("falconry.$id") ?: return
        if (target["owner", ""] != accountName) {
            message("This isn't your falcon.")
            return
        }
        val loot = listOf(row.item("fur"), "bones")
        if (inventory.spaces < loot.size) {
            val slots = loot.size - inventory.spaces
            message("You don't have enough inventory space. You need $slots more free ${"slot".plural(slots)}.")
            return
        }
        anim("net_catch")
        delay(1)
        NPCs.remove(target)
        equipment.replace("falconers_glove", "falconers_glove_2")
        for (item in loot) {
            inventory.add(item)
        }
        exp(Skill.Hunter, row.int("xp") / 10.0)
        message("You retrieve your falcon and its catch.")
    }
}
