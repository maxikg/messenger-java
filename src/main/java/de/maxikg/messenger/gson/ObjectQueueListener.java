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

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import de.maxikg.messenger.queue.listener.QueueListener;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

@RequiredArgsConstructor
public class ObjectQueueListener implements QueueListener, ObjectListenerRegistry {

    private final Map<String, ObjectMessageListener<?>> listeners = Maps.newConcurrentMap();

    @NonNull
    private final TypeRegistry typeRegistry;

    @NonNull
    private final Gson gson;

    @Override
    public <T> void register(ObjectMessageListener<T> listener) {
        listeners.put(typeRegistry.resolveTypeNameByClass(listener.getMessageClass()), listener);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onMessage(String namespace, String target, String type, byte[] message) {
        Class<?> clazz = typeRegistry.resolveClassByTypeName(type);
        ObjectMessageListener listener = listeners.get(type);
        if (type != null && listener != null)
            listener.onMessage(namespace, target, gson.fromJson(reader(message), clazz));
    }

    private static Reader reader(byte[] data) {
        return new InputStreamReader(new ByteArrayInputStream(data));
    }
}
