package com.cz.android.cpp.sample.basic

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import com.cz.android.cpp.sample.R
import kotlinx.android.synthetic.main.activity_native_file_search.*

class NativeFileSearchActivity : AppCompatActivity() {
    companion object {
        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("file-search")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_native_file_search)

        searchButton.setOnClickListener {
            val externalStorageDirectory = Environment.getExternalStorageDirectory()
            searchFile(externalStorageDirectory.absolutePath)
        }
    }

    private external fun searchFile(filePath:String)
}