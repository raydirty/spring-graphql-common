/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 oEmbedler Inc. and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 *  documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 *  rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 *  persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.oembedler.moon.graphql.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oembedler.moon.graphql.GraphQLSchemaBeanFactory;
import com.oembedler.moon.graphql.SpringGraphQLSchemaBeanFactory;
import com.oembedler.moon.graphql.engine.GraphQLSchemaBuilder;
import com.oembedler.moon.graphql.engine.GraphQLSchemaConfig;
import com.oembedler.moon.graphql.engine.GraphQLSchemaHolder;
import com.oembedler.moon.graphql.engine.execute.GraphQLQueryExecutor;
import com.oembedler.moon.graphql.engine.execute.GraphQLRxExecutionResult;
import com.oembedler.moon.graphql.test.simpleschema.ComputationDelaySchema;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.concurrent.Executors;

/**
 * @author <a href="mailto:java.lang.RuntimeException@gmail.com">oEmbedler Inc.</a>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = GenericComputationDelaySchemaParserTest.SimpleSchemaConfiguration.class)
public class GenericComputationDelaySchemaParserTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericComputationDelaySchemaParserTest.class);


    private ObjectMapper mapper = new ObjectMapper();

    @Configuration
    @ComponentScan(basePackages = "com.oembedler.moon.graphql.test.simpleschema")
    public static class SimpleSchemaConfiguration {

        @Bean
        public GraphQLSchemaBeanFactory graphQLSchemaBeanFactory() {
            return new SpringGraphQLSchemaBeanFactory();
        }

        @Bean
        public GraphQLSchemaConfig graphQLSchemaConfig() {
            GraphQLSchemaConfig graphQLSchemaConfig = new GraphQLSchemaConfig();
            return graphQLSchemaConfig;
        }

        @Bean
        public GraphQLSchemaBuilder graphQLSchemaBuilder() {
            return new GraphQLSchemaBuilder(graphQLSchemaConfig(), graphQLSchemaBeanFactory());
        }

        @Bean
        public GraphQLSchemaHolder graphQLSchemaHolder() {
            return graphQLSchemaBuilder().buildSchema(ComputationDelaySchema.class);
        }
    }

    @Autowired
    public GraphQLSchemaBuilder graphQLSchemaBuilder;
    @Autowired
    public GraphQLSchemaHolder graphQLSchemaHolder;

    public int parallelism = 2;

    private Object prettifyPrint(Object input) {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(input);
        } catch (JsonProcessingException e) {
            LOGGER.error("Prettifying error", e);
        }
        return input;
    }

    public String getClasspathResourceAsString(final String resourceName) {
        String resourceAsString = null;

        ClassPathResource classPathResource = new ClassPathResource(resourceName);
        try (InputStream is = classPathResource.getInputStream()) {
            resourceAsString = StreamUtils.copyToString(is, Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resourceAsString;
    }

    @Test
    public void concurrentIntrospectionQueryForkJoin_Success() throws IOException {
        GraphQLRxExecutionResult result =
                GraphQLQueryExecutor
                        .create(graphQLSchemaHolder)
                        .forkJoinExecutorService(parallelism)
                        .query(getClasspathResourceAsString("introspection.query"))
                        .execute();

        Assert.assertTrue(result.getErrors().size() == 0);
        LOGGER.info("Complexity: {}. Result: {}", result.getComplexity(), prettifyPrint(result.getData()));
    }

    @Test
    public void concurrentExecutionQueryForkJoin_Success() throws IOException {
        GraphQLRxExecutionResult result =
                GraphQLQueryExecutor
                        .create(graphQLSchemaHolder)
                        .forkJoinExecutorService(parallelism)
                        .query(getClasspathResourceAsString("root-nodes.query"))
                        .execute();

        Assert.assertTrue(result.getErrors().size() == 0);
        LOGGER.info("Complexity: {}. Result: {}", result.getComplexity(), prettifyPrint(result.getData()));
    }

    @Test
    public void concurrentExecutionQueryFixedThreadPool_Success() throws IOException {
        GraphQLRxExecutionResult result =
                GraphQLQueryExecutor
                        .create(graphQLSchemaHolder)
                        .executorService(Executors.newFixedThreadPool(parallelism))
                        .query(getClasspathResourceAsString("root-nodes.query"))
                        .execute();

        Assert.assertTrue(result.getErrors().size() == 0);
        LOGGER.info("Complexity: {}. Result: {}", result.getComplexity(), prettifyPrint(result.getData()));
    }

    @Test
    public void concurrentIntrospectionQueryFixedThreadPool_Success() throws IOException {
        GraphQLRxExecutionResult result =
                GraphQLQueryExecutor
                        .create(graphQLSchemaHolder)
                        .executorService(Executors.newFixedThreadPool(parallelism))
                        .query(getClasspathResourceAsString("introspection.query"))
                        .execute();

        Assert.assertTrue(result.getErrors().size() == 0);
        LOGGER.info("Complexity: {}. Result: {}", result.getComplexity(), prettifyPrint(result.getData()));
    }

    @Test
    public void serialExecutionQuery_Success() throws IOException {
        GraphQLRxExecutionResult result =
                GraphQLQueryExecutor
                        .create(graphQLSchemaHolder)
                        .query(getClasspathResourceAsString("root-nodes.query"))
                        .execute();

        Assert.assertTrue(result.getErrors().size() == 0);
        LOGGER.info("Complexity: {}. Result: {}", result.getComplexity(), prettifyPrint(result.getData()));
    }

    @Test
    public void serialIntrospectionQuery_Success() throws IOException {
        GraphQLRxExecutionResult result =
                GraphQLQueryExecutor
                        .create(graphQLSchemaHolder)
                        .query(getClasspathResourceAsString("introspection.query"))
                        .execute();

        Assert.assertTrue(result.getErrors().size() == 0);
        LOGGER.info("Complexity: {}. Result: {}", result.getComplexity(), prettifyPrint(result.getData()));
    }
}
