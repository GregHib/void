package rs.dusk.utility.func

import io.github.classgraph.ClassGraph

class FileFunction {

    companion object {

        inline fun <reified T> getChildClassesOf(): MutableList<T> {
            val kClass = T::class
            val name = kClass.qualifiedName
            val result = ClassGraph().enableClassInfo().blacklistClasses(name).scan()
            val classes = mutableListOf<T>()
            result.use { result ->
                val subclasses = result.getSubclasses(name)
                subclasses.forEach {
                    val clazz = result.loadClass(it.name, true).newInstance() as T
                    classes.add(clazz)
                }
            }
            return classes
        }

    }
}