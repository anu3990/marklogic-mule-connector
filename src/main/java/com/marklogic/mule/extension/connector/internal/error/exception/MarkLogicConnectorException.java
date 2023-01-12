/**
 * MarkLogic Mule Connector
 *
 * Copyright © 2021 MarkLogic Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * This project and its code and functionality is not representative of MarkLogic Server and is not supported by MarkLogic.
 */
package com.marklogic.mule.extension.connector.internal.error.exception;

import com.marklogic.mule.extension.connector.internal.error.MarkLogicConnectorSimpleErrorType;
import org.mule.runtime.extension.api.exception.ModuleException;

// sonarqube is disappointed that this has 6 parent classes instead of the max of 5, but that is due to ModuleException's
// hierarchy and does not seem avoidable if a class needs to extend ModuleException
@SuppressWarnings("java:S110")
public class MarkLogicConnectorException extends ModuleException
{

    public MarkLogicConnectorException(String errorMessge)
    {
        super(errorMessge, MarkLogicConnectorSimpleErrorType.DATA_MOVEMENT_ERROR);
    }

    public MarkLogicConnectorException(String errorMessage, Throwable error)
    {
        super(errorMessage, MarkLogicConnectorSimpleErrorType.DATA_MOVEMENT_ERROR, error);
    }
}
