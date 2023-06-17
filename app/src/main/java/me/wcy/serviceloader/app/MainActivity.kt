package me.wcy.serviceloader.app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import me.wcy.apple.api.INonSingleton
import me.wcy.banana.Banana
import me.wcy.serviceloader.api.ServiceLoader

class MainActivity : AppCompatActivity() {
    private val tv: TextView by lazy { findViewById(R.id.tv) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tv.setText("All Apples: ")
        Banana.getApples().forEach {
            tv.append(it.getName())
            tv.append(".")
        }

        tv.append("\n\n")
        tv.append("Three NonSingleton: ")
        for (i in 0..2) {
            val entity = ServiceLoader.loadFirstOrNull(INonSingleton::class)
            tv.append(entity.hashCode().toString())
            tv.append(".")
        }
    }
}