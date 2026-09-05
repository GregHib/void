package content.area.karamja.musa_point

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.quest.questStage
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.removeToLimit
import world.gregs.voidps.type.Tile

/**
 * Luthas' banana plantation job; fill his export crate with ten bananas for thirty coins.
 */
class Luthas : Script {

    init {
        npcOperate("Talk-to", "luthas_musa_point") {
            if (!get("banana_plantation_job", false)) {
                npc<Happy>("Hello, I'm Luthas, I run the banana plantation here.")
                introduction()
                return@npcOperate
            }
            if (get("banana_crate_bananas", 0) < CRATE_CAPACITY) {
                npc<Quiz>("Have you completed your task yet?")
                inProgress()
                return@npcOperate
            }
            player<Happy>("I've filled a crate with bananas.")
            npc<Happy>("Well done, here's your payment.")
            if (!inventory.add("coins", 30)) {
                inventoryFull()
                return@npcOperate
            }
            set("banana_crate_bananas", 0)
            set("banana_plantation_job", false)
            if (get("pirates_treasure_stashed_rum", false)) {
                set("pirates_treasure_stashed_rum", false)
                set("pirates_treasure_collected_rum", false)
                set("pirates_treasure_delivered_rum", true)
            }
            message("Luthas hands you 30 coins.")
            npc<Neutral>("If you go outside you should see the old crate has been loaded on to the ship, and there is another empty crate in its place.")
        }

        objectOperate("Search", "crate_14") { (target) ->
            if (target.tile != EXPORT_CRATE) {
                return@objectOperate
            }
            if (!get("banana_plantation_job", false)) {
                message("I don't know what goes in there.")
                return@objectOperate
            }
            val bananas = get("banana_crate_bananas", 0)
            val stashed = get("pirates_treasure_stashed_rum", false)
            when {
                bananas == 0 && stashed -> message("There is some rum in here, although with no bananas to cover it. It is a little obvious.")
                bananas == 0 -> message("The crate is completely empty.")
                bananas >= CRATE_CAPACITY -> message("The crate is full of bananas.")
                bananas == 1 -> message("The crate has 1 banana inside.")
                else -> message("The crate has $bananas bananas inside.")
            }
            if (stashed && bananas > 0) {
                message("There is also some rum stashed in here too.")
            }
        }

        objectOperate("Fill", "crate_14") { (target) ->
            if (target.tile != EXPORT_CRATE) {
                return@objectOperate
            }
            if (!get("banana_plantation_job", false)) {
                message("I don't know what goes in there.")
                return@objectOperate
            }
            val space = CRATE_CAPACITY - get("banana_crate_bananas", 0)
            if (space <= 0) {
                message("The crate is already full.")
                return@objectOperate
            }
            val packed = inventory.removeToLimit("banana", space)
            if (packed == 0) {
                message("You don't have any bananas to pack.")
                return@objectOperate
            }
            animDelay("take")
            inc("banana_crate_bananas", packed)
            if (!inventory.contains("banana")) {
                message("You pack all your bananas into the crate.")
            } else {
                message("You pack bananas into the crate until it is full.")
            }
        }

        itemOnObjectOperate("karamjan_rum", "crate_14") { (target) ->
            if (target.tile != EXPORT_CRATE) {
                return@itemOnObjectOperate
            }
            when {
                questStage("pirates_treasure") == 0 -> message("Why would I want to do that?")
                questStage("pirates_treasure") > 1 -> message("I see no reason to do that.")
                get("pirates_treasure_stashed_rum", false) -> message("There's already some rum in here...")
                !get("banana_plantation_job", false) -> message("I don't know what goes in there.")
                else -> {
                    if (inventory.remove("karamjan_rum")) {
                        animDelay("take")
                        set("pirates_treasure_stashed_rum", true)
                        message("You stash the rum in the crate.")
                    }
                }
            }
        }

        itemOnObjectOperate("banana", "crate_14") { (target) ->
            if (target.tile != EXPORT_CRATE) {
                return@itemOnObjectOperate
            }
            if (!get("banana_plantation_job", false)) {
                message("I don't know what goes in there.")
                return@itemOnObjectOperate
            }
            if (get("banana_crate_bananas", 0) >= CRATE_CAPACITY) {
                message("The crate is already full.")
                return@itemOnObjectOperate
            }
            if (!inventory.remove("banana")) {
                return@itemOnObjectOperate
            }
            animDelay("take")
            inc("banana_crate_bananas")
            statement("You pack a banana into the crate.")
        }
    }

    private suspend fun Player.inProgress() {
        choice {
            option<Quiz>("What did I have to do again?") {
                npc<Happy>("There's a crate ready to be loaded onto the ship. If you could fill it up with bananas, I'll pay you 30 gold.")
            }
            option<Sad>("No, the crate isn't full yet...") {
                npc<Angry>("Well come back when it is.")
            }
            option<Quiz>("So where are these bananas going to be delivered to?") {
                npc<Neutral>("I sell them to Wydin who runs a grocery store in Port Sarim.")
            }
            option<Shifty>("That customs officer is annoying isn't she?") {
                customsOfficer()
            }
        }
    }

    private suspend fun Player.introduction() {
        choice {
            option<Quiz>("Could you offer me employment on your plantation?") {
                employment()
            }
            option<Shifty>("That customs officer is annoying isn't she?") {
                customsOfficer()
            }
        }
    }

    private suspend fun Player.employment() {
        npc<Happy>("Yes, I can sort something out. There's a crate ready to be loaded onto the ship.")
        npc<Neutral>("You wouldn't believe the demand for bananas from Wydin's shop over in Port Sarim. I think this is the third crate I've shipped him this month.")
        npc<Happy>("If you could go fill it up with bananas, I'll pay you 30 gold.")
        set("banana_plantation_job", true)
    }

    private suspend fun Player.customsOfficer() {
        npc<Neutral>("Well I know her pretty well. She doesn't cause me any trouble any more.")
        npc<Neutral>("She doesn't even search my export crates any more. She knows they only contain bananas.")
        player<Shifty>("Really? How interesting. Whereabouts do you send those to?")
        npc<Neutral>("There is a little shop over in Port Sarim that buys them up by the crate. I believe it is run by a man called Wydin.")
    }

    companion object {
        private const val CRATE_CAPACITY = 10
        private val EXPORT_CRATE = Tile(2943, 3151)
    }
}
