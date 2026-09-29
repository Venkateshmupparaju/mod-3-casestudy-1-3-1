package project.rag_basedassistant3_1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnOpenUpload: Button = findViewById(R.id.btnOpenUpload)
        val btnOpenChat: Button = findViewById(R.id.btnOpenChat)

        btnOpenUpload.setOnClickListener {
            startActivity(Intent(this, UploadActivity::class.java))
        }

        btnOpenChat.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }
    }
}
