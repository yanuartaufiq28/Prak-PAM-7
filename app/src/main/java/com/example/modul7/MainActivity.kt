package com.example.modul7

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException
import java.net.URL
import java.util.Random
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var imgSlot1: ImageView
    private lateinit var imgSlot2: ImageView
    private lateinit var imgSlot3: ImageView
    private lateinit var btnGet: Button
    private lateinit var tvHasil: TextView
    private var isPlay = false

    private var execService1: ExecutorService? = null
    private var execService2: ExecutorService? = null
    private var execService3: ExecutorService? = null
    private var execServicePool: ExecutorService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnGet = findViewById(R.id.btn_get)
        imgSlot1 = findViewById(R.id.img_slot1)
        imgSlot2 = findViewById(R.id.img_slot2)
        imgSlot3 = findViewById(R.id.img_slot3)
        tvHasil = findViewById(R.id.tv_hasil)

        imgSlot1.setImageResource(R.drawable.slotbar)
        imgSlot2.setImageResource(R.drawable.slotbar)
        imgSlot3.setImageResource(R.drawable.slotbar)

        execService1 = Executors.newSingleThreadExecutor()
        execService2 = Executors.newSingleThreadExecutor()
        execService3 = Executors.newSingleThreadExecutor()
        execServicePool = Executors.newFixedThreadPool(3)

        val slotTask1 = SlotTask(imgSlot1)
        val slotTask2 = SlotTask(imgSlot2)
        val slotTask3 = SlotTask(imgSlot3)

        btnGet.setOnClickListener {
            if (!isPlay) {
                tvHasil.visibility = View.INVISIBLE
                slotTask1.reset()
                slotTask2.reset()
                slotTask3.reset()

                execServicePool?.execute(slotTask1)
                execServicePool?.execute(slotTask2)
                execServicePool?.execute(slotTask3)

                btnGet.text = "Stop"
                isPlay = true
            } else {
                slotTask1.stop()
                slotTask2.stop()
                slotTask3.stop()

                val allSame = slotTask1.imageId == slotTask2.imageId && slotTask2.imageId == slotTask3.imageId

                if (allSame) {
                    tvHasil.visibility = View.VISIBLE
                    tvHasil.text = "Gacorr Petir Zeuss"
                } else {
                    tvHasil.visibility = View.VISIBLE
                    tvHasil.text = "Anda Belum Beruntung"
                }

                btnGet.text = "Mulai Slot!!!"
                isPlay = false
            }
        }
    }

    internal class SlotTask(private val slotImg: ImageView) : Runnable {
        private val random = Random()
        var play = true
        private var arrayUrl = ArrayList<String>()
        var imageId: Int = -1

        override fun run() {
            try {
                while (play) {
                    val (imageUrl, id) = getRandomImage()

                    Handler(Looper.getMainLooper()).post {
                        Glide.with(slotImg.context)
                            .load(imageUrl)
                            .into(slotImg)
                    }

                    imageId = id

                    Thread.sleep(random.nextInt(500).toLong())
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }

        fun reset() {
            play = true
        }

        fun stop() {
            play = false
        }

        private fun getRandomImage(): Pair<String, Int> {
            val apiUrl = "https://662e87fba7dda1fa378d337e.mockapi.io/api/v1/fruits"
            return try {
                if (arrayUrl.isEmpty()) {
                    val jsonString = URL(apiUrl).readText()
                    val jsonArray = JSONArray(jsonString)
                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        arrayUrl.add(jsonObject.getString("url"))
                    }
                }

                val randomIndex = random.nextInt(arrayUrl.size)
                val imageUrl = arrayUrl[randomIndex]

                Pair(imageUrl, randomIndex)
            } catch (e: IOException) {
                e.printStackTrace()
                Pair("", -1)
            } catch (e: JSONException) {
                e.printStackTrace()
                Pair("", -1)
            }
        }
    }
}