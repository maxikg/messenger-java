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

import com.google.gson.Gson;
import de.maxikg.messenger.Messenger;

public class ObjectMessenger {

    private final TypeRegistry typeRegistry;
    private final ObjectPublisher publisher;
    private final ObjectQueueListener listener;

    public ObjectMessenger(Messenger messenger, ClassLoader classLoader) {
        this(new Gson(), messenger, classLoader);
    }

    public ObjectMessenger(Gson gson, Messenger messenger, ClassLoader classLoader) {
        typeRegistry = new TypeRegistry(classLoader);
        publisher = new ObjectPublisher(messenger.getPublisher(), typeRegistry, gson);
        listener = new ObjectQueueListener(typeRegistry, gson);

        messenger.getQueue().addListener(listener);
    }

    public TypeRegistry getTypeRegistry() {
        return typeRegistry;
    }

    public ObjectPublisher getPublisher() {
        return publisher;
    }

    public ObjectListenerRegistry getListenerRegistry() {
        return listener;
    }
}
