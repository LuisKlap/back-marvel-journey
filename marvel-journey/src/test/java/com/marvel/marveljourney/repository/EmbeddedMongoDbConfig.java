package com.marvel.marveljourney.repository;

import com.marvel.marveljourney.exception.CustomExceptionHandler;
import com.mongodb.client.MongoClients;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.Defaults;
import de.flapdoodle.embed.mongo.config.ImmutableMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.runtime.Network;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.data.mongodb.core.MongoTemplate;

public abstract class EmbeddedMongoDbConfig {

    private static final String CONNECTION_STRING = "mongodb://%s:%d";
    private MongodExecutable mongodExecutable;
    protected MongoTemplate mongoTemplate;

    @BeforeEach
    void setup() {
        try {
            String ip = "localhost";
            int port = 27017;

            ImmutableMongodConfig mongodConfig = MongodConfig.builder()
                    .version(Version.Main.PRODUCTION)
                    .net(new Net(ip, port, Network.localhostIsIPv6()))
                    .build();

            ProcessOutput processOutput = ProcessOutput.getDefaultInstanceSilent();

            MongodStarter starter = MongodStarter.getInstance(Defaults.runtimeConfigFor(Command.MongoD)
                    .processOutput(processOutput)
                    .artifactStore(Defaults.extractedArtifactStoreFor(Command.MongoD))
                    .build());

            mongodExecutable = starter.prepare(mongodConfig);
            mongodExecutable.start();
            mongoTemplate = new MongoTemplate(MongoClients.create(String.format(CONNECTION_STRING, ip, port)), "testdb");
        } catch (Exception e) {
            CustomExceptionHandler.handleException(e);
        }
    }

    @AfterEach
    void clean() {
        try {
            if (mongodExecutable != null) {
                mongodExecutable.stop();
            }
        } catch (Exception e) {
            CustomExceptionHandler.handleException(e);
        }
    }
}