package com.cz.android.cpp.sample.file

import android.Manifest
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.cz.android.cpp.sample.R
import com.cz.android.sample.library.component.message.SampleMessage
import com.cz.android.sample.library.function.permission.SamplePermission
import kotlinx.android.synthetic.main.activity_file_list_sample.*
import java.io.File

@SampleMessage
@SamplePermission(Manifest.permission.READ_EXTERNAL_STORAGE)
class FileListSampleActivity : AppCompatActivity() {
    companion object {
        private const val TAG="FileListSampleActivity"
        init {
            System.loadLibrary("file-native-lib")
        }
    }

    private external fun listDirectory(path: String)
    private external fun listDirectoryRecursively(path: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_list_sample)
        listDirectoryButton1.setOnClickListener {
            val st = SystemClock.elapsedRealtime()
            val file = Environment.getExternalStorageDirectory()
            startSearch(file)
            val time=SystemClock.elapsedRealtime()-st
            println("time1:$time")
        }
        listDirectoryButton2.setOnClickListener {
            val st = SystemClock.elapsedRealtime()
            val file = Environment.getExternalStorageDirectory()
            listDirectoryRecursively(file.absolutePath)
            val time=SystemClock.elapsedRealtime()-st
            println("time2:$time")
        }
        listDirectoryButton3.setOnClickListener {
            val st = SystemClock.elapsedRealtime()
            val file = Environment.getExternalStorageDirectory()
            listDirectoryRecursively(file.absolutePath)
            val time=SystemClock.elapsedRealtime()-st
            println("time3:$time")
        }
    }

    /**
     * List directory use java api.
     * @param file
     */
    private fun startSearch(file: File) {
        if (file.isDirectory) {
            val files: Array<File> = file.listFiles()
            if (null != files) {
                for (i in files.indices) {
                    startSearch(files[i])
                }
            }
        }
    }

}