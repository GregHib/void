package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.skill.summoning.familiarTeleport
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

class SpiritKyatt : Script {
    init {
        npcOperate("Interact", "spirit_kyatt_familiar") {
            choice {
                option("Chat") {
                    chat()
                }
                // The kyatt carries its owner home to the Piscatoris hunter area.
                option("Teleport") {
                    familiarTeleport(Tile(2326, 3636), "the kyatt")
                }
            }
        }
    }

    private suspend fun Player.chat() {
            if (inventory.contains("ball_of_wool") || inventory.contains("ball_of_black_wool")) {
                npc<Neutral>("Human, hand me that ball of wool.")
                player<Happy>("Aww...do you want to play with it?")
                npc<Neutral>("I do not 'play', human.")
                player<Happy>("If you say so, kitty! Alright, you can have it.")
                npc<Neutral>("Aha! Ball of wool: you are mine now. I will destroy you!")
                player<Happy>("Well I'm not giving it to you, now! I'll never get it back.")
                npc<Neutral>("Then you leave me no choice but to destroy YOU, human!")
                player<Happy>("Bad kitty!")
                return
            }
            when (random.nextInt(4)) {
                0 -> {
                    npc<Neutral>("Guess who wants a belly rub, human.")
                    player<Happy>("Umm...is it me?")
                    npc<Neutral>("No, human, it is not you. Guess again.")
                    player<Happy>("Is it the Duke of Lumbridge?")
                    npc<Neutral>("You try my patience, human!")
                    player<Happy>("Is it Zamorak? That would explain why he's so cranky.")
                    npc<Neutral>("Please do not make me destroy you before I get my belly rub!")
                }
                1 -> {
                    player<Happy>("Here, kitty!")
                    npc<Neutral>("What do you want, human?")
                    player<Happy>("I just thought I would see how you were.")
                    npc<Neutral>("I do not have time for your distractions. Leave me be!")
                    player<Happy>("Well, sorry! Would a ball of wool cheer you up?")
                    npc<Neutral>("How dare you insult my intelli- what colour wool?")
                    player<Happy>("Umm...white?")
                    npc<Neutral>("I will end you!")
                }
                2 -> {
                    player<Happy>("Hello, kitty cat!")
                    npc<Neutral>("Human, leave me be. I'm far too busy to deal with your nonsense.")
                    player<Happy>("What are you up to?")
                    npc<Neutral>("I am engaged in an intricate dirt-purging operation!")
                    player<Happy>("Aww, kitty's cleaning his paws! How cute!")
                    npc<Neutral>("Know this, human. Once I finish cleaning my paws...")
                    npc<Neutral>("I will destroy you!")
                }
                3 -> {
                    player<Happy>("Here, kitty!")
                    npc<Neutral>("Do not toy with me, human!")
                    player<Happy>("What about under your chin?")
                    npc<Neutral>("I am not one of your playful kittens, human. I eat playful kittens for breakfast!")
                    player<Happy>("Not even behind your ears?")
                    statement("You lean down and tickle the kyatt behind the ears.")
                    npc<Neutral>("I will...purrrrr...ooh that's quite nice...destroy...purrrrrrr...you.")
                }
            }
    }
}
