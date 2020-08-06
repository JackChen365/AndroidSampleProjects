package com.cz.android.sample.tool;

import android.content.Context;

import com.cz.android.sample.tool.component.ActivityComponentAnalyzer;
import com.cz.android.sample.tool.component.FragmentComponentAnalyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Created by cz
 * @date 2020/8/6 12:03 PM
 * @email bingo110@126.com
 */
public class Debug {
    private static long startTimeMillis;
    private static Map<String,AnalysisObject> debugObjectMap =new HashMap<>();
    private static final List<ComponentAnalyzer> componentAnalyzerList =new ArrayList<>();

    static {
        componentAnalyzerList.add(new ActivityComponentAnalyzer());
        componentAnalyzerList.add(new FragmentComponentAnalyzer());
    }

    public static void debug(Context context){
        startTimeMillis=System.currentTimeMillis();
        for(ComponentAnalyzer componentAnalyzer:componentAnalyzerList){
            componentAnalyzer.registerComponent(context);
        }
    }

    public static void trace(Object object, String tag){
        String simpleName = object.getClass().getSimpleName()+"_"+object.hashCode();
        AnalysisObject analysisObject = debugObjectMap.get(simpleName);
        if(null==analysisObject){
            analysisObject=new AnalysisObject(object.getClass(),simpleName);
            debugObjectMap.put(simpleName,analysisObject);
        }
        analysisObject.traceMap.put(tag,System.currentTimeMillis());
    }

    public static void analysis(Context context){
        for(AnalysisObject analysisObject:debugObjectMap.values()){
            for(ComponentAnalyzer componentAnalyzer:componentAnalyzerList){
                if(componentAnalyzer.applyInstance(analysisObject.clazz)){
                    componentAnalyzer.analysis(context,analysisObject.clazz,analysisObject.traceMap);
                }
            }
        }
    }

    static class AnalysisObject{
        final Class clazz;
        final String name;
        Map<String,Long> traceMap=new LinkedHashMap<>();

        public AnalysisObject(Class clazz, String name) {
            this.clazz = clazz;
            this.name = name;
        }
    }
}
