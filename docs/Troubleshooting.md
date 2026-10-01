## Port used by another process

```
Exception in thread "main" java.net.BindException: Address already in use: bind
        at java.base/sun.nio.ch.Net.bind0(Native Method)
        at java.base/sun.nio.ch.Net.bind(Net.java:555)
        at java.base/sun.nio.ch.ServerSocketChannelImpl.netBind(ServerSocketChannelImpl.java:344)
        at java.base/sun.nio.ch.ServerSocketChannelImpl.bind(ServerSocketChannelImpl.java:301)
        at io.ktor.network.sockets.ConnectUtilsJvmKt.bind(ConnectUtilsJvm.kt:35)
        at io.ktor.network.sockets.TcpSocketBuilder.bind(TcpSocketBuilder.kt:45)
        at io.ktor.network.sockets.TcpSocketBuilder.bind(TcpSocketBuilder.kt:29)
        at io.ktor.network.sockets.TcpSocketBuilder.bind$default(TcpSocketBuilder.kt:25)
        at world.gregs.voidps.network.GameServer$start$1.invokeSuspend(GameServer.kt:47)
        at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33)
        at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:108)
        at kotlinx.coroutines.EventLoopImplBase.processNextEvent(EventLoop.common.kt:280)
        at kotlinx.coroutines.BlockingCoroutine.joinBlocking(Builders.kt:85)
        at kotlinx.coroutines.BuildersKt__BuildersKt.runBlocking(Builders.kt:59)
        at kotlinx.coroutines.BuildersKt.runBlocking(Unknown Source)
        at kotlinx.coroutines.BuildersKt__BuildersKt.runBlocking$default(Builders.kt:38)
        at kotlinx.coroutines.BuildersKt.runBlocking$default(Unknown Source)
        at world.gregs.voidps.network.GameServer.start(GameServer.kt:30)
        at world.gregs.voidps.Main.main(Main.kt:65)
```

Another application is currently using that port. Possibly another copy of the server which hasn't closed correctly. Check Task Manager for unexpected Java Applications running in the background.

## Missing cache files

```
Exception in thread "main" java.io.FileNotFoundException: Main file not found at '\void\data\cache\main_file_cache.dat2'.
        at world.gregs.voidps.cache.CacheLoader.load(CacheLoader.kt:23)
        at world.gregs.voidps.cache.CacheLoader.load$default(CacheLoader.kt:20)
        at world.gregs.voidps.cache.CacheLoader.load(CacheLoader.kt:17)
        at world.gregs.voidps.cache.CacheLoader.load$default(CacheLoader.kt:12)
        at world.gregs.voidps.cache.Cache$Companion.load(Cache.kt:47)
        at world.gregs.voidps.Main$main$cache$1.invoke(Main.kt:48)
        at world.gregs.voidps.Main$main$cache$1.invoke(Main.kt:48)
        at world.gregs.voidps.engine.TimedLoaderKt.timed(TimedLoader.kt:10)
        at world.gregs.voidps.Main.main(Main.kt:48)
```

Cache files couldn't be found. Make sure you correctly extracted the cache files into `/data/cache/`

## Missing config files

```
Caused by: java.io.FileNotFoundException: .\data\definitions\animations.toml (The system cannot find the file specified)
        at java.base/java.io.FileInputStream.open0(Native Method)
        at java.base/java.io.FileInputStream.open(FileInputStream.java:219)
        at java.base/java.io.FileInputStream.<init>(FileInputStream.java:158)
        at world.gregs.yaml.Yaml.load(Yaml.kt:31)
        at world.gregs.voidps.engine.data.definition.DefinitionsDecoder.decode(DefinitionsDecoder.kt:53)
        at world.gregs.voidps.engine.data.definition.AnimationDefinitions$load$1.invoke(AnimationDefinitions.kt:19)
        at world.gregs.voidps.engine.data.definition.AnimationDefinitions$load$1.invoke(AnimationDefinitions.kt:18)
        at world.gregs.voidps.engine.TimedLoaderKt.timedLoad(TimedLoader.kt:18)
        at world.gregs.voidps.engine.data.definition.AnimationDefinitions.load(AnimationDefinitions.kt:18)
        at world.gregs.voidps.engine.data.definition.AnimationDefinitions.load$default(AnimationDefinitions.kt:17)
        at world.gregs.voidps.Main$cache$1$6.invoke(Main.kt:101)
        at world.gregs.voidps.Main$cache$1$6.invoke(Main.kt:101)
        at org.koin.core.instance.InstanceFactory.create(InstanceFactory.kt:50)
        ... 13 more
```

A config file could not be found. Double check that the file name and the relative directory inside `game.properties` is correct.

## Startup error

```
Exception in thread "main" org.koin.core.error.InstanceCreationException: Could not create instance for '[Singleton:'world.gregs.voidps.engine.data.definition.Definition']'
        at org.koin.core.instance.InstanceFactory.create(InstanceFactory.kt:57)
        at org.koin.core.instance.SingleInstanceFactory.create(SingleInstanceFactory.kt:46)
        at org.koin.core.instance.SingleInstanceFactory$get$1.invoke(SingleInstanceFactory.kt:55)
        at org.koin.core.instance.SingleInstanceFactory$get$1.invoke(SingleInstanceFactory.kt:53)
        at org.koin.mp.KoinPlatformTools.synchronized(KoinPlatformTools.kt:36)
        at org.koin.core.instance.SingleInstanceFactory.get(SingleInstanceFactory.kt:53)
        at org.koin.core.registry.InstanceRegistry.createEagerInstances(InstanceRegistry.kt:91)
        at org.koin.core.registry.InstanceRegistry.createAllEagerInstances$koin_core(InstanceRegistry.kt:62)
        at org.koin.core.Koin.createEagerInstances(Koin.kt:330)
        at org.koin.core.KoinApplication.createEagerInstances(KoinApplication.kt:74)
        at org.koin.core.context.GlobalContext.startKoin(GlobalContext.kt:65)
        at org.koin.core.context.DefaultContextExtKt.startKoin(DefaultContextExt.kt:40)
        at world.gregs.voidps.Main.preload(Main.kt:82)
        at world.gregs.voidps.Main.main(Main.kt:50)
```

This is a generic error in the preload step in setup. Scroll down further for the real error.


## Running with Gradle Wrapper

```bash
> Kotlin could not find the required JDK tools in the Java installation. Make sure Kotlin compilation is running on a JDK, not JRE.
```

Open the `gradle.properties` file, remove the hash from the first line and replace the directory with the location of your JDK installation.

```properties
org.gradle.java.home=C:/Users/Greg/.jdks/openjdk-19.0.1/
```

## Opening the project in IntelliJ

### Incorrect JDK

`File | Project Structure | Project Settings | Project`

`SDK:` and `Language level:` are set to a valid jdk (19+)

### Broken Gradle

`View | Tool Windows | Gradle` (also the elephant icon on the right-hand bar)

Press the `Reload All Gradle Project` button.


> [!IMPORTANT]
> If you have tried all of the above steps and still are having problems please create a [New Issue](https://github.com/GregHib/void/issues/new) with the problem described, what you have tried, and any errors or logs you have and I'll try to help you out.