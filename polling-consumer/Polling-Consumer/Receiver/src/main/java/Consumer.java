import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.concurrent.*;
import java.util.function.Function;

public class Consumer {
    public static void main(String[] args) {
        IAmAHandler<Greeting> greetingHandler = new GreetingHandler();
        Function<String, Greeting> messageDeserializer = messageBody -> {
            try {
                return new ObjectMapper().readValue(messageBody, Greeting.class);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        };

        var consumer = new PollingConsumer<Greeting>(greetingHandler, messageDeserializer, "greeting", "localhost");

        var executorService = Executors.newSingleThreadExecutor();

        try {
            System.out.println("Consumer running, entering loop until signalled");
            System.out.println(" Press [enter] to exit.");

            // has its own thread and will continue until signaled
            var task = consumer.run(executorService);

            while (true) {
                // loop until we get a keyboard interrupt
                if (System.in.available() > 0) {
                    // Note: This will deadlock with System.out.println on the task thread unless we have called println first
                    char key = (char) System.in.read();
                    if (key == '\n') {
                        // signal exit
                        task.cancel(true);
                        break;
                    }

                    Thread.yield();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }
    }
}
