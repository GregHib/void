package world.gregs.voidps

import world.gregs.voidps.bot.taskModule
import world.gregs.voidps.engine.client.clientConnectionModule
import world.gregs.voidps.engine.client.update.batch.batchedChunkModule
import world.gregs.voidps.engine.data.fileStorageModule
import world.gregs.voidps.engine.data.playerLoaderModule
import world.gregs.voidps.engine.entity.entityListModule
import world.gregs.voidps.engine.entity.item.drop.dropTableModule
import world.gregs.voidps.engine.event.eventModule
import world.gregs.voidps.engine.map.area.areasModule
import world.gregs.voidps.engine.map.chunk.instanceModule
import world.gregs.voidps.engine.map.collision.collisionModule
import world.gregs.voidps.engine.map.instance.instancePoolModule
import world.gregs.voidps.engine.map.region.xteaModule
import world.gregs.voidps.engine.path.algorithm.lineOfSightModule
import world.gregs.voidps.engine.path.pathFindModule
import world.gregs.voidps.engine.tick.schedulerModule
import world.gregs.voidps.world.interact.entity.player.music.musicModule
import world.gregs.voidps.world.interact.world.stairsModule

fun getGameModules() = listOf(
    eventModule,
    fileStorageModule,
    entityListModule,
    playerLoaderModule,
    xteaModule,
    clientConnectionModule,
    collisionModule,
    pathFindModule,
    schedulerModule,
    batchedChunkModule,
    instanceModule,
    instancePoolModule,
    dropTableModule,
    lineOfSightModule,
    stairsModule,
    musicModule,
    areasModule,
    taskModule
)
