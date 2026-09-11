package content.minigame.mage_training_arena

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.entity.player.inv.item.addOrDrop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.transact.operation.ReplaceItem.replace

/**
 * The Pizazz Progress Hat handed out by the Entrance Guardian; its tier reflects total points earned.
 */
class PizazzHat : Script {

    init {
        npcOperate("Talk-to", "entrance_guardian") {
            player<Neutral>("Hi.")
            if (get("mage_training_arena_started", false)) {
                menu()
            } else {
                npc<Neutral>("Greetings. What wisdom do you seek?")
                choice {
                    option<Neutral>("I'm new to this place. Where am I?") {
                        about()
                    }
                    option<Neutral>("None, I don't really care.")
                }
            }
        }

        itemOption("Talk-to", "progress_hat*") {
            talk()
        }

        itemOption("Talk-to", "progress_hat*", inventory = "worn_equipment") {
            talk()
        }

        itemOption("Destroy", "progress_hat*") { (item, slot) ->
            npc<Neutral>("pizazz_hat", "How dare you destroy me? You'll lose your Pizazz Points!")
            choice("Destroy Hat?") {
                option("Yes") {
                    if (!inventory.remove(slot, item.id, item.amount)) {
                        return@option
                    }
                    sound("destroy_object")
                    PizazzPoints.reset(this)
                    statement("The hat whispers as you destroy it. You can get another from the Entrance Guardian.")
                }
                option("No") {
                    npc<Neutral>("pizazz_hat", "I think so too!")
                }
            }
        }
    }

    private suspend fun Player.talk() {
        val greeting = greetings.random()
        player<Neutral>(greeting.first)
        npc<Neutral>("pizazz_hat", greeting.second)
        player<Quiz>(greeting.third)
        npc<Neutral>("pizazz_hat", "Ok, I suppose it's my job. You have: ${points(this)}")
        player<Neutral>("Thank you!")
    }

    private suspend fun Player.menu() {
        choice {
            option<Neutral>("Can you tell me about this place again?") {
                about()
            }
            option<Neutral>("Can you explain the different portals?") {
                portals()
            }
            option<Neutral>("About the progress hat...") {
                if (MageTrainingArena.hasProgressHat(this)) {
                    npc<Neutral>("Which you have stored somewhere I'm sure.")
                    player<Neutral>("Yes. What's it for?")
                    npc<Neutral>("Collect Pizazz Points from the various areas and the hat will remember your totals. Talk to the hat at any time to find out what points you have. Go upstairs and talk to the Rewards Guardian to claim items for the points.")
                } else {
                    npc<Neutral>("You want another one don't you.")
                    player<Neutral>("Sorry, can I?")
                    give(this)
                    npc<Neutral>("Here you go. Talk to the hat to find out your current Pizazz Point totals.")
                }
                menu()
            }
            option<Neutral>("Thanks, bye!")
        }
    }

    private suspend fun Player.about() {
        npc<Neutral>("Well young one, you have entered the Magic Training Arena. It was built at the start of the Fifth Age, when runestones were first discovered. It was made because of the many pointless accidents caused by inexperienced mages.")
        player<Neutral>("Who created it?")
        npc<Neutral>("Good question. It was originally made by the ancestors of the wizards in the Wizards Tower. However, it was destroyed by melee and ranged warriors who took offence at the use of this new 'Magic Art'. Recently")
        npc<Neutral>("the current denizens of the Wizards Tower have resurrected the arena including various Guardians you will see as you look around. We are here to help and to ensure things run smoothly.")
        player<Neutral>("Interesting. So what can I do here?")
        npc<Neutral>("You may train up your skills in the magic arts by travelling through one of the portals at the back of this entrance hall. By training up in one of these areas you will be awarded special Pizazz Points unique to each room.")
        npc<Neutral>("With these points you may claim a variety of items from my fellow guardian up the stairs.")
        player<Neutral>("How do you record the points I have earned?")
        if (get("mage_training_arena_started", false)) {
            npc<Neutral>("With the Pizazz Progress Hat I gave you, of course.")
            player<Neutral>("OK.")
            npc<Neutral>("Oh, and a word of warning: should you decide to log out whilst in any of the rooms in the arena, you will be teleported to the entrance and have any items that you picked up in the room removed.")
            menu()
            return
        }
        npc<Neutral>("You really are full of questions! You will need a special Pizzaz Progress Hat! I can give you one if you so wish to train here.")
        player<Neutral>("Yes Please!")
        set("mage_training_arena_started", true)
        give(this)
        npc<Neutral>("Here you go. Talk to the hat to find out your current Pizzaz Points totals.")
        player<Neutral>("Talk to it?")
        npc<Neutral>("Well of course, it's a magic Pizazz Progress Hat! Mind your manners though, hats have feelings too!")
        player<Neutral>("Er... if you insist.")
        npc<Neutral>("Oh, and a word of warning: should you decide to leave the rooms by any method other than the exit portals, you will be teleported to the entrance and have any items that you picked up in the room removed.")
        player<Neutral>("Okay. Thanks!")
    }

    private suspend fun Player.portals() {
        npc<Neutral>("They lead to four areas to train your magic: The Telekinetic Theatre, The Alchemists' Playground, The Enchanting Chamber, and The Creature Graveyard.")
        choice {
            option<Neutral>("What's the Telekinetic Theatre?") {
                npc<Neutral>("In there you can earn Telekinetic Pizazz Points for trade upstairs.")
                player<Neutral>("What will I be doing in there?")
                npc<Neutral>("That depends on how much of a time-waster you are! You are required to use the Telekinetic Grab spell in order to move a statue through a maze. Casting the spell will move the statue towards you until it reaches a wall.")
                npc<Neutral>("So by standing on the different sides of the maze you can move the statue North, East, South or West. You will be rewarded Pizazz Points for each maze successfully solved.")
                portals()
            }
            option<Neutral>("What's the Alchemists' Playground?") {
                npc<Neutral>("In the playground you can earn Alchemist Pizazz Points for trading with the rewards guardian upstairs.")
                player<Neutral>("What's in there?")
                npc<Neutral>("You'll find eight cupboards containing items you can turn to gold using the low and high alchemy spells. The money you earn will be taken from you upon leaving the playground to pay for the upkeep of this training")
                npc<Neutral>("arena and to help fund magic shops around Gielinor. You will be rewarded with 1 Pizazz Point for every 100 coins deposited and a percentage of the money you create. Keep in mind that you will not be able to take more than 1000 coins back out with you.")
                player<Neutral>("Sounds simple.")
                portals()
            }
            option<Neutral>("What's the Enchanting Chamber?") {
                npc<Neutral>("In here you will be able to earn Enchantment Pizazz Points for trade upstairs.")
                player<Neutral>("What will I have to do?")
                npc<Neutral>("You will find yourself amongst various piles of shapes. You can pick up these shapes and use one of your enchanting spells upon it to morph it into an orb. You'll also see a hole in the centre of the room; put the orbs")
                npc<Neutral>("in here and for every 20 we will credit you with a gift. You will get Pizazz Points for every 10 shapes that you convert and the amount of points depends on the spell you use.")
                portals()
            }
            option<Neutral>("What's the Creature Graveyard?") {
                npc<Neutral>("In here you will be able to earn Graveyard Pizazz Points for trade upstairs.")
                player<Neutral>("But what do I have to do in there?")
                npc<Neutral>("Patience young one. A great many creatures die in this world and much of their remains can cause clutter. We have taken it upon ourselves to teleport many bones from these creatures into this graveyard for a")
                npc<Neutral>("use. It's up to you to practice your bones to bananas spell in order to put these bones into immediate use - nutritious food for monsters! Just convert the bones and put them in the holes in the walls. There is a")
                npc<Neutral>("drawback to this room however, the bones often fall on people so you may want to eat some of those bananas to stay alive. When people die we confiscate Pizazz Points for the effort of teleporting incompetent mages.")
                portals()
            }
            option<Neutral>("Thanks, bye!")
        }
    }

    companion object {
        val hats = listOf("progress_hat", "progress_hat_2", "progress_hat_3")

        private val greetings = listOf(
            Triple("Hello... Mr Progress Hat?", "That's me, why are you bothering me human?", "Sorry, but do you think you could tell me my Pizazz Points?"),
            Triple("Erm, excuse me Mr Progress Hat?", "Can't you see I'm busy?", "But you're just a hat? Can you tell me my Pizazz Point totals?"),
            Triple("Mr Progress Hat... sir?", "What do you want?", "Do you think you could tell me my Pizazz Points?"),
            Triple("Mr Progress Hat? Hello?", "I suppose you want to know your Pizazz Points.", "That would be nice, yes."),
        )

        fun points(player: Player): String = "${PizazzPoints.get(player, "telekinetic")} Telekinetic, ${PizazzPoints.get(player, "alchemist")} Alchemist, ${PizazzPoints.get(player, "enchanting")} Enchantment, and ${PizazzPoints.get(player, "graveyard")} Graveyard Pizazz Points."

        fun tier(player: Player): String {
            val total = PizazzPoints.total(player)
            return when {
                total > 600 -> "progress_hat_3"
                total > 300 -> "progress_hat_2"
                else -> "progress_hat"
            }
        }

        /**
         * Hands the player a fresh hat of the correct tier, replacing any they already carry.
         */
        fun give(player: Player) {
            for (hat in hats) {
                val held = player.inventory.count(hat)
                if (held > 0) {
                    player.message("The hat shifts unexpectedly.")
                    player.inventory.remove(hat, held)
                }
            }
            player.addOrDrop(tier(player))
        }

        /**
         * Swaps a carried or worn hat for the tier matching the player's current points.
         */
        fun refresh(player: Player) {
            val tier = tier(player)
            for (hat in hats) {
                if (hat == tier) {
                    continue
                }
                if (player.inventory.contains(hat)) {
                    player.inventory.transaction { replace(hat, tier) }
                    player.message("Your progress hat updates to reflect your achievements.")
                }
                if (player.equipment.contains(hat)) {
                    player.equipment.transaction { replace(hat, tier) }
                    player.message("Your progress hat updates to reflect your achievements.")
                }
            }
        }
    }
}
