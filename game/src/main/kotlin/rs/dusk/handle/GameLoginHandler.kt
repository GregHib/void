package rs.dusk.handle

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import rs.dusk.engine.client.Sessions
import rs.dusk.engine.entity.character.player.GameLoginInfo
import rs.dusk.engine.entity.character.player.PlayerRegistered
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.map.region.RegionLogin
import rs.dusk.engine.task.TaskExecutor
import rs.dusk.engine.task.sync
import rs.dusk.network.codec.Handler
import rs.dusk.network.codec.game.GameCodec
import rs.dusk.network.codec.login.LoginCodec
import rs.dusk.network.codec.login.encode.GameLoginDetailsEncoder
import rs.dusk.network.codec.login.encode.LoginResponseEncoder
import rs.dusk.network.codec.setCipherIn
import rs.dusk.network.codec.setCipherOut
import rs.dusk.network.codec.setCodec
import rs.dusk.network.crypto.IsaacKeyPair
import rs.dusk.utility.inject
import rs.dusk.world.interact.entity.player.spawn.login.Login
import rs.dusk.world.interact.entity.player.spawn.login.LoginResponse

/**
 * @author Greg Hibberd <greg@greghibberd.com>
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
        println("Game login")
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
//                    player.start()
//                    bus.emit(Registered(player))
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