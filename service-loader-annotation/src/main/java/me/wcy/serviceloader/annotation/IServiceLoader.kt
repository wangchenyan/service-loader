package me.wcy.serviceloader.annotation

import kotlin.reflect.KClass

/**
 * Created by wangchenyan.top on 2022/8/10.
 */
interface IServiceLoader {
    fun load(map: MutableMap<KClass<*>, List<ServiceImplEntity>>)
}