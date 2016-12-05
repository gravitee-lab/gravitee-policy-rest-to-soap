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

import io.gravitee.common.http.HttpHeaders;
import io.gravitee.common.http.HttpHeadersValues;
import io.gravitee.common.http.HttpMethod;
import io.gravitee.common.http.MediaType;
import io.gravitee.gateway.api.ExecutionContext;
import io.gravitee.gateway.api.Request;
import io.gravitee.gateway.api.Response;
import io.gravitee.gateway.api.buffer.Buffer;
import io.gravitee.gateway.api.stream.ReadWriteStream;
import io.gravitee.gateway.api.stream.TransformableStream;
import io.gravitee.gateway.api.stream.exception.TransformationException;
import io.gravitee.policy.api.PolicyChain;
import io.gravitee.policy.api.annotations.OnRequest;
import io.gravitee.policy.api.annotations.OnRequestContent;
import io.gravitee.policy.rest2soap.configuration.SoapTransformerPolicyConfiguration;
import io.gravitee.policy.rest2soap.el.ContentAwareEvaluableRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
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

        // Force HTTP headers with SOAP envelope
        request.headers().set(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_XML);
        request.headers().set(HttpHeaders.TRANSFER_ENCODING, HttpHeadersValues.TRANSFER_ENCODING_CHUNKED);

        if (soapTransformerPolicyConfiguration.getSoapAction() != null &&
                !soapTransformerPolicyConfiguration.getSoapAction().trim().isEmpty()) {
            LOGGER.debug("Add a SOAPAction header to invoke SOAP WS: {}", soapTransformerPolicyConfiguration.getSoapAction());
            request.headers().set(SOAP_ACTION_HEADER, soapTransformerPolicyConfiguration.getSoapAction());
        }

        policyChain.doNext(request, response);
    }

    @OnRequestContent
    public ReadWriteStream onRequestContent(Request request, ExecutionContext executionContext) {
        return new TransformableStream() {
            @Override
            public Function<Buffer, Buffer> transform() throws TransformationException {
                return buffer -> {
                    executionContext.getTemplateEngine().getTemplateContext().setVariable("request",
                            new ContentAwareEvaluableRequest(request, buffer.toString()));

                    String soapEnvelope = executionContext.getTemplateEngine().convert(
                            soapTransformerPolicyConfiguration.getEnvelope());

                    return Buffer.buffer(soapEnvelope);
                };
            }

            @Override
            public void end() {
                Buffer content = transform().apply(buffer);

                // Flush content
                super.flush(content);

                // Mark the end of content
                super.end();
            }
        };
    }
}
