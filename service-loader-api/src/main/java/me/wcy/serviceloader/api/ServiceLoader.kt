package me.wcy.serviceloader.api

import me.wcy.serviceloader.annotation.IServiceLoader
import me.wcy.serviceloader.annotation.ServiceImplEntity
import kotlin.reflect.KClass

/**
 * Created by wangchenyan.top on 2022/8/11.
 */
object ServiceLoader {
    private const val TAG = "ServiceLoader"
    private val serviceMap = mutableMapOf<KClass<*>, MutableList<ServiceImplEntity>>()
    private val singletonImplMap = mutableMapOf<KClass<*>, Any>()

    init {
        init()
    }

    private fun init() {
    }

    @Synchronized
    fun register(loader: IServiceLoader) {
        val map = mutableMapOf<KClass<*>, List<ServiceImplEntity>>()
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
        return serviceMap[service]?.let { list ->
            list.map { loadInternal(it) }
        } ?: emptyList()
    }

    private fun <T : Any> loadInternal(implEntity: ServiceImplEntity): T {
        return if (implEntity.singleton) {
            if (singletonImplMap.containsKey(implEntity.implClass).not()) {
                singletonImplMap[implEntity.implClass] = implEntity.implClass.java.newInstance()
            }
            singletonImplMap[implEntity.implClass] as T
        } else {
            implEntity.implClass.java.newInstance() as T
        }
    }

    private fun combineService(map: Map<KClass<*>, List<ServiceImplEntity>>) {
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