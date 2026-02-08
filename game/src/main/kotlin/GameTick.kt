import com.github.michaelbull.logging.InlineLogger
import content.bot.BotManager
import content.social.trade.exchange.GrandExchange
import world.gregs.voidps.engine.client.instruction.InstructionHandlers
import world.gregs.voidps.engine.client.instruction.InstructionTask
import world.gregs.voidps.engine.client.update.CharacterTask
import world.gregs.voidps.engine.client.update.CharacterUpdateTask
import world.gregs.voidps.engine.client.update.NPCTask
import world.gregs.voidps.engine.client.update.PlayerTask
import world.gregs.voidps.engine.client.update.batch.ZoneBatchUpdates
import world.gregs.voidps.engine.client.update.iterator.ParallelIterator
import world.gregs.voidps.engine.client.update.iterator.SequentialIterator
import world.gregs.voidps.engine.client.update.iterator.TaskIterator
import world.gregs.voidps.engine.client.update.npc.NPCResetTask
import world.gregs.voidps.engine.client.update.npc.NPCUpdateTask
import world.gregs.voidps.engine.client.update.player.PlayerResetTask
import world.gregs.voidps.engine.client.update.player.PlayerUpdateTask
import world.gregs.voidps.engine.data.SaveQueue
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.npc.hunt.Hunting
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItemTracking
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.zone.DynamicZones
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.network.client.ConnectionQueue
import world.gregs.voidps.network.login.protocol.npcVisualEncoders
import java.io.File
import java.util.concurrent.TimeUnit

fun getTickStages(
    floorItems: FloorItemTracking = get(),
    queue: ConnectionQueue = get(),
    accountSave: SaveQueue = get(),
    hunting: Hunting = get(),
    grandExchange: GrandExchange = get(),
    sequential: Boolean = CharacterTask.DEBUG,
    handlers: InstructionHandlers = get(),
    dynamicZones: DynamicZones = get(),
    botManager: BotManager = get(),
): List<Runnable> {
    val sequentialNpc: TaskIterator<NPC> = SequentialIterator()
    val sequentialPlayer: TaskIterator<Player> = SequentialIterator()
    val iterator: TaskIterator<Player> = if (sequential) SequentialIterator() else ParallelIterator()
    return listOf(
        PlayerResetTask(sequentialPlayer),
        NPCResetTask(sequentialNpc),
        hunting,
        grandExchange,
        // Connections/Tick Input
        queue,
        NPCs,
        FloorItems,
        // Tick
        InstructionTask(handlers),
        World,
        NPCTask(sequentialNpc),
        PlayerTask(sequentialPlayer),
        floorItems,
        GameObjects.timers,
        // Update
        dynamicZones,
        ZoneBatchUpdates,
        CharacterUpdateTask(
            iterator,
            PlayerUpdateTask(),
            NPCUpdateTask(npcVisualEncoders()),
        ),
        botManager,
        accountSave,
        SaveLogs(),
    )
}

private class SaveLogs : Runnable {
    private val directory = File(Settings["storage.players.logs"])
    private var ticks = TimeUnit.SECONDS.toTicks(Settings["storage.players.logs.seconds", 10])
    private val logger = InlineLogger()

    init {
        directory.mkdirs()
    }

    override fun run() {
        if (ticks-- < 0) {
            if (AuditLog.logs.isEmpty) {
                return
            }
            val count = AuditLog.logs.size
            val start = System.currentTimeMillis()
            AuditLog.save(directory)
            logger.info { "Saved $count logs to disk in ${System.currentTimeMillis() - start}ms." }
            ticks = TimeUnit.SECONDS.toTicks(Settings["storage.players.logs.seconds", 10])
        }
    }
}
