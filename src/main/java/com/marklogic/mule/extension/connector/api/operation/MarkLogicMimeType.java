/**
 * MarkLogic Mule Connector
 * <p>
 * Copyright © 2021 MarkLogic Corporation.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 * <p>
 * This project and its code and functionality is not representative of MarkLogic Server and is not supported by MarkLogic.
 */
package com.marklogic.mule.extension.connector.api.operation;

// sonarqube wants these to be uppercase, but cannot change them in the 1.x timeline since they're part of the
// public API
@SuppressWarnings("java:S115")
public enum MarkLogicMimeType {

    xml,
    json,
    text,
    binary;

}
