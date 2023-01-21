package dev.unnm3d.redischat.redis.redistools;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import lombok.AllArgsConstructor;

import java.time.Duration;
import java.util.concurrent.*;
import java.util.function.Function;

@AllArgsConstructor
public abstract class RedisAbstract {
    protected static final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    protected RedisClient lettuceRedisClient;

    public <T> CompletionStage<T> getConnectionAsync(Function<RedisAsyncCommands<String, String>, CompletionStage<T>> redisCallBack) {
        StatefulRedisConnection<String, String> connection = lettuceRedisClient.connect();
        CompletionStage<T> returnable = redisCallBack.apply(connection.async());
        return returnable.thenApply(t -> {
            connection.close();
            return t;
        });
    }

    public void close() {
        lettuceRedisClient.shutdown(Duration.ofSeconds(1), Duration.ofSeconds(1));
        executorService.shutdown();
    }

}
