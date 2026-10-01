Errors can be pretty daunting, here's how you break down what an error on void looks like, which parts are useful, and how to figure out what it means.

# Koin errors

Koin is what void uses to startup so if something fails first you'll see a koin error about failing to create an instance
```kotlin
ERROR [[Koin]] * Instance creation error : could not create instance for '[Singleton: 'world.gregs.voidps.engine.entity.item.drop.DropTables']'
```

This error tells us that it was trying to startup and load DropTables but failed.

Koin errors won't always be useful, the real cause is normally found below.


# Log lines

Errors normally include a list of points in the code the server got to before the error occurred. This is called a stack trace.
They are read from the bottom up.

```kotlin
world.gregs.voidps.engine.entity.item.drop.DropTables.load$lambda$0(DropTables.kt:26)
world.gregs.voidps.engine.TimedLoaderKt.timedLoad(TimedLoader.kt:18)
world.gregs.voidps.engine.entity.item.drop.DropTables.load(DropTables.kt:23)
```
This tells us the error occurred in:
- `DropTables` line 23
- `TimedLoader` line 18
- `DropTables` line 26

Unless you're debugging code you don't need to read through this, it might give you some ideas as to what the error is though.

# Exceptions

Now we get to real errors, these generally look like this and provide a the end the real cause of the issue.

```kotlin
Caused by: java.lang.IllegalArgumentException: Unable to find item with id 'fellstalk_seed'.
```

In this case we can see there was an item "fellstalk_seed" which couldn't be found.

# Putting it all together

We now know:
- There was an error on startup
- It failed to load DropTables
- It couldn't find an item with id 'fellstalk_seed'.

If you did a search for "fellstalk_seed" you'd see a usage in a `*.drops.toml` but not in an `*.items.toml`.

Searching on the rs3 wiki you'd find [fellstalk seed](https://runescape.wiki/w/Fellstalk_seed) was added on 6th of September 2011.

Void's 634 revision is only up to January 2011, so the cause of this error is fellstalk seed doesn't exist in this revision.


# Your turn

Now here's a full example, don't worry about how many lines there are, pick out the same pieces of information as shown before and piece together what is going wrong.

```kotlin
ERROR [[Koin]] * Instance creation error : could not create instance for '[Singleton: 'world.gregs.voidps.engine.data.definition.ItemDefinitions']': java.lang.IllegalArgumentException: Duplicate item id found 'dragon_sq_shield_or' at .\data\minigame\treasure_trail\treasure_trail.items.toml.
	world.gregs.voidps.engine.data.definition.ItemDefinitions.load$lambda$0$0(ItemDefinitions.kt:123)
	world.gregs.config.Config.fileReader(Config.kt:11)
	world.gregs.voidps.engine.data.definition.ItemDefinitions.load$lambda$0(ItemDefinitions.kt:56)
	world.gregs.voidps.engine.TimedLoaderKt.timedLoad(TimedLoader.kt:18)
	world.gregs.voidps.engine.data.definition.ItemDefinitions.load(ItemDefinitions.kt:51)
	Main.cache$lambda$0$4(Main.kt:125)
	org.koin.core.instance.InstanceFactory.create(InstanceFactory.kt:49)
	org.koin.core.instance.SingleInstanceFactory.create(SingleInstanceFactory.kt:46)
	org.koin.core.instance.SingleInstanceFactory.get$lambda$0(SingleInstanceFactory.kt:55)
	org.koin.mp.KoinPlatformTools.synchronized(KoinPlatformTools.kt:36)
	org.koin.core.instance.SingleInstanceFactory.get(SingleInstanceFactory.kt:53)
	org.koin.core.registry.InstanceRegistry.resolveInstance$koin_core(InstanceRegistry.kt:132)
	org.koin.core.resolution.CoreResolver.resolveFromRegistry(CoreResolver.kt:87)
	org.koin.core.resolution.CoreResolver.resolveFromContextOrNull(CoreResolver.kt:74)
	org.koin.core.resolution.CoreResolver.resolveFromContextOrNull$default(CoreResolver.kt:72)
	org.koin.core.resolution.CoreResolver.resolveFromContext(CoreResolver.kt:69)
	org.koin.core.scope.Scope.resolveFromContext(Scope.kt:321)
	org.koin.core.scope.Scope.stackParametersCall(Scope.kt:284)
	org.koin.core.scope.Scope.resolveInstance(Scope.kt:270)
	org.koin.core.scope.Scope.resolve(Scope.kt:243)
	org.koin.core.scope.Scope.get(Scope.kt:225)
	Main.cache$lambda$0$19(Main.kt:926)
	org.koin.core.instance.InstanceFactory.create(InstanceFactory.kt:49)
	org.koin.core.instance.SingleInstanceFactory.create(SingleInstanceFactory.kt:46)
	org.koin.core.instance.SingleInstanceFactory.get$lambda$0(SingleInstanceFactory.kt:55)
	org.koin.mp.KoinPlatformTools.synchronized(KoinPlatformTools.kt:36)
	org.koin.core.instance.SingleInstanceFactory.get(SingleInstanceFactory.kt:53)
	org.koin.core.registry.InstanceRegistry.createEagerInstances(InstanceRegistry.kt:102)
	org.koin.core.registry.InstanceRegistry.createAllEagerInstances$koin_core(InstanceRegistry.kt:68)
	org.koin.core.Koin.createEagerInstances(Koin.kt:340)
	org.koin.core.KoinApplication.createEagerInstances(KoinApplication.kt:77)
	org.koin.core.context.GlobalContext.startKoin(GlobalContext.kt:65)
	org.koin.core.context.DefaultContextExtKt.startKoin(DefaultContextExt.kt:41)
	Main.preload(Main.kt:95)
	Main.main(Main.kt:57)
ERROR [[Koin]] * Instance creation error : could not create instance for '[Singleton: 'world.gregs.voidps.engine.entity.item.drop.DropTables']': org.koin.core.error.InstanceCreationException: Could not create instance for '[Singleton: 'world.gregs.voidps.engine.data.definition.ItemDefinitions']'
	org.koin.core.instance.InstanceFactory.create(InstanceFactory.kt:56)
	org.koin.core.instance.SingleInstanceFactory.create(SingleInstanceFactory.kt:46)
	org.koin.core.instance.SingleInstanceFactory.get$lambda$0(SingleInstanceFactory.kt:55)
	org.koin.mp.KoinPlatformTools.synchronized(KoinPlatformTools.kt:36)
	org.koin.core.instance.SingleInstanceFactory.get(SingleInstanceFactory.kt:53)
	org.koin.core.registry.InstanceRegistry.resolveInstance$koin_core(InstanceRegistry.kt:132)
	org.koin.core.resolution.CoreResolver.resolveFromRegistry(CoreResolver.kt:87)
	org.koin.core.resolution.CoreResolver.resolveFromContextOrNull(CoreResolver.kt:74)
	org.koin.core.resolution.CoreResolver.resolveFromContextOrNull$default(CoreResolver.kt:72)
	org.koin.core.resolution.CoreResolver.resolveFromContext(CoreResolver.kt:69)
	org.koin.core.scope.Scope.resolveFromContext(Scope.kt:321)
	org.koin.core.scope.Scope.stackParametersCall(Scope.kt:284)
	org.koin.core.scope.Scope.resolveInstance(Scope.kt:270)
	org.koin.core.scope.Scope.resolve(Scope.kt:243)
	org.koin.core.scope.Scope.get(Scope.kt:225)
	Main.cache$lambda$0$19(Main.kt:926)
	org.koin.core.instance.InstanceFactory.create(InstanceFactory.kt:49)
	org.koin.core.instance.SingleInstanceFactory.create(SingleInstanceFactory.kt:46)
	org.koin.core.instance.SingleInstanceFactory.get$lambda$0(SingleInstanceFactory.kt:55)
	org.koin.mp.KoinPlatformTools.synchronized(KoinPlatformTools.kt:36)
	org.koin.core.instance.SingleInstanceFactory.get(SingleInstanceFactory.kt:53)
	org.koin.core.registry.InstanceRegistry.createEagerInstances(InstanceRegistry.kt:102)
	org.koin.core.registry.InstanceRegistry.createAllEagerInstances$koin_core(InstanceRegistry.kt:68)
	org.koin.core.Koin.createEagerInstances(Koin.kt:340)
	org.koin.core.KoinApplication.createEagerInstances(KoinApplication.kt:77)
	org.koin.core.context.GlobalContext.startKoin(GlobalContext.kt:65)
	org.koin.core.context.DefaultContextExtKt.startKoin(DefaultContextExt.kt:41)
	Main.preload(Main.kt:95)
	Main.main(Main.kt:57)
ERROR [Main] Error loading files.
org.koin.core.error.InstanceCreationException: Could not create instance for '[Singleton: 'world.gregs.voidps.engine.entity.item.drop.DropTables']'
	at org.koin.core.instance.InstanceFactory.create(InstanceFactory.kt:56)
	at org.koin.core.instance.SingleInstanceFactory.create(SingleInstanceFactory.kt:46)
	at org.koin.core.instance.SingleInstanceFactory.get$lambda$0(SingleInstanceFactory.kt:55)
	at org.koin.mp.KoinPlatformTools.synchronized(KoinPlatformTools.kt:36)
	at org.koin.core.instance.SingleInstanceFactory.get(SingleInstanceFactory.kt:53)
	at org.koin.core.registry.InstanceRegistry.createEagerInstances(InstanceRegistry.kt:102)
	at org.koin.core.registry.InstanceRegistry.createAllEagerInstances$koin_core(InstanceRegistry.kt:68)
	at org.koin.core.Koin.createEagerInstances(Koin.kt:340)
	at org.koin.core.KoinApplication.createEagerInstances(KoinApplication.kt:77)
	at org.koin.core.context.GlobalContext.startKoin(GlobalContext.kt:65)
	at org.koin.core.context.DefaultContextExtKt.startKoin(DefaultContextExt.kt:41)
	at Main.preload(Main.kt:95)
	at Main.main(Main.kt:57)
Caused by: org.koin.core.error.InstanceCreationException: Could not create instance for '[Singleton: 'world.gregs.voidps.engine.data.definition.ItemDefinitions']'
	at org.koin.core.instance.InstanceFactory.create(InstanceFactory.kt:56)
	at org.koin.core.instance.SingleInstanceFactory.create(SingleInstanceFactory.kt:46)
	at org.koin.core.instance.SingleInstanceFactory.get$lambda$0(SingleInstanceFactory.kt:55)
	at org.koin.mp.KoinPlatformTools.synchronized(KoinPlatformTools.kt:36)
	at org.koin.core.instance.SingleInstanceFactory.get(SingleInstanceFactory.kt:53)
	at org.koin.core.registry.InstanceRegistry.resolveInstance$koin_core(InstanceRegistry.kt:132)
	at org.koin.core.resolution.CoreResolver.resolveFromRegistry(CoreResolver.kt:87)
	at org.koin.core.resolution.CoreResolver.resolveFromContextOrNull(CoreResolver.kt:74)
	at org.koin.core.resolution.CoreResolver.resolveFromContextOrNull$default(CoreResolver.kt:72)
	at org.koin.core.resolution.CoreResolver.resolveFromContext(CoreResolver.kt:69)
	at org.koin.core.scope.Scope.resolveFromContext(Scope.kt:321)
	at org.koin.core.scope.Scope.stackParametersCall(Scope.kt:284)
	at org.koin.core.scope.Scope.resolveInstance(Scope.kt:270)
	at org.koin.core.scope.Scope.resolve(Scope.kt:243)
	at org.koin.core.scope.Scope.get(Scope.kt:225)
	at Main.cache$lambda$0$19(Main.kt:926)
	at org.koin.core.instance.InstanceFactory.create(InstanceFactory.kt:49)
	... 12 common frames omitted
Caused by: java.lang.IllegalArgumentException: Duplicate item id found 'dragon_sq_shield_or' at .\data\minigame\treasure_trail\treasure_trail.items.toml.
	at world.gregs.voidps.engine.data.definition.ItemDefinitions.load$lambda$0$0(ItemDefinitions.kt:123)
	at world.gregs.config.Config.fileReader(Config.kt:11)
	at world.gregs.voidps.engine.data.definition.ItemDefinitions.load$lambda$0(ItemDefinitions.kt:56)
	at world.gregs.voidps.engine.TimedLoaderKt.timedLoad(TimedLoader.kt:18)
	at world.gregs.voidps.engine.data.definition.ItemDefinitions.load(ItemDefinitions.kt:51)
	at Main.cache$lambda$0$4(Main.kt:125)
	at org.koin.core.instance.InstanceFactory.create(InstanceFactory.kt:49)
	... 28 common frames omitted

```

## Quiz:
- When did the error occur?
- What system failed?
- Why was there an error?
- Where is the issue located?
- How can you fix it?

<details>
<summary>Answers</summary>
<ul>
<li>Server Startup</li>
<li>ItemDefinitions</li>
<li>Duplicate item string id</li>
<li>treasure_trail.items.toml</li>
<li>Rename the duplicate</li>
</ul>
</details>


