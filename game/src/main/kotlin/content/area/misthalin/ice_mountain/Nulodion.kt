package content.area.misthalin.ice_mountain

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Amazed
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.items
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import content.skill.ranged.weapon.CANNON_PARTS
import content.skill.ranged.weapon.returnCannon
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.carriesItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

/**
 * The dwarven armoury engineer at the Dwarven Mine entrance. He sells a whole multicannon through
 * dialogue, trades the pieces separately, and replaces a cannon that vanished on a world switch.
 */
class Nulodion : Script {

    init {
        npcOperate("Talk-to", "nulodion") { (target) ->
            if (!questCompleted("dwarf_cannon")) {
                player<Neutral>("Hello.")
                npc<Quiz>("Who are you? I only supply the Black Guard.")
                return@npcOperate
            }
            player<Neutral>("Hello.")
            npc<Happy>("Hello traveller, how's things?")
            player<Neutral>("Not bad thanks, yourself?")
            npc<Neutral>("I'm good, just working hard as usual...")
            menu(target)
        }

        npcOperate("Trade", "nulodion") { (target) ->
            if (!questCompleted("dwarf_cannon")) {
                npc<Quiz>("Who are you? I only supply the Black Guard.")
                return@npcOperate
            }
            openShop(target.def["shop"])
        }
    }

    private suspend fun Player.menu(target: NPC) {
        choice {
            option<Quiz>("I was hoping you might sell me a cannon.") {
                sellCannon(target)
            }
            option<Sad>("I've lost my cannon.") {
                replaceCannon()
            }
            option<Quiz>("I want to know more about the cannon.") {
                npc<Neutral>("There's only so much I can tell you, adventurer. We've been working on this little beauty for some time now.")
                player<Quiz>("Is it effective?")
                npc<Happy>("In short bursts it's very effective, the most destructive weapon to date. The cannon automatically targets monsters close by. You just have to make the ammo and let rip.")
                menu(target)
            }
            option<Neutral>("Well, take care of yourself then.") {
                npc<Neutral>("Indeed I will adventurer.")
            }
        }
    }

    private suspend fun Player.sellCannon(target: NPC) {
        val cost = Settings["world.objs.cannon.price", 750_000]
        npc<Neutral>("Hmmmmmm... I shouldn't really, but as you helped us so much, well, I could sort something out. I'll warn you though, they don't come cheap!")
        player<Quiz>("How much?")
        npc<Neutral>("For the full setup, $cost coins. Or I can sell you the separate parts... but it'll cost extra!")
        player<Amazed>("That's not cheap!")
        choice {
            option<Neutral>("Okay, I'll take a cannon please.") {
                buyCannon(cost)
            }
            option<Quiz>("Can I look at the separate parts please?") {
                npc<Happy>("Of course!")
                openShop(target.def["shop"])
            }
            option<Quiz>("Have you any ammo or instructions to sell?") {
                npc<Happy>("Of course!")
                openShop(target.def["shop"])
            }
            option<Neutral>("Sorry, that's too much for me.") {
                npc<Neutral>("Fair enough, it's too much for most of us.")
            }
        }
    }

    private suspend fun Player.buyCannon(cost: Int) {
        if (inventory.spaces < PURCHASE.size) {
            npc<Neutral>("Okay. There are four pieces to carry, plus the mould and instruction book, so you'll need to free up some space.")
            return
        }
        if (!carriesItem("coins", cost)) {
            npc<Neutral>("Okay, come back when you've got the $cost coins.")
            return
        }
        npc<Neutral>("Okay then, but keep it quiet... This thing's top secret!")
        inventory.transaction {
            remove("coins", cost)
            for (item in PURCHASE) {
                add(item)
            }
        }
        if (inventory.transaction.error != TransactionError.None) {
            return
        }
        items("coins", "cannon_base", "You give the cannon engineer $cost coins. He gives you the four parts that make the cannon, plus an ammo mould and an instruction manual.")
        npc<Neutral>("There you go, you be careful with that thing.")
        player<Happy>("Will do. Take care, mate.")
        npc<Happy>("Take care, adventurer.")
    }

    private suspend fun Player.replaceCannon() {
        if (contains("cannon_tile")) {
            npc<Neutral>("Hmmm. I think you'll find it's still happily parked on the spot where you put it.")
            player<Quiz>("Oh, is it still there? I thought I'd lost it.")
            npc<Laugh>("Ha ha ha, what a muddle-headed numpty you are!")
            player<Neutral>("...")
            return
        }
        if (inventory.spaces < CANNON_PARTS.size) {
            npc<Neutral>("That's unfortunate! But don't worry, I can sort you out if you free up some inventory space...")
            return
        }
        npc<Neutral>("That's unfortunate! But don't worry, I can sort you out...")
        val parts = get("cannon_lost_parts", 0)
        if (parts <= 0) {
            npc<Neutral>("Actually, I'm only allowed to replace cannons that were stolen in action. I'm sorry, but you'll have to buy a new set.")
            return
        }
        val balls = get("cannon_lost_balls", 0)
        clear("cannon_lost_parts")
        clear("cannon_lost_balls")

        returnCannon(parts, balls)
        items("cannon_barrels", "cannon_furnace", "The dwarf gives you a new cannon.")
        npc<Neutral>("Keep that quiet or I'll be in real trouble!")
        player<Happy>("Thanks a lot.")
    }

    private companion object {
        /**
         * A cannon bought whole comes with the mould and manual, hence the six free slots asked for.
         */
        private val PURCHASE = CANNON_PARTS + listOf("ammo_mould", "instruction_manual")
    }
}
