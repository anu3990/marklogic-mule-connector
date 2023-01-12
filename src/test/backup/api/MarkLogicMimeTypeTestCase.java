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
package com.marklogic.api;

import static org.hamcrest.CoreMatchers.instanceOf;
import com.marklogic.mule.extension.connector.api.operation.MarkLogicMimeType;
import com.marklogic.mule.extension.connector.internal.result.resultset.MarkLogicBinaryRecordExtractor;
import com.marklogic.mule.extension.connector.internal.result.resultset.MarkLogicXMLRecordExtractor;
import com.marklogic.mule.extension.connector.internal.result.resultset.MarkLogicJSONRecordExtractor;
import com.marklogic.mule.extension.connector.internal.result.resultset.MarkLogicTextRecordExtractor;

import org.junit.Test;
import static org.junit.Assert.*;

public class MarkLogicMimeTypeTestCase
{
    @Test
    public void testXml() {
        MarkLogicMimeType type = MarkLogicMimeType.fromString("application/xml");
        Assert.assertEquals(MarkLogicMimeType.xml, type);
        Assert.assertNotNull(type.getRecordExtractor());
        Assert.assertThat(type.getRecordExtractor(), CoreMatchers.instanceOf(MarkLogicXMLRecordExtractor.class));
    }
    
    @Test
    public void testJson() {
        MarkLogicMimeType type = MarkLogicMimeType.fromString("application/json");
        Assert.assertEquals(MarkLogicMimeType.json, type);
        Assert.assertNotNull(type.getRecordExtractor());
        Assert.assertThat(type.getRecordExtractor(), CoreMatchers.instanceOf(MarkLogicJSONRecordExtractor.class));
    }

    @Test
    public void testText() {
        MarkLogicMimeType type = MarkLogicMimeType.fromString("application/text");
        Assert.assertEquals(MarkLogicMimeType.text, type);
        Assert.assertNotNull(type.getRecordExtractor());
        Assert.assertThat(type.getRecordExtractor(), CoreMatchers.instanceOf(MarkLogicTextRecordExtractor.class));
    }

    @Test
    public void testBinary() {
        MarkLogicMimeType type = MarkLogicMimeType.fromString(null);
        Assert.assertEquals(MarkLogicMimeType.binary, type);
        Assert.assertNotNull(type.getRecordExtractor());
        Assert.assertThat(type.getRecordExtractor(), CoreMatchers.instanceOf(MarkLogicBinaryRecordExtractor.class));
    }
}