package com.cz.android.sample.tool.component;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cz.android.sample.tool.ComponentAnalyzer;
import com.cz.android.sample.tool.Debug;

import java.util.Map;

/**
 * @author Created by cz
 * @date 2020/8/6 12:09 PM
 * @email bingo110@126.com
 */
public class ActivityComponentAnalyzer implements ComponentAnalyzer {

    @Override
    public boolean applyInstance(Class<?> clazz) {
        return clazz.isAssignableFrom(Activity.class);
    }

    @Override
    public void registerComponent(Context context) {
        Application application= (Application) context.getApplicationContext();
        application.registerActivityLifecycleCallbacks(new ActivityLifeCycleCallbackAdapter() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
                Debug.trace(activity,"onActivityCreated");
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
                Debug.trace(activity,"onActivityStarted");
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                Debug.trace(activity,"onActivityResumed");
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
                Debug.trace(activity,"onActivityPaused");
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
                Debug.trace(activity,"onActivityStopped");
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {
                Debug.trace(activity,"onActivitySaveInstanceState");
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                Debug.trace(activity,"onActivityDestroyed");
            }

            @Override
            public void onActivityPreCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                super.onActivityPreCreated(activity, savedInstanceState);
                Debug.trace(activity,"onActivityPreCreated");
            }

            @Override
            public void onActivityPostCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                super.onActivityPostCreated(activity, savedInstanceState);
                Debug.trace(activity,"onActivityPostCreated");
            }

            @Override
            public void onActivityPreStarted(@NonNull Activity activity) {
                super.onActivityPreStarted(activity);
                Debug.trace(activity,"onActivityPreStarted");
            }

            @Override
            public void onActivityPostStarted(@NonNull Activity activity) {
                super.onActivityPostStarted(activity);
                Debug.trace(activity,"onActivityPostStarted");
            }

            @Override
            public void onActivityPreResumed(@NonNull Activity activity) {
                super.onActivityPreResumed(activity);
                Debug.trace(activity,"onActivityPreResumed");
            }

            @Override
            public void onActivityPostResumed(@NonNull Activity activity) {
                super.onActivityPostResumed(activity);
                Debug.trace(activity,"onActivityPostResumed");
            }

            @Override
            public void onActivityPrePaused(@NonNull Activity activity) {
                super.onActivityPrePaused(activity);
                Debug.trace(activity,"onActivityPrePaused");
            }

            @Override
            public void onActivityPostPaused(@NonNull Activity activity) {
                super.onActivityPostPaused(activity);
                Debug.trace(activity,"onActivityPostPaused");
            }

            @Override
            public void onActivityPreStopped(@NonNull Activity activity) {
                super.onActivityPreStopped(activity);
                Debug.trace(activity,"onActivityPreStopped");
            }

            @Override
            public void onActivityPostStopped(@NonNull Activity activity) {
                super.onActivityPostStopped(activity);
                Debug.trace(activity,"onActivityPostStopped");
            }

            @Override
            public void onActivityPreSaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
                super.onActivityPreSaveInstanceState(activity, outState);
                Debug.trace(activity,"onActivityPreSaveInstanceState");
            }

            @Override
            public void onActivityPostSaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
                super.onActivityPostSaveInstanceState(activity, outState);
                Debug.trace(activity,"onActivityPostSaveInstanceState");
            }

            @Override
            public void onActivityPreDestroyed(@NonNull Activity activity) {
                super.onActivityPreDestroyed(activity);
                Debug.trace(activity,"onActivityPreDestroyed");
            }

            @Override
            public void onActivityPostDestroyed(@NonNull Activity activity) {
                super.onActivityPostDestroyed(activity);
                Debug.trace(activity,"onActivityPostDestroyed");
            }
        });
    }

    @Override
    public void analysis(Context context,Class<?> clazz,Map<String,Long> traceMap) {
        StringBuilder output=new StringBuilder();
        output.append("Class:"+clazz.getSimpleName()+"\n");

        Long postResumed = traceMap.get("onActivityPostResumed");
        Long preResumed = traceMap.get("onActivityPreResumed");
        long resumedTimeConsumption = postResumed - preResumed;
        output.append("|-- Resumed:"+(postResumed));
        output.append("\t|-- PreResumed:"+(postResumed));
        output.append("\t|-- PostResumed:"+(preResumed));
        output.append("\t|-- Resume:"+(resumedTimeConsumption));


    }
}
