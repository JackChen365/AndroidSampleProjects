package com.cz.android.cpp.sample.basic

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.cz.android.cpp.sample.R
import kotlinx.android.synthetic.main.activity_native_method_invoke_sample.*
import kotlin.system.measureTimeMillis

class NativeMethodInvokeSampleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_native_method_invoke_sample)
        val getterMethodId = nGetIntMethod(NativeModel::class.java, "getAge")
        val setterMethodId = nSetIntMethod(NativeModel::class.java, "setAge")

        testButton.setOnClickListener {
            sample_text.text = "GetterMethodId:$getterMethodId\nSetterMethodId:$setterMethodId\n"
            val st1 = measureTimeMillis {
                val person = NativeModel(100, "SuperMan")
                for (i in 0 until 1000000) {
                    nCallSetIntMethod(person, setterMethodId, i)
                    nCallGetIntMethod(person, getterMethodId)
                }
            }
            sample_text.append("Native invoke time cost:$st1\n")

            val setterMethod = NativeModel::class.java.getMethod("setAge", Int::class.java)
            val getterMethod = NativeModel::class.java.getMethod("getAge")
            val st2 = measureTimeMillis {
                val person = NativeModel(100, "SuperMan")
                for (i in 0 until 1000000) {
                    setterMethod.invoke(person,i)
                    getterMethod.invoke(person)
                }
            }
            sample_text.append("Reflect time cost:$st2\n")

            val st3 = measureTimeMillis {
                val person = NativeModel(100, "SuperMan")
                for (i in 0 until 1000000) {
                    person.age=i
                    person.getAge();
                }
            }
            sample_text.append("Object invoke time cost:$st3\n")
            //times:100000
            //time1:429 time2:487
            //time1:418 time2:469

            //times:10000
            //time1:43 time2:47
            //time1:42 time2:46

            //times:1000000
            //time1:4221 time2:4664 time3:2

            Log.i(TAG,"test:"+(sample_text.text))
        }
    }

    external fun nGetIntMethod(targetClass: Class<*>, methodName: String): Long

    external fun nSetIntMethod(targetClass: Class<*>, methodName: String): Long

    external fun nCallSetIntMethod(target: Any, methodId:Long, arg:Int)

    external fun nCallGetIntMethod(target: Any, methodId:Long): Int

    companion object {
        private const val TAG="MainActivity"
        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("method-native-lib")
        }
    }
}
