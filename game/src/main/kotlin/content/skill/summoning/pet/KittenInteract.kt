package content.skill.summoning.pet

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Follow
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.queue.softQueue

private const val SCAN_RADIUS = 10
private const val CHASE_RADIUS = 8

class KittenInteract(definitions: PetDefinitions) : Script {

    init {
        val registered = mutableSetOf<String>()
        for (def in definitions.all) {
            if (!def.isCatLike) continue
            if (!registered.add(def.babyNpc)) continue
            npcOperate("Interact-with", def.babyNpc) { interact ->
                if (pet?.index != interact.target.index) {
                    message("This isn't your pet.")
                    return@npcOperate
                }
                openMenu(interact.target)
            }
        }
    }

    private suspend fun Player.openMenu(kitten: NPC) {
        choice("Interact with Kitten") {
            option("Stroke") { stroke(kitten) }
            option("Chase vermin") { chaseVermin(kitten) }
            option("Shoo away") { shooConfirm(kitten) }
        }
    }

    private suspend fun Player.stroke(kitten: NPC) {
        steps.clear()
        kitten.steps.clear()
        kitten.mode = EmptyMode
        kitten.watch(this)
        face(kitten)
        anim("pet_stroke_player")
        kitten.anim("pet_stroke_kitten")
        kitten.say("Purr...purr...")
        player<Happy>("That cat sure loves to be stroked.")
        if (pet?.index == kitten.index) {
            kitten.say("Miaow!")
            kitten.mode = Follow(kitten, this)
        }
    }

    private fun Player.chaseVermin(kitten: NPC) {
        say("Go on puss...kill that rat!")
        val nearbyRat = NPCs.at(tile.regionLevel)
            .filter { it.id.contains("rat") }
            .filter { it.tile.distanceTo(tile) <= SCAN_RADIUS }
            .minByOrNull { it.tile.distanceTo(kitten.tile) }
        if (nearbyRat == null || nearbyRat.tile.distanceTo(kitten.tile) > CHASE_RADIUS) {
            message("Your cat cannot get to its prey.")
            return
        }
        kitten.say("Meeeoooooowwww!")
        nearbyRat.say("Eeek!")
        kitten.mode = EmptyMode
        kitten.walkTo(nearbyRat.tile)
        softQueue("kitten_chase", 5) {
            val current = player.pet
            if (current != null && current.index == kitten.index) {
                current.mode = Follow(current, player)
            }
            player.message("The rat manages to get away!")
        }
    }

    private suspend fun Player.shooConfirm(kitten: NPC) {
        choice("Are you sure?") {
            option<Quiz>("Yes I am.") {
                if (pet?.index != kitten.index) return@option
                say("Shoo cat!")
                kitten.say("Miaow!")
                dismissPet()
                message("The cat has run away.")
            }
            option<Sad>("No I'm not.")
        }
    }
}
