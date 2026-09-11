package content.minigame.mage_training_arena

import content.entity.combat.hit.damage
import content.entity.gfx.areaGfx
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.entity.player.inv.item.addOrDrop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.an
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

/**
 * Bones grabbed from piles are converted to fruit and fed through the food chute for points while
 * bones rain down on everyone in the room.
 */
class CreatureGraveyard : Script {

    private val lives = HashMap<Tile, Int>()

    init {
        objectOperate("Grab", "bones_mage_training_arena_*") { (target) ->
            val row = bones.firstOrNull { it.obj("pile") == target.id } ?: return@objectOperate
            if (!inventory.add(row.item("item"))) {
                message("You have no free space!")
                return@objectOperate
            }
            anim("climb_down")
            sound("mta_deposit_bone")
            val remaining = lives.getOrDefault(target.tile, Tables.int("mta_graveyard.settings.pile_lives")) - 1
            if (remaining > 0) {
                lives[target.tile] = remaining
                return@objectOperate
            }
            lives.remove(target.tile)
            target.replace(row.obj("next"))
        }

        objectOperate("Deposit", "food_chute") {
            val bananas = inventory.count("banana")
            val peaches = inventory.count("peach")
            if (bananas + peaches == 0) {
                statement("You don't have any bananas or peaches to deposit.")
                return@objectOperate
            }
            if (bananas > 0 && !inventory.remove("banana", bananas)) {
                return@objectOperate
            }
            if (peaches > 0 && !inventory.remove("peach", peaches)) {
                return@objectOperate
            }
            val perReward = Tables.int("mta_graveyard.settings.fruit_per_reward")
            var total = get("mage_training_arena_fruit_deposited", 0) + bananas + peaches
            AuditLog.event(this, "mta_fruit_deposited", bananas + peaches, total)
            while (total >= perReward) {
                total -= perReward
                set("mage_training_arena_fruit_deposited", total)
                val rune = Tables.itemList("mta_graveyard.settings.rune_rewards").random()
                addOrDrop(rune)
                PizazzPoints.add(this, "graveyard", 1)
                exp(Skill.Magic, Tables.int("mta_graveyard.settings.xp") / 10.0)
                sound("mta_deposit_fruit")
                statement("Congratulations - you've been awarded ${ItemDefinitions.get(rune).name.lowercase().an()} and extra magic XP.")
            }
            set("mage_training_arena_fruit_deposited", total)
        }

        npcOperate("Talk-to", "graveyard_guardian") {
            player<Neutral>("Hello.")
            guardianMenu()
        }

        entered("mage_training_arena_creature_graveyard") {
            World.timers.startIfAbsent("mta_graveyard")
        }

        playerDeath {
            if (!MageTrainingArena.inRoom(this, "graveyard")) {
                return@playerDeath
            }
            val lost = PizazzPoints.get(this, "graveyard").coerceAtMost(Tables.int("mta_graveyard.settings.death_penalty"))
            if (lost <= 0) {
                return@playerDeath
            }
            PizazzPoints.remove(this, "graveyard", lost)
            message("You lost $lost Pizazz Points upon death!")
        }

        worldTimerStart("mta_graveyard") {
            Tables.int("mta_graveyard.settings.interval")
        }

        worldTimerTick("mta_graveyard") {
            val players = Players.filter { MageTrainingArena.inRoom(it, "graveyard") }
            if (players.isEmpty()) {
                return@worldTimerTick Timer.CANCEL
            }
            for (tile in Tables.tileList("mta_graveyard.settings.bone_drops")) {
                if (random.nextInt(12) < 8) {
                    areaGfx("mta_falling_bones", tile)
                }
            }
            val hit = Tables.int("mta_graveyard.settings.hit")
            for (player in players) {
                player.sound("mta_bone_fall")
                if (player.dialogue == null) {
                    player.damage(hit)
                }
            }
            Timer.CONTINUE
        }
    }

    private suspend fun Player.guardianMenu() {
        choice {
            option<Neutral>("What do I have to do in this room?") {
                npc<Neutral>("Have you noticed all the bones around the room? These are teleported here from all over Gielinor to help clean up the landscape of countless bones left behind from combat. What better use for these bones than to")
                npc<Neutral>("convert them to nutritious fruit to be eaten by you mortals? You have to use your Bones to Bananas spell to convert the bones and then place them in the holes on the walls to earn Graveyard Pizazz Points.")
                npc<Neutral>("Unluckily for you, your health will constantly decrease from getting hit by these dropping bones so you will probably want to eat some of the bananas yourself to increase your stay here.")
                guardianMenu()
            }
            option<Neutral>("What are the rewards?") {
                npc<Neutral>("You will get experience from casting your bones to bananas spell and you will get Graveyard Pizazz Points when you put the bananas though the wall. Occasionally you will also be credited with a runestone")
                npc<Neutral>("to help you in your future spell casting.")
                guardianMenu()
            }
            option<Neutral>("Got any tips that may help me?") {
                npc<Neutral>("Different bones will provide you with different numbers of bananas, so try to find the best type. Collect enough points and you will be able to buy a 'Bones to Peaches' spell from my fellow guardian above the entrance hall.")
                npc<Neutral>("This spell can be used here just like the bones to bananas spell, except this spell will give you even more experience and the peaches will restore more health!")
                player<Neutral>("I see.")
                npc<Neutral>("Oh, and a word of warning: should you decide to leave this room by a method other than the exit portals, you will be teleported to the entrance and have any items that you picked up in the room removed.")
            }
            option<Neutral>("Thanks, bye!")
        }
    }

    companion object {
        val bones: List<RowDefinition>
            get() = Tables.get("mta_bones").rows()
    }
}
