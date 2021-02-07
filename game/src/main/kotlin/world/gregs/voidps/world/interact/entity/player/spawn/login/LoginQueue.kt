package world.gregs.voidps.world.interact.entity.player.spawn.login

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.dsl.module
import world.gregs.voidps.engine.data.PlayerLoader
import world.gregs.voidps.engine.entity.character.IndexAllocator
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerSpawn
import world.gregs.voidps.engine.entity.list.MAX_PLAYERS
import world.gregs.voidps.engine.event.EventBus
import world.gregs.voidps.utility.getIntProperty
import java.lang.Runnable
import java.util.*

/**
 * @author GregHib <greg@gregs.world>
 * @since March 31, 2020
 */
val loginQueueModule = module {
    single {
        LoginQueue(
            get(),
            get(),
            getIntProperty("loginPerTickCap", 1)
        )
    }
}

/**
 * Keeps track of number of players online, prevents duplicate login attempts
 * Loads player save files in the background, queueing once successful
 * Each tick at the correct time, accepts the first [loginPerTickCap] players into the world.
 */
class LoginQueue(
    private val loader: PlayerLoader,
    private val bus: EventBus,
    private val loginPerTickCap: Int,
    private val attempts: MutableSet<String> = mutableSetOf(),
    private val loginQueue: Queue<Pair<Player, Login>> = LinkedList(),
    private val indexer: IndexAllocator = IndexAllocator(MAX_PLAYERS)
) : Runnable {

    private val load = Mutex()
    private val login = Mutex()

    /**
     * Calls login for first loginPerTickCap loaded players
     */

    override fun run() = runBlocking {
        login.withLock {
            var count = 0
            var next = loginQueue.poll()
            while (next != null) {
                login(next.first, next.second)
                if (count++ >= loginPerTickCap) {
                    break
                }
                next = loginQueue.poll()
            }
        }
    }

    /**
     * Accepts client and spawns player in world.
     */
    fun login(player: Player, attempt: Login) {
        bus.emit(PlayerSpawn(player, attempt.name, attempt.session, attempt.data))
        attempt.respond(LoginResponse.Success(player))
    }

    /**
     * Check hasn't already attempted login before attempt to load in background
     */
    fun add(login: Login): Deferred<Unit>? = runBlocking {
        load.withLock {
            if (attempts.contains(login.name)) {
                login.respond(LoginResponse.AccountOnline)
                return@runBlocking null
            } else {
                attempts.add(login.name)
            }
            val index = indexer.obtain()
            scope.async {
                val response = load(index, login)
                if(response !is LoginResponse.Success) {
                    remove(login.name)
                    login.respond(response)
                }
            }
        }
    }

    fun remove(name: String) = runBlocking {
        load.withLock { attempts.remove(name) }
    }

    /**
     * Loads player save in the background.
     */
    suspend fun load(index: Int?, attempt: Login): LoginResponse {
        if(index == null) {
            return LoginResponse.WorldFull
        }
        try {
            val player = loader.loadPlayer(attempt.name)
            player.index = index
            login.withLock { loginQueue.add(player to attempt) }
            logger.info { "Player ${attempt.name} loaded and queued for login." }
            return LoginResponse.Success(player)
        } catch (e: IllegalStateException) {
            logger.error(e) { "Error loading player $attempt" }
            return LoginResponse.CouldNotCompleteLogin
        }
    }

    companion object {
        private val logger = InlineLogger()
        private val scope = CoroutineScope(newFixedThreadPoolContext(2, "LoginQueue"))
    }
}