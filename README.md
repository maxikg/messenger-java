# Messenger

Messenger is a routing library primary for AMQP but may be adoptable for other messaging systems. Messenger helps to
realize a communication between various instances and software.

Possible use cases may be:

 * **Multiple Websocket servers:** Routes a message to the point on which a determined websocket is connected.
 * **Distributed Bukkit/BungeeCord:** Routes a message from Bukkit to a BungeeCord or another Bukkit instance on which
   a player is connected. Of course also routing to servers by their names is possible.

By default an AMQP implementation is shipped with this library. Each endpoint generates a queue and ensures that the
right exchange is available. Each subscription will bind the temporary queue to the exchange.

## Features

 * Simple handle of AMQP binds/unbinds

## Example

Since the default implementation uses the AMQP protocol there is a compatible message broker required. I highly
recommend [RabbitMQ](https://www.rabbitmq.com/).

If you're not sure which imports are used in the following code snippets, here's a list of them:

```java
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import de.maxikg.messenger.AmqpMessenger;
import de.maxikg.messenger.queue.Queue;
import de.maxikg.messenger.queue.listener.QueueListener;
```

First of all we need a connection to the broker. In this case we connect to a Broker on localhost and use the virtual
host `test`. The virtual host must be present.

```java
ConnectionFactory connectionFactory = new ConnectionFactory();
connectionFactory.setUri("amqp://localhost/test");

Connection connection = connectionFactory.newConnection();
```

Now we can construct a new `AmqpMessenger`. A `Messenger` is a helper object containing a `Publisher` for outgoing
messages and a `Queue` for incoming messages. Each Messenger is able to serve one namespace and will use one Channel.

```java
AmqpMessenger messenger = new AmqpMessenger(connection, "messenger", "test");
messenger.initialize();
```

After the messenger is initialized we're able to subscribe to messages and publish them. But first of all we're going
to subscribe some targets and add a listener:

```java
Queue queue = messenger.getQueue();
queue.subscribe("test");
queue.addListener(new QueueListener() {
    @Override
    public void onMessage(String namespace, String target, String type, byte[] message) {
        System.out.println(new String(message));
    }
});
```

Now we can test it by publishing two messages:

```java
messenger.getPublisher().publish("test", null, "Hello World!".getBytes());
messenger.getPublisher().publish("something", null, "This shouldn't be displayed!".getBytes());
```

The console will display "Hello World!" since this is published to *test* what we've subscribed in the last step. But
the second message won't be displayed since it is published on something we've never subscribed. The unroutable message
will be just dropped.

## ToDo

 * Correct reconnection handle
 * GSON serialization/deserialization adapter

## License

This software is licensed under Apache License v2. A copy of this license is shipped within this repository in the
[LICENSE.txt](/LICENSE.txt) file.
