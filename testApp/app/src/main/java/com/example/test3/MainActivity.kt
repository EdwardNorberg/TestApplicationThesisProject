package com.example.test3

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btn = findViewById<Button>(R.id.getTimeBtn)
        btn.setOnClickListener {
            val text = findViewById<TextView>(R.id.textView)
            text.text = "Time is"
            var result = ""
            GlobalScope.launch {
                result = getTime()
            }
            text.text = result

        }
    }
}

suspend fun getHttp(): String {
    val client = HttpClient(CIO)
    val response: HttpResponse = client.get("https://ktor.io/")
    val result = response.request.toString()
    client.close()
    return result
}

fun getTime(): String {
    val result = URL("https://httpbin.org/ip").readText()
    println(result)
    return result
}