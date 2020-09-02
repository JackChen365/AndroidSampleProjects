package com.cz.android.cpp.sample.file

import android.Manifest
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import com.cz.android.cpp.sample.R
import com.cz.android.sample.library.function.permission.PermissionResult
import com.cz.android.sample.library.function.permission.PermissionViewModelProviders
import com.cz.android.sample.library.function.permission.SamplePermission
import kotlinx.android.synthetic.main.activity_file_reader_sample.*
import java.io.File

@SamplePermission(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE)
class FileReaderSampleActivity : AppCompatActivity() {
    companion object {
        private const val TAG="FileReaderSampleActivity"
        init {
            System.loadLibrary("file-native-lib")
        }
    }

    private external fun readFileText(filePath:String):String;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_reader_sample)

        val viewModel = PermissionViewModelProviders.getViewModel(this)
        viewModel.addObserver { result ->
            if(!result.granted){
                textView.setText(R.string.permission_denied)
                loadButton.isEnabled=false;
            } else {
                loadButton.isEnabled=true
                loadButton.setOnClickListener {
                    val externalStorageDirectory = Environment.getExternalStorageDirectory()
                    val file= File(externalStorageDirectory,"test.txt")
                    if(!file.exists()){
                        val text=assets.open("little_price.txt").reader().readText()
                        file.createNewFile()
                        file.writeText(text)
                    }
                    file.deleteOnExit()
                    textView.text=readFileText(file.absolutePath)
                }
            }
        }

    }
}