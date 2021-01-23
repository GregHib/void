package world.gregs.voidps.handle

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.engine.client.Sessions
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.player.GameLoginInfo
import world.gregs.voidps.engine.entity.character.player.PlayerRegistered
import world.gregs.voidps.engine.event.EventBus
import world.gregs.voidps.engine.map.region.RegionLogin
import world.gregs.voidps.engine.task.TaskExecutor
import world.gregs.voidps.engine.task.sync
import world.gregs.voidps.network.codec.Handler
import world.gregs.voidps.network.codec.game.GameCodec
import world.gregs.voidps.network.codec.login.LoginCodec
import world.gregs.voidps.network.codec.login.encode.GameLoginDetailsEncoder
import world.gregs.voidps.network.codec.login.encode.LoginResponseEncoder
import world.gregs.voidps.network.codec.setCipherIn
import world.gregs.voidps.network.codec.setCipherOut
import world.gregs.voidps.network.codec.setCodec
import world.gregs.voidps.network.crypto.IsaacKeyPair
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.entity.player.spawn.login.Login
import world.gregs.voidps.world.interact.entity.player.spawn.login.LoginResponse

/**
 * @author GregHib <greg@gregs.world>
 * @since April 18, 2020
 */
class GameLoginHandler : Handler() {

    val logger = InlineLogger()
    val sessions: Sessions by inject()
    val bus: EventBus by inject()
    val executor: TaskExecutor by inject()
    private val login: LoginCodec by inject()
    private val game: GameCodec by inject()
    private val responseEncoder = LoginResponseEncoder()
    private val loginEncoder = GameLoginDetailsEncoder()

    override fun loginGame(
        context: ChannelHandlerContext,
        username: String,
        password: String,
        isaacKeys: IntArray,
        mode: Int,
        width: Int,
        height: Int,
        antialias: Int,
        settings: String,
        affiliate: Int,
        session: Int,
        os: Int,
        is64Bit: Int,
        versionType: Int,
        vendorType: Int,
        javaRelease: Int,
        javaVersion: Int,
        javaUpdate: Int,
        isUnsigned: Int,
        heapSize: Int,
        processorCount: Int,
        totalMemory: Int
    ) {
        val channel = context.channel()
        val keyPair = IsaacKeyPair(isaacKeys)

        channel.setCodec(login)

        val callback: (LoginResponse) -> Unit = { response ->
            if (response is LoginResponse.Success) {
                val player = response.player
                loginEncoder.encode(channel, 2, player.index, username)

                executor.sync {
                    channel.setCodec(game)
                    channel.setCipherIn(keyPair.inCipher)
                    channel.setCipherOut(keyPair.outCipher)
                    bus.emit(RegionLogin(player))
                    bus.emit(PlayerRegistered(player))
                    player.start()
                    bus.emit(Registered(player))
                }
            } else {
                responseEncoder.encode(channel, response.code)
            }
        }

        executor.sync {
            bus.emit(
                Login(
                    username,
                    channel,
                    callback,
                    GameLoginInfo(username, password, isaacKeys, mode, width, height, antialias, settings, affiliate, session, os, is64Bit, versionType, vendorType, javaRelease, javaVersion, javaUpdate, isUnsigned, heapSize, processorCount, totalMemory)
                )
            )
        }
    }
}