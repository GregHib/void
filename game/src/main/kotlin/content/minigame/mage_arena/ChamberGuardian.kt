package content.minigame.mage_arena

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.carriesItem
import world.gregs.voidps.engine.inv.inventory

class ChamberGuardian : Script {

    init {
        npcOperate("Talk-to", "chamber_guardian") {
            when {
                get("mage_arena", "unstarted") == "staff_received" -> anotherStaff()
                heldCape() != null -> grantStaff()
                else -> intro()
            }
        }
    }

    fun Player.heldCape(): String? = listOf("saradomin", "guthix", "zamorak").firstOrNull { carriesItem("${it}_cape") }

    suspend fun Player.intro() {
        player<Neutral>("Hello my friend, Kolodion sent me down.")
        npc<Neutral>("Sssshhh... the gods are talking. I can hear their whispers. Can you hear them adventurer, they're calling you.")
        player<Confused>("Erm... ok!")
        npc<Neutral>("Go chant at the statue of the god you most wish to represent in this world, you will be rewarded. Once you are done, come back to me. I shall supply you with a mage staff ready for battle.")
    }

    suspend fun Player.grantStaff() {
        val god = heldCape() ?: return
        player<Neutral>("Hi.")
        npc<Quiz>("Hello adventurer, have you made your choice?")
        player<Happy>("I have.")
        npc<Happy>("Good, good, I hope you have chosen well. I will now present you with a magic staff. This, along with the cape awarded to you by your chosen god, are all the weapons and armour you will need here.")
        if (inventory.isFull()) {
            player<Neutral>("Sorry, I don't have enough inventory space.")
            return
        }
        inventory.add("${god}_staff")
        set("mage_arena", "staff_received")
        item("${god}_staff", "The guardian hands you an ornate magic staff.")
    }

    suspend fun Player.anotherStaff() {
        player<Happy>("Hello again.")
        npc<Happy>("Hello adventurer, are you looking for another staff?")
        choice {
            option<Quiz>("What do you have to offer?") {
                openShop("mage_arena_staves")
            }
            option<Neutral>("No thanks.") {
                npc<Happy>("Well let me know if you need one.")
            }
        }
    }
}
