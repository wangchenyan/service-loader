package me.wcy.serviceloader.app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import me.wcy.banana.Banana

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
    }
}