package com.cz.android.sample.tool.component;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.cz.android.sample.tool.ComponentAnalyzer;
import com.cz.android.sample.tool.Debug;

import java.util.Map;

/**
 * @author Created by cz
 * @date 2020/8/6 12:13 PM
 * @email bingo110@126.com
 */
public class FragmentComponentAnalyzer implements ComponentAnalyzer {
    @Override
    public boolean applyInstance(Class<?> clazz) {
        return clazz.isAssignableFrom(Fragment.class);
    }

    @Override
    public void registerComponent(Context context) {
        Application application= (Application) context.getApplicationContext();
        application.registerActivityLifecycleCallbacks(new ActivityLifeCycleCallbackAdapter() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
                super.onActivityCreated(activity, bundle);
                if(activity instanceof FragmentActivity){
                    FragmentActivity fragmentActivity = (FragmentActivity) activity;
                    FragmentManager supportFragmentManager = fragmentActivity.getSupportFragmentManager();
                    supportFragmentManager.registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
                        @Override
                        public void onFragmentPreAttached(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull Context context) {
                            super.onFragmentPreAttached(fm, f, context);
                            Debug.trace(f,"onFragmentPreAttached");
                        }

                        @Override
                        public void onFragmentAttached(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull Context context) {
                            super.onFragmentAttached(fm, f, context);
                            Debug.trace(f,"onFragmentAttached");
                        }

                        @Override
                        public void onFragmentPreCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @Nullable Bundle savedInstanceState) {
                            super.onFragmentPreCreated(fm, f, savedInstanceState);
                            Debug.trace(f,"onFragmentPreCreated");
                        }

                        @Override
                        public void onFragmentCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @Nullable Bundle savedInstanceState) {
                            super.onFragmentCreated(fm, f, savedInstanceState);
                            Debug.trace(f,"onFragmentCreated");
                        }

                        @Override
                        public void onFragmentActivityCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @Nullable Bundle savedInstanceState) {
                            super.onFragmentActivityCreated(fm, f, savedInstanceState);
                            Debug.trace(f,"onFragmentActivityCreated");
                        }

                        @Override
                        public void onFragmentViewCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull View v, @Nullable Bundle savedInstanceState) {
                            super.onFragmentViewCreated(fm, f, v, savedInstanceState);
                            Debug.trace(f,"onFragmentViewCreated");
                        }

                        @Override
                        public void onFragmentStarted(@NonNull FragmentManager fm, @NonNull Fragment f) {
                            super.onFragmentStarted(fm, f);
                            Debug.trace(f,"onFragmentStarted");
                        }

                        @Override
                        public void onFragmentResumed(@NonNull FragmentManager fm, @NonNull Fragment f) {
                            super.onFragmentResumed(fm, f);
                            Debug.trace(f,"onFragmentResumed");
                        }

                        @Override
                        public void onFragmentPaused(@NonNull FragmentManager fm, @NonNull Fragment f) {
                            super.onFragmentPaused(fm, f);
                            Debug.trace(f,"onFragmentPaused");
                        }

                        @Override
                        public void onFragmentStopped(@NonNull FragmentManager fm, @NonNull Fragment f) {
                            super.onFragmentStopped(fm, f);
                            Debug.trace(f,"onFragmentStopped");
                        }

                        @Override
                        public void onFragmentSaveInstanceState(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull Bundle outState) {
                            super.onFragmentSaveInstanceState(fm, f, outState);
                            Debug.trace(f,"onFragmentSaveInstanceState");
                        }

                        @Override
                        public void onFragmentViewDestroyed(@NonNull FragmentManager fm, @NonNull Fragment f) {
                            super.onFragmentViewDestroyed(fm, f);
                            Debug.trace(f,"onFragmentViewDestroyed");
                        }

                        @Override
                        public void onFragmentDestroyed(@NonNull FragmentManager fm, @NonNull Fragment f) {
                            super.onFragmentDestroyed(fm, f);
                            Debug.trace(f,"onFragmentDestroyed");
                        }

                        @Override
                        public void onFragmentDetached(@NonNull FragmentManager fm, @NonNull Fragment f) {
                            super.onFragmentDetached(fm, f);
                            Debug.trace(f,"onFragmentDetached");
                        }
                    },true);
                }
            }
        });
    }

    @Override
    public void analysis(Context context,Class<?> clazz, Map<String,Long> traceMap) {
    }
}
