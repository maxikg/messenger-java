/*
 * Copyright 2016 Max Walsch (github.com/maxikg)
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package de.maxikg.messenger.gson;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TypeRegistry {

    private final BiMap<Class<?>, String> types = Maps.synchronizedBiMap(HashBiMap.<Class<?>, String>create());

    @NonNull
    private final ClassLoader classLoader;

    public void register(String name, Class<?> type) {
        types.put(type, name);
    }

    public Class<?> resolveClassByTypeName(String name) {
        Class<?> clazz = types.inverse().get(name);
        if (clazz == null) {
            try {
                clazz = classLoader.loadClass(name);
                register(name, clazz);
            } catch (ClassNotFoundException ignore) {
            }
        }
        return clazz;
    }

    public String resolveTypeNameByClass(Class<?> clazz) {
        String type = types.get(clazz);
        if (type == null) {
            type = clazz.getName();
            register(type, clazz);
        }
        return types.get(clazz);
    }
}
