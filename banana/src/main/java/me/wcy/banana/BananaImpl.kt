package me.wcy.banana

import me.wcy.banana.api.IBanana
import me.wcy.serviceloader.annotation.ServiceImpl

/**
 * Created by wangchenyan.top on 2022/8/10.
 */
@ServiceImpl(IBanana::class)
class BananaImpl : IBanana {
    override fun getName(): String {
        return "Banana"
    }
}