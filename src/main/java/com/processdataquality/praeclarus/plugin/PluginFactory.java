/*
 * Copyright (c) 2021 Queensland University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.processdataquality.praeclarus.plugin;


import com.processdataquality.praeclarus.annotations.Pattern;
import com.processdataquality.praeclarus.annotations.Plugin;
import com.processdataquality.praeclarus.config.PluginConfig;
import com.processdataquality.praeclarus.writer.DataWriter;

import java.io.IOException;
import java.util.*;

/**
 * @author Michael Adams
 * @date 14/4/21
 */
public class PluginFactory<T> {

    private final Map<String, Class<T>> _classMap;
    
    public PluginFactory(Class<T> type) {
        _classMap = buildMap(type);
    }


    public List<String> getPluginNames() {
        return new ArrayList<>(_classMap.keySet());
    }


    public List<Plugin> getMetaDataList() {
        List<Plugin> list = new ArrayList<>();
        for (Class<?> clazz : _classMap.values()) {
            Plugin metaData = clazz.getAnnotation(Plugin.class);
            if (metaData != null) {
                list.add(metaData);
            }
        }
        return list;
    }


    public Plugin getPluginAnnotation(String className) {
        Class<?> clazz = _classMap.get(className);
        if (clazz != null) {
            return clazz.getAnnotation(Plugin.class);
        }
        return null;
    }


    public List<Pattern> getPatternAnnotations(String className) {
        Class<?> clazz = _classMap.get(className);
        if (clazz != null) {
            Pattern[] annotations = clazz.getAnnotationsByType(Pattern.class);
            return annotations.length > 0 ? Arrays.asList(annotations) :
                    Collections.emptyList();
        }
        return null;
    }


    public T newInstance(String className) {
        Class<T> clazz = _classMap.get(className);
        return newInstance(clazz);
    }


    private T newInstance(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        }
        catch (Throwable e) {
            return null;
        }
    }


    private Map<String, Class<T>> buildMap(Class<T> type) {
        try {
            PluginLoader loader = new PluginLoader(new PluginConfig().getPaths());
            return loader.loadAsMap(type);
        }
        catch (IOException e) {
            System.out.println("Failed to load plugins.");
            return Collections.emptyMap();
            //e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        PluginFactory<DataWriter> p = new PluginFactory<>(DataWriter.class);
        List<String> list = p.getPluginNames();
        for (String n : list) {
            System.out.println(n);
        }
        DataWriter d = p.newInstance(list.get(0));

    }
}
