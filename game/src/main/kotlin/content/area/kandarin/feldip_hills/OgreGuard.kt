package content.area.kandarin.feldip_hills

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.type.Tile

class OgreGuard : Script {

    init {
        npcOperate("Talk-to", "zogre_ogre_guard") { (target) ->
            when (quest("zogre_flesh_eaters")) {
                "unstarted" -> warnAway()
                "investigate" -> openBarricade(target)
                else -> postBarricadeWarning()
            }
        }
    }

    // ===== Progress 0: Generic warning, player hasn't accepted quest =====

    private suspend fun Player.warnAway() {
        npc<Neutral>("Yous needs ta stay away from dis place...yous get da sickies and mebe yous goes to dead if yous da unlucky fing.")
    }

    // ===== Progress 2: Player has accepted quest, ready to break barricade =====

    private suspend fun Player.openBarricade(guard: NPC) {
        npc<Neutral>("Yous needs ta stay away from dis place...yous get da sickies and mebe yous goes to dead if yous da unlucky fing.")
        player<Neutral>("But Grish has asked me to look into this place and find out why all the undead ogres are here.")
        npc<Neutral>("Ok, dat is da big, big scary, danger fing!<br>You's sure you's wants to go in?")
        player<Neutral>("Yes, I'm sure.")
        npc<Neutral>("Ok, I opens da stoppa's for yous creature.")
        breakBarricadeCutscene(guard)
        npc<Neutral>("Ok der' yous goes!")
    }

    // ===== Progress 3+: Past the barricade, just a flavor warning =====

    private suspend fun Player.postBarricadeWarning() {
        npc<Neutral>("Hey yous tryin' not to get da sickies else yous be da sick-un and mebe get to be a dead-un if yous be da unlucky fing.")
        player<Neutral>("Don't worry, I know how to take care of myself.")
    }

    // ===== Helpers - replace with project-specific implementations =====

    private suspend fun Player.breakBarricadeCutscene(guard: NPC) {
        guard.clearWatch()
        guard.face(Tile(2458, 3049, 0))
        delay(2)
        guard.anim("ogre_kick")
        sound("unarmed_kick")
        delay(1)
        set("zogre_flesh_eaters", "barricade")
        set("thzfe_blocking_barricade", true)
        sound("ogre_destroy_barricade")
        delay(2)
    }
}
