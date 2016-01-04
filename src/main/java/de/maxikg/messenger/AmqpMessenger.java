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

package de.maxikg.messenger;

import com.google.common.base.Throwables;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import de.maxikg.messenger.publisher.AmqpPublisher;
import de.maxikg.messenger.publisher.Publisher;
import de.maxikg.messenger.queue.AmqpQueue;
import de.maxikg.messenger.queue.Queue;
import de.maxikg.messenger.utils.Initializable;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RequiredArgsConstructor
public class AmqpMessenger implements Messenger, Initializable, Closeable {

    @NonNull
    private final Connection connection;
    @NonNull
    private final String exchangeName;
    @NonNull
    private final String namespace;
    private Channel channel;
    private Publisher publisher;
    private Queue queue;

    @Override
    public synchronized void initialize() {
        try {
            channel = connection.createChannel();

            channel.exchangeDeclare(exchangeName, "topic");
            String queueName = channel.queueDeclare().getQueue();
            queue = new AmqpQueue(channel, queueName, exchangeName, namespace);
            publisher = new AmqpPublisher(channel, exchangeName, namespace, false);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public Publisher getPublisher() {
        if (publisher == null)
            throw new IllegalStateException("Publisher is not initialized.");

        return publisher;
    }

    @Override
    public Queue getQueue() {
        if (queue == null)
            throw new IllegalStateException("Queue is not initialized.");

        return queue;
    }

    @Override
    public void close() throws IOException {
        try {
            channel.close();
        } catch (TimeoutException e) {
            throw new IOException("Close timeout.", e);
        }
    }
}
