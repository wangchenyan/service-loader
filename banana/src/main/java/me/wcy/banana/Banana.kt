package me.wcy.banana

import me.wcy.apple.api.IApple
import me.wcy.serviceloader.api.ServiceLoader

/**
 * Created by wangchenyan.top on 2022/8/11.
 */
object Banana {

    fun getApples(): List<IApple> {
        return ServiceLoader.loadAll(IApple::class)
    }
}