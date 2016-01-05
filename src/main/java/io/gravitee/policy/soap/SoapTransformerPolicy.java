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
import io.gravitee.common.http.HttpHeadersValues;
import io.gravitee.gateway.api.ExecutionContext;
import io.gravitee.gateway.api.Request;
import io.gravitee.gateway.api.Response;
import io.gravitee.policy.api.PolicyChain;
import io.gravitee.policy.api.annotations.OnRequest;
import io.gravitee.policy.soap.configuration.SoapTransformerPolicyConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author David BRASSELY (brasseld at gmail.com)
 */
public class SoapTransformerPolicy {

    /**
     * LOGGER
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(SoapTransformerPolicy.class);

    /**
     * SOAP transformer configuration
     */
    private final SoapTransformerPolicyConfiguration soapTransformerPolicyConfiguration;

    public SoapTransformerPolicy(SoapTransformerPolicyConfiguration soapTransformerPolicyConfiguration) {
        this.soapTransformerPolicyConfiguration = soapTransformerPolicyConfiguration;
    }

    @OnRequest
    public void onRequest(Request request, Response response, ExecutionContext executionContext, PolicyChain policyChain) {
        String soapEnvelope = executionContext.getTemplateEngine().convert(
                soapTransformerPolicyConfiguration.getEnvelope());

        // By specifying this attribute, you're reading the request data without pushing them in the final client request.
        executionContext.setAttribute(ExecutionContext.ATTR_BODY_CONTENT, soapEnvelope);

        request.headers().set(HttpHeaders.CONTENT_TYPE, "text/xml");
        request.headers().set(HttpHeaders.CONTENT_LENGTH, Integer.toString(soapEnvelope.length()));

        policyChain.doNext(request, response);
    }
}
