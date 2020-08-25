package com.cz.android.cpp.sample.file

import android.Manifest
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.cz.android.cpp.sample.R
import com.cz.android.sample.library.function.permission.SamplePermission
import kotlinx.android.synthetic.main.activity_file_watch_sample.*
import java.io.File

@SamplePermission(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE)
class FileWatchSampleActivity : AppCompatActivity() {
    companion object{
        private const val TEST_FILE = "native_test.txt"
        init {
            System.loadLibrary("file-native-lib")
        }
    }

    private external fun startWatchFile(path: String?)
    private external fun removeWatchFile()
    private var filePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_watch_sample)

        val externalStorageDirectory = Environment.getExternalStorageDirectory()
        val file=File(externalStorageDirectory,TEST_FILE)
        editor.setText(file.absolutePath)

        startWatchButton.setOnClickListener {
            val file=File(editor.text.toString())
            if(!file.exists()){
                file.createNewFile()
            }
            startWatchFile(it)
        }

        stopWatchButton.setOnClickListener(this::stopWatchFile)

        addButton.setOnClickListener {
            newFile();
        }

        removeButton.setOnClickListener {
            removeFile()
        }

        updateButton.setOnClickListener{
            updateFile()
        }
    }

    //create a new file in path
    private fun newFile() {
        val file=File(editor.text.toString())
        if(!file.exists()){
            file.createNewFile()
        }
        updateFile()
    }

    //remove a file from path
    private fun removeFile() {
        val file=File(editor.text.toString())
        if(file.exists()){
            file.delete()
        }
    }

    //update a file from path,If not existed,will be to create a new file
    private fun updateFile() {
        val file=File(editor.text.toString())
        file?.appendText("New line\n")
    }

    //watch a existed file path
    private fun startWatchFile(view: View) {
        view.isEnabled = false
        stopWatchButton.setEnabled(true)
        filePath = editor.getText().toString()
        val file = File(filePath)
        startWatchFile(file.parentFile.absolutePath)
    }

    //remove the watch file path
    private fun stopWatchFile(view: View) {
        startWatchButton.setEnabled(true)
        view.isEnabled = false
        removeWatchFile()
    }

    override fun onDestroy() {
        removeFile()
        removeWatchFile()
        super.onDestroy()
    }
}