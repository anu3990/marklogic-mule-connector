/**
 * MarkLogic Mule Connector
 *
 * Copyright © 2023 MarkLogic Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * This project and its code and functionality is not representative of MarkLogic Server and is not supported by MarkLogic.
 */
package com.marklogic.mule.extension.connector.internal.error;

import com.marklogic.mule.extension.connector.internal.error.provider.MarkLogicExecuteErrorsProvider;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mule.runtime.extension.api.error.ErrorTypeDefinition;

/**
 *
 * @author jshingle
 */
public class MarkLogicExecuteErrorsProviderTest
{

    /**
     * Test of getErrorTypes method, of class MarkLogicExecuteErrorsProvider.
     */
    @Test
    public void testGetErrorTypes()
    {
        MarkLogicExecuteErrorsProvider instance = new MarkLogicExecuteErrorsProvider();
        Set<ErrorTypeDefinition> result = instance.getErrorTypes();

        assertEquals(1, result.size());
        assertTrue(result.contains(MarkLogicConnectorSimpleErrorType.DATA_MOVEMENT_ERROR));
    }

}
