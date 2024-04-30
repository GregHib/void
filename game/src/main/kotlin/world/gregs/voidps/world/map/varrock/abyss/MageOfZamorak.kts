package world.gregs.voidps.world.map.varrock.abyss

import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.*
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.engine.queue.ActionPriority
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.entity.sound.playSound

val areas: AreaDefinitions by inject()

val abyss = areas["abyss_multi_area"]
val abyssCenter = areas["abyss_center"]

playerSpawn { player ->
    player.sendVariable("enter_the_abyss")
}

npcOperate("Talk-to", "mage_of_zamorak") {

}

npcOperate("Teleport", "mage_of_zamorak_wilderness") {
    if (player.queue.contains(ActionPriority.Normal)) {
        return@npcOperate
    }
    player.closeInterfaces()
    player.queue("teleport", onCancel = null) {
        target.face(player)
        target.setGraphic("tele_other")
        target.setAnimation("tele_other")
        player.playSound("tele_other_cast")
        target.forceChat = "Veniens! Sallakar! Rinnesset!"
        pause(2)
        player.setAnimation("lunar_teleport")
        player.setGraphic("tele_other_receive")
        player.playSound("teleport_all")
        pause(2)
        player["abyss_obstacles"] = random.nextInt(0, 12)
        var tile = abyss.random(player)
        var count = 0
        while ((tile == null || tile in abyssCenter) && count++ < 100) {
            tile = abyss.random(player)
        }
        player.tele(tile!!)
        player.clearAnimation()
    }
}