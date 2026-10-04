package content.area.asgarnia.burthorpe.warriors_guild

import content.entity.npc.shop.openShop
import content.entity.obj.door.doorTarget
import content.entity.obj.door.enterDoor
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

class WarriorsGuild : Script {

    init {
        objectOperate("Open", "warriors_guild_door_closed") { (target) ->
            val destination = doorTarget(this, target)
            val entering = destination != null && destination.x < tile.x
            if (entering && !canEnter()) {
                npc<Angry>("ghommal", "You not pass. You too weedy.")
                return@objectOperate
            }
            enterDoor(target)
        }

        npcOperate("Talk-to", "ghommal") {
            if (canEnter()) {
                npc<Happy>("Ghommal welcome you to Warrior Guild!") // TODO proper message
            } else {
                npc<Angry>("You not pass. You too weedy.")
            }
        }

        npcOperate("Claim-tokens", "gamfred,jimmy,ref,shanomi,harrallak_menarous") {
            claimTokens()
        }

        npcOperate("Talk-to", "anton") {
            npc<Happy>("Ahhh, hello there. How can I help?") // TODO proper message
            shopChoice("warriors_guild_armoury")
        }

        npcOperate("Talk-to", "lidio") {
            npc<Happy>("Greetings, warrior, how can I fill your stomach today?") // TODO proper message
            shopChoice("warriors_guild_food_shop")
        }

        npcOperate("Talk-to", "lilly") {
            npc<Happy>("Uh..... hi... didn't see you there. Can.... I help?") // TODO proper message
            shopChoice("warrior_guild_potion_shop")
        }
    }

    private suspend fun Player.shopChoice(shop: String) {
        choice {
            option("What do you have for sale?") {
                openShop(shop)
            }
            option<Neutral>("No thanks.")
        }
    }

    companion object {
        /**
         * https://runescape.wiki/w/Warriors%27_Guild
         */
        fun Player.canEnter(): Boolean {
            val attack = levels.getMax(Skill.Attack)
            val strength = levels.getMax(Skill.Strength)
            return attack + strength >= 130 || attack >= 99 || strength >= 99
        }

        fun Player.earnTokens(amount: Int) {
            inc("warriors_guild_tokens", amount)
        }

        suspend fun Player.claimTokens() {
            player<Quiz>("May I claim my tokens please?")
            val tokens: Int = get("warriors_guild_tokens", 0)
            if (tokens <= 0) {
                npc<Neutral>("I'm afraid you have not earned any tokens yet. Try some of the activities around the guild to earn some.")
                player<Neutral>("Ok, I'll go see what I can find.")
                return
            }
            npc<Happy>("Of course! Here you go, you've earned $tokens tokens!")
            if (!inventory.add("warrior_guild_token", tokens)) {
                player<Neutral>("Sorry, I don't seem to have enough inventory space.")
                return
            }
            set("warriors_guild_tokens", 0)
            player<Happy>("Thanks!")
        }
    }
}
