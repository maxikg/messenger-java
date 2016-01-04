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

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import de.maxikg.messenger.publisher.Publisher;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.nio.charset.Charset;

@RequiredArgsConstructor
public class ObjectPublisher {

    private static final Charset DEFAULT_CHARSET = Charsets.UTF_8;

    @NonNull
    private final Publisher publisher;

    @NonNull
    private final TypeRegistry typeRegistry;

    @NonNull
    private final Gson gson;

    @NonNull
    private final Charset charset;

    public ObjectPublisher(Publisher publisher, TypeRegistry typeRegistry, Gson gson) {
        this(publisher, typeRegistry, gson, DEFAULT_CHARSET);
    }

    public void publish(String target, Object o) {
        String type = typeRegistry.resolveTypeNameByClass(o.getClass());
        byte[] message = gson.toJson(o).getBytes(charset);

        publisher.publish(target, type, message);
    }
}
