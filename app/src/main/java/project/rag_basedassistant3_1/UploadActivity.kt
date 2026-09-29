package project.rag_basedassistant3_1

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import project.rag_basedassistant3_1.utils.FileUtils

class UploadActivity : AppCompatActivity() {

    private var selectedFileUri: Uri? = null

    private lateinit var btnSelectFile: Button
    private lateinit var btnUpload: Button
    private lateinit var tvSelectedFile: TextView
    private lateinit var tvStatus: TextView
    private lateinit var progressBar: ProgressBar

    private val selectFileLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            val fileName = FileUtils.getFileName(this, it)
            tvSelectedFile.text = fileName
            btnUpload.isEnabled = true
            tvStatus.text = ""
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        btnSelectFile = findViewById(R.id.btnSelectFile)
        btnUpload = findViewById(R.id.btnUpload)
        tvSelectedFile = findViewById(R.id.tvSelectedFile)
        tvStatus = findViewById(R.id.tvStatus)
        progressBar = findViewById(R.id.progressBar)

        btnSelectFile.setOnClickListener {
            selectFileLauncher.launch("*/*")
        }

        btnUpload.setOnClickListener {
            selectedFileUri?.let { uri ->
                uploadDocument(uri)
            }
        }
    }

    private fun uploadDocument(uri: Uri) {
        val file = FileUtils.getFileFromUri(this, uri)
        if (file == null || !file.exists()) {
            Toast.makeText(this, "Failed to load selected file", Toast.LENGTH_SHORT).show()
            return
        }

        val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
        val requestBody = file.asRequestBody(mimeType.toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestBody)

        progressBar.visibility = View.VISIBLE
        btnUpload.isEnabled = false
        btnSelectFile.isEnabled = false
        tvStatus.text = "Uploading..."

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.apiService.uploadDocument(body)
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    btnSelectFile.isEnabled = true
                    btnUpload.isEnabled = true

                    if (response.isSuccessful && response.body() != null) {
                        val uploadResponse = response.body()!!
                        tvStatus.text = uploadResponse.message ?: getString(R.string.upload_success)
                        Toast.makeText(this@UploadActivity, R.string.upload_success, Toast.LENGTH_SHORT).show()
                    } else {
                        tvStatus.text = getString(R.string.upload_failed) + " (Error: ${response.code()})"
                        Toast.makeText(this@UploadActivity, R.string.upload_failed, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    btnSelectFile.isEnabled = true
                    btnUpload.isEnabled = true
                    tvStatus.text = "Upload error: ${e.localizedMessage}"
                    Toast.makeText(this@UploadActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
