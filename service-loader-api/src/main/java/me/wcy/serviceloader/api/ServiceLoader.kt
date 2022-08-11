package me.wcy.serviceloader.api

import me.wcy.serviceloader.annotation.IServiceLoader
import kotlin.reflect.KClass

/**
 * Created by wangchenyan.top on 2022/8/11.
 */
object ServiceLoader {
    private const val TAG = "ServiceLoader"
    private val serviceMap = mutableMapOf<KClass<*>, MutableList<KClass<*>>>()
    private val implMap = mutableMapOf<KClass<*>, Any>()

    init {
        init()
    }

    private fun init() {
    }

    @Synchronized
    fun register(loader: IServiceLoader) {
        val map = mutableMapOf<KClass<*>, List<KClass<*>>>()
        loader.load(map)
        combineService(map)
    }

    @Synchronized
    fun <T : Any> loadFirst(service: KClass<T>): T {
        val implClass = serviceMap[service]!!.first()
        return loadInternal(implClass) as T
    }

    @Synchronized
    fun <T : Any> loadFirstOrNull(service: KClass<T>): T? {
        return try {
            loadFirst(service)
        } catch (e: Exception) {
            null
        }
    }

    @Synchronized
    fun <T : Any> loadAll(service: KClass<T>): List<T> {
        return serviceMap[service]!!.map { loadInternal(it) } as List<T>
    }

    private fun <T : Any> loadInternal(implClass: KClass<T>): T {
        if (implMap.containsKey(implClass).not()) {
            implMap[implClass] = implClass.java.newInstance()
        }
        return implMap[implClass] as T
    }

    private fun combineService(map: Map<KClass<*>, List<KClass<*>>>) {
        map.forEach { entry ->
            val implList = serviceMap[entry.key] ?: kotlin.run {
                serviceMap[entry.key] = mutableListOf()
                serviceMap[entry.key]!!
            }
            entry.value.forEach {
                if (it !in implList) {
                    implList.add(it)
                }
            }
        }
    }
}