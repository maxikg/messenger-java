# Messenger [![Build Status](https://travis-ci.org/maxikg/messenger-java.svg)](https://travis-ci.org/maxikg/messenger-java)

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
 * Gson adapter for working with models instead of raw messages

## Installation

The software is currently not deployed to any Maven repository. So you need to install it by hand:

 1. Clone the repository: `git clone https://github.com/maxikg/messenger-java.git`
 2. Change directory: `cd messenger-java`
 3. Invoke maven: `mvn install`

After a successful build the library is available in your local Maven repository:

```xml
<dependency>
    <groupId>de.maxikg</groupId>
    <artifactId>messenger</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

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

### Use Gson adapter

If you want to use model objects instead of raw messages you can use Google GSON and the build in adapter for it.
Please follow all above steps up to `messenger.initialize();` before you start with the Gson adapter. In the following
example we use different imports:

```java
import com.rabbitmq.client.ConnectionFactory;
import de.maxikg.messenger.AmqpMessenger;
import de.maxikg.messenger.gson.AbstractObjectMessageListener;
import de.maxikg.messenger.gson.ObjectMessenger;
```

Furthermore we using a Model class named `Demo`:

```java
public class Demo {

    private final String test;
    
    public Demo(String test) {
        this.test = test;
    }
    
    public String getTest() {
        return test;
    }
    
    @Override
    public void toString() {
        return getClass().getSimpleName() + "(test=" + getTest() + ")";
    }
}
```

If your Messenger is ready you can start by constructing a `ObjectMessenger`:

```java
ObjectMessenger objectMessenger = new ObjectMessenger(messenger);
```

The ObjectMessenger is used to manage the Message-Object-Conversion. By default a objects class domain name is used for
the type argument but you can specify type aliases on the `TypeRegistry`. In this example we don't use this feature.
We directly registering a listener:

```java
messenger.getQueue().subscribe("test");
objectMessenger.getListenerRegistry().register(new AbstractObjectMessageListener<Demo>(Demo.class) {
    @Override
    public void onMessage(String namespace, String target, Demo object) {
        System.out.println(object);
    }
});
```

After the listener is registered you can submit a `Demo` object:

```java
objectMessenger.getPublisher().publish("test", new Demo("Hello World"));
```

The console should display something like `Demo(test=Hello World)`.

## ToDo

 * Correct reconnection handle
 * Add JavaDoc

## License

This software is licensed under Apache License v2. A copy of this license is shipped within this repository in the
[LICENSE.txt](/LICENSE.txt) file.
