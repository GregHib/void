package org.redrune.engine.script

/**
 * Loads script by matching test class package and name
 */
abstract class TestScriptLoader(private val suffix: String) {

    fun loadScriptClass(): Class<*> {
        val clazz = this::class.java
        val scriptPackage = "${clazz.packageName}.${clazz.simpleName.substring(0, clazz.simpleName.length - suffix.length)}"
        return Class.forName(scriptPackage)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> loadScript(): T {
        val clazz = this::class.java
        val scriptPackage = "${clazz.packageName}.${clazz.simpleName.substring(0, clazz.simpleName.length - suffix.length)}"
        return Class.forName(scriptPackage).constructors.first().newInstance(emptyArray<String>()) as T
    }

}