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

package com.oembedler.moon.graphql.engine.execute;

import graphql.GraphQLError;
import graphql.execution.ExecutionContext;
import graphql.execution.ExecutionStrategy;
import graphql.language.FragmentDefinition;
import graphql.language.OperationDefinition;
import graphql.schema.GraphQLSchema;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:java.lang.RuntimeException@gmail.com">oEmbedler Inc.</a>
 */
class GraphQLExecutionContext extends ExecutionContext {

    private final ExecutionContext delegate;
    private final int currentDepth;

    public GraphQLExecutionContext(ExecutionContext delegate, int currentDepth) {
        super(delegate.getGraphQLSchema(), delegate.getQueryStrategy(), delegate.getMutationStrategy(),
                delegate.getFragmentsByName(), delegate.getOperationDefinition(), delegate.getVariables(), delegate.getRoot());
        Assert.notNull(delegate, "ExecutionContext can not be null");
        Assert.notNull(delegate, "GraphQLSchemaHolder can not be null");
        this.delegate = delegate;
        this.currentDepth = currentDepth;
    }

    public int getCurrentDepth() {
        return currentDepth;
    }

    public GraphQLSchema getGraphQLSchema() {
        return delegate.getGraphQLSchema();
    }

    public Map<String, FragmentDefinition> getFragmentsByName() {
        return delegate.getFragmentsByName();
    }

    public OperationDefinition getOperationDefinition() {
        return delegate.getOperationDefinition();
    }

    public Map<String, Object> getVariables() {
        return delegate.getVariables();
    }

    public Object getRoot() {
        return delegate.getRoot();
    }

    public ExecutionStrategy getQueryStrategy() {
        return delegate.getQueryStrategy();
    }

    public FragmentDefinition getFragment(String name) {
        return delegate.getFragment(name);
    }

    public void addError(GraphQLError error) {
        delegate.addError(error);
    }

    public List<GraphQLError> getErrors() {
        return delegate.getErrors();
    }

    public ExecutionContext getDelegate() {
        return delegate;
    }
}
