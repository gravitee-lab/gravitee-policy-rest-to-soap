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
package io.gravitee.policy.rest2soap.swagger;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.gravitee.policy.api.swagger.Policy;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
public class RestToSoapOAIOperationVisitorTest {

    private RestToSoapOAIOperationVisitor visitor = new RestToSoapOAIOperationVisitor();

    @Test
    public void operationWithoutExtension() {
        Operation operationMock = mock(Operation.class);
        when(operationMock.getExtensions()).thenReturn(null);
        Optional<Policy> policy = visitor.visit(mock(OpenAPI.class), operationMock);
        assertFalse(policy.isPresent());
    }

    @Test
    public void operationWithoutSoapEnvelope() {
        Map<String, Object> extensions = new HashMap<>();
        extensions.put(RestToSoapOAIOperationVisitor.SOAP_EXTENSION_ACTION, "someaction");
        Operation operationMock = mock(Operation.class);
        when(operationMock.getExtensions()).thenReturn(extensions);
        Optional<Policy> policy = visitor.visit(mock(OpenAPI.class), operationMock);
        assertFalse(policy.isPresent());
    }

    @Test
    public void operationWithoutSoapAction() throws Exception {
        Map<String, Object> extensions = new HashMap<>();
        extensions.put(RestToSoapOAIOperationVisitor.SOAP_EXTENSION_ENVELOPE, "<soap:envelope></soap:envelope>");
        Operation operationMock = mock(Operation.class);
        when(operationMock.getExtensions()).thenReturn(extensions);
        Optional<Policy> policy = visitor.visit(mock(OpenAPI.class), operationMock);
        assertTrue(policy.isPresent());
        String configuration = policy.get().getConfiguration();
        assertNotNull(configuration);
        HashMap readConfig = new ObjectMapper().readValue(configuration, HashMap.class);
        assertEquals(extensions.get(RestToSoapOAIOperationVisitor.SOAP_EXTENSION_ENVELOPE), readConfig.get("envelope"));
        assertNull(extensions.get(RestToSoapOAIOperationVisitor.SOAP_EXTENSION_ACTION));
    }

    @Test
    public void operationWithSoapData() throws Exception {
        Map<String, Object> extensions = new HashMap<>();
        extensions.put(RestToSoapOAIOperationVisitor.SOAP_EXTENSION_ENVELOPE, "<soap:envelope></soap:envelope>");
        extensions.put(RestToSoapOAIOperationVisitor.SOAP_EXTENSION_ACTION, "someaction");
        Operation operationMock = mock(Operation.class);
        when(operationMock.getExtensions()).thenReturn(extensions);
        Optional<Policy> policy = visitor.visit(mock(OpenAPI.class), operationMock);
        assertTrue(policy.isPresent());
        String configuration = policy.get().getConfiguration();
        assertNotNull(configuration);
        HashMap readConfig = new ObjectMapper().readValue(configuration, HashMap.class);
        assertEquals(extensions.get(RestToSoapOAIOperationVisitor.SOAP_EXTENSION_ENVELOPE), readConfig.get("envelope"));
        assertEquals(extensions.get(RestToSoapOAIOperationVisitor.SOAP_EXTENSION_ACTION), readConfig.get("soapAction"));
    }
}
