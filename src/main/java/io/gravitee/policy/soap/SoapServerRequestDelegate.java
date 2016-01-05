/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.policy.soap;

import io.gravitee.common.http.HttpHeaders;
import io.gravitee.common.http.HttpMethod;
import io.gravitee.common.http.HttpVersion;
import io.gravitee.gateway.api.Request;
import io.gravitee.gateway.api.handler.Handler;
import io.gravitee.gateway.api.http.BodyPart;

import java.time.Instant;
import java.util.Map;

/**
 * @author David BRASSELY (brasseld at gmail.com)
 */
public class SoapServerRequestDelegate implements Request {

    private final Request delegate;

    private String content;

    SoapServerRequestDelegate(final Request delegate) {
        this.delegate = delegate;
    }

    @Override
    public String id() {
        return delegate.id();
    }

    @Override
    public String uri() {
        return delegate.uri();
    }

    @Override
    public String path() {
        return delegate.path();
    }

    @Override
    public Map<String, String> parameters() {
        return delegate.parameters();
    }

    @Override
    public HttpHeaders headers() {
        return delegate.headers();
    }

    @Override
    public HttpMethod method() {
        return delegate.method();
    }

    @Override
    public HttpVersion version() {
        return delegate.version();
    }

    @Override
    public Instant timestamp() {
        return delegate.timestamp();
    }

    @Override
    public String remoteAddress() {
        return delegate.remoteAddress();
    }

    @Override
    public String localAddress() {
        return delegate.localAddress();
    }

    @Override
    public Request bodyHandler(Handler<BodyPart> bodyHandler) {
        bodyHandler.handle(new StringBodyPart(content));
        return this;
    }

    @Override
    public Request endHandler(Handler<Void> handler) {
        return delegate.endHandler(handler);
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
