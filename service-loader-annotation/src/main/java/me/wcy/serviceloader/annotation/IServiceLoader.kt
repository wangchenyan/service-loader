package me.wcy.serviceloader.annotation

/**
 * Created by wangchenyan.top on 2022/8/10.
 */
interface IServiceLoader {
    fun load(map: MutableMap<String, List<ServiceImplInfo>>)
}