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

package de.maxikg.messenger.queue;

import com.google.common.base.Throwables;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import de.maxikg.messenger.queue.listener.QueueListener;
import de.maxikg.messenger.utils.AmqpUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
public class AmqpQueue implements Queue {

    @NonNull
    private final Channel channel;

    @NonNull
    private final String queueName;

    @NonNull
    private final String exchangeName;

    @NonNull
    private final String namespace;

    @Override
    public void subscribe(String target) {
        try {
            channel.queueBind(queueName, exchangeName, buildRoutingKey(target));
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void unsubscribe(String target) {
        try {
            channel.queueUnbind(queueName, exchangeName, buildRoutingKey(target));
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void addListener(final QueueListener listener) {
        try {
            channel.basicConsume(queueName, true, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String routingKey = envelope.getRoutingKey();
                    int dotIndex = routingKey.indexOf('.');
                    String namespace = routingKey.substring(0, dotIndex);
                    String target = routingKey.substring(dotIndex + 1);
                    String type = extractType(properties.getHeaders());

                    listener.onMessage(namespace, target, type, body);
                }
            });
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    private String buildRoutingKey(String target) {
        return namespace + "." + target;
    }

    public static String extractType(Map<String, Object> headers) {
        Object object = headers.get(AmqpUtils.HEADER_TYPE);
        return object != null ? object.toString() : null;
    }
}
