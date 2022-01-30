package world.gregs.voidps

import world.gregs.voidps.bot.taskModule
import world.gregs.voidps.engine.action.schedulerModule
import world.gregs.voidps.engine.client.cacheConfigModule
import world.gregs.voidps.engine.client.cacheDefinitionModule
import world.gregs.voidps.engine.client.cacheModule
import world.gregs.voidps.engine.client.clientConnectionModule
import world.gregs.voidps.engine.data.fileStorageModule
import world.gregs.voidps.engine.data.playerLoaderModule
import world.gregs.voidps.engine.entity.definition.definitionsModule
import world.gregs.voidps.engine.entity.item.drop.dropTableModule
import world.gregs.voidps.engine.entity.list.entityListModule
import world.gregs.voidps.engine.entity.obj.customObjectModule
import world.gregs.voidps.engine.entity.obj.objectFactoryModule
import world.gregs.voidps.engine.event.eventModule
import world.gregs.voidps.engine.map.area.areasModule
import world.gregs.voidps.engine.map.chunk.batchedChunkModule
import world.gregs.voidps.engine.map.chunk.instanceModule
import world.gregs.voidps.engine.map.collision.collisionModule
import world.gregs.voidps.engine.map.instance.instancePoolModule
import world.gregs.voidps.engine.map.nav.navModule
import world.gregs.voidps.engine.map.region.regionModule
import world.gregs.voidps.engine.map.region.xteaModule
import world.gregs.voidps.engine.path.algorithm.lineOfSightModule
import world.gregs.voidps.engine.path.pathFindModule
import world.gregs.voidps.script.scriptModule
import world.gregs.voidps.world.interact.entity.player.music.musicModule
import world.gregs.voidps.world.interact.world.stairsModule

fun getGameModules() = listOf(
    eventModule,
    cacheModule,
    fileStorageModule,
    entityListModule,
    scriptModule,
    playerLoaderModule,
    xteaModule,
    clientConnectionModule,
    regionModule,
    collisionModule,
    cacheDefinitionModule,
    cacheConfigModule,
    pathFindModule,
    schedulerModule,
    batchedChunkModule,
    instanceModule,
    instancePoolModule,
    definitionsModule,
    dropTableModule,
    objectFactoryModule,
    lineOfSightModule,
    navModule,
    customObjectModule,
    stairsModule,
    musicModule,
    areasModule,
    taskModule
)