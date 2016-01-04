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

package de.maxikg.messenger.publisher;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.impl.LongStringHelper;
import de.maxikg.messenger.utils.AmqpUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
public class AmqpPublisher implements Publisher {

    @NonNull
    private final Channel channel;

    @NonNull
    private final String exchangeName;

    @NonNull
    private final String namespace;

    private final boolean durableMessages;

    @Override
    public void publish(String target, String type, byte[] message) {
        try {
            channel.basicPublish(
                    exchangeName,
                    namespace + "." + target,
                    new AMQP.BasicProperties.Builder()
                            .headers(headers(type))
                            .deliveryMode(durableMessages ? 2 : 1)
                            .build(),
                    message
            );
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    private static Map<String, Object> headers(String type) {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        if (type != null)
            builder.put(AmqpUtils.HEADER_TYPE, LongStringHelper.asLongString(type));
        return builder.build();
    }
}
