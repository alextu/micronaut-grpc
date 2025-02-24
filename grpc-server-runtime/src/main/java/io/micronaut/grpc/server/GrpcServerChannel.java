/*
 * Copyright 2017-2019 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.grpc.server;

import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.scheduling.TaskExecutors;

import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * A factory that returns a {@link ManagedChannel} allowing communication with the embedded server.
 * Primarily used for testing.
 *
 * @author graemerocher
 * @since 1.0
 */
@Factory
public class GrpcServerChannel {

    public static final String NAME = "grpc-server";

    /**
     * Constructs a managed server channel.
     * @param server The server
     * @param executorService The executor service
     * @param clientInterceptors The client interceptors
     * @return The channel
     */
    @Singleton
    @Named(NAME)
    @Requires(beans = GrpcEmbeddedServer.class)
    @Bean(preDestroy = "shutdown")
    protected ManagedChannel serverChannel(
            GrpcEmbeddedServer server,
            @Named(TaskExecutors.IO) ExecutorService executorService,
            List<ClientInterceptor> clientInterceptors) {
        final ManagedChannelBuilder<?> builder = ManagedChannelBuilder.forAddress(
                server.getHost(),
                server.getPort()
        ).executor(executorService);
        if (!server.getServerConfiguration().isSecure()) {
            builder.usePlaintext();
        }
        if (CollectionUtils.isNotEmpty(clientInterceptors)) {
            builder.intercept(clientInterceptors);
        }
        return builder.build();
    }
}
