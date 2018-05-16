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
package io.gravitee.policy.rest2soap;

import io.gravitee.common.http.HttpMethod;
import io.gravitee.common.http.MediaType;
import io.gravitee.gateway.api.ExecutionContext;
import io.gravitee.gateway.api.Request;
import io.gravitee.gateway.api.Response;
import io.gravitee.gateway.api.buffer.Buffer;
import io.gravitee.gateway.api.http.stream.TransformableRequestStreamBuilder;
import io.gravitee.gateway.api.stream.ReadWriteStream;
import io.gravitee.policy.api.ChainScope;
import io.gravitee.policy.api.PolicyChain;
import io.gravitee.policy.api.annotations.*;
import io.gravitee.policy.rest2soap.configuration.SoapTransformerPolicyConfiguration;
import io.gravitee.policy.rest2soap.el.ContentAwareEvaluableRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
@Policy(
        category = @Category(io.gravitee.policy.api.Category.TRANSFORMATION),
        scope = @Scope(ChainScope.API)
)
public class RestToSoapTransformerPolicy {

    /**
     * LOGGER
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(RestToSoapTransformerPolicy.class);

    private final static String SOAP_ACTION_HEADER = "SOAPAction";

    /**
     * SOAP transformer configuration
     */
    private final SoapTransformerPolicyConfiguration soapTransformerPolicyConfiguration;

    public RestToSoapTransformerPolicy(SoapTransformerPolicyConfiguration soapTransformerPolicyConfiguration) {
        this.soapTransformerPolicyConfiguration = soapTransformerPolicyConfiguration;
    }

    @OnRequest
    public void onRequest(Request request, Response response, ExecutionContext executionContext, PolicyChain policyChain) {
        // Force method to POST for SOAP requests
        LOGGER.debug("Override HTTP methods for SOAP invocation");
        executionContext.setAttribute(ExecutionContext.ATTR_REQUEST_METHOD, HttpMethod.POST);

        if (soapTransformerPolicyConfiguration.getSoapAction() != null &&
                !soapTransformerPolicyConfiguration.getSoapAction().trim().isEmpty()) {
            LOGGER.debug("Add a SOAPAction header to invoke SOAP WS: {}", soapTransformerPolicyConfiguration.getSoapAction());
            request.headers().set(SOAP_ACTION_HEADER, soapTransformerPolicyConfiguration.getSoapAction());
        }

        policyChain.doNext(request, response);
    }

    @OnRequestContent
    public ReadWriteStream onRequestContent(Request request, ExecutionContext executionContext) {
        return TransformableRequestStreamBuilder
                .on(request)
                .contentType(MediaType.TEXT_XML)
                .transform(
                        buffer -> {
                            executionContext.getTemplateEngine().getTemplateContext().setVariable("request",
                                    new ContentAwareEvaluableRequest(request, buffer.toString()));

                            String soapEnvelope = executionContext.getTemplateEngine().convert(
                                    soapTransformerPolicyConfiguration.getEnvelope());

                            return Buffer.buffer(soapEnvelope);
                        }
                ).build();
    }
}
