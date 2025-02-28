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
package com.marklogic.mule.extension.connector.internal.operation;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.datamovement.DataMovementManager;
import com.marklogic.client.datamovement.JobTicket;
import com.marklogic.client.datamovement.WriteBatcher;
import com.marklogic.client.document.ServerTransform;
import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.client.io.InputStreamHandle;
import org.mule.runtime.api.scheduler.SchedulerService;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.weave.v2.model.structure.Attributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Created by jkrebs on 9/12/2018. Singleton class that manages inserting
 * documents into MarkLogic
 */
public class MarkLogicInsertionBatcher implements MarkLogicConnectionInvalidationListener
{
    private static final Logger LOGGER = LoggerFactory.getLogger(MarkLogicInsertionBatcher.class);

    // a hash used internally to uniquely identify the batcher based on its current configuration
    private final int signature;

    // Object that describes the metadata for documents being inserted
    private DocumentMetadataHandle metadataHandle;

    // How will we know when the resources are ready to be freed up and provide the results report?
    private JobTicket jobTicket;

    private WriteBatcher batcher;

    private DataMovementManager dmm;

    private boolean batcherRequiresReinit;

    private SchedulerService schedulerService;

    /**
     * Creates a new insertion batcher.
     *
     * @param context captures inputs and context for the insertion process
     */
    public MarkLogicInsertionBatcher(InsertionBatcherContext context, SchedulerService schedulerService)
    {
        this.schedulerService = schedulerService;
        this.batcherRequiresReinit = false;
        LOGGER.debug("MarkLogicInsertionBatcher batcherRequiresReinit {}", batcherRequiresReinit);
        this.signature = context.computeSignature();

        // get the object handles needed to talk to MarkLogic
        initializeBatcher(context);
        LOGGER.info("MarkLogicInsertionBatcher with job name: {}", context.getJobName());
    }

    private void initializeBatcher(InsertionBatcherContext context)
    {
        context.getConnection().addMarkLogicClientInvalidationListener(this);
        DatabaseClient myClient = context.getConnection().getClient();
        dmm = myClient.newDataMovementManager();
        batcher = dmm.newWriteBatcher();
        batcher.withBatchSize(context.getConfiguration().getBatchSize())
                .withThreadCount(context.getConfiguration().getThreadCount())
                .onBatchSuccess(batch -> LOGGER.info("Batcher with signature {} on connection ID {} writes so far: {}",
                    getSignature(), context.getConnection().getId(), batch.getJobWritesSoFar()))
                .onBatchFailure((batch, throwable) -> LOGGER.error("Exception thrown by an onBatchSuccess listener", throwable));

        // Configure the transform to be used, if any
        // ASSUMPTION: The same transform (or lack thereof) will be used for every document to be inserted during the
        // lifetime of this object

        final String temporalCollection = context.getTemporalCollection();
        if (temporalCollection != null && !"null".equalsIgnoreCase(temporalCollection))
        {
            LOGGER.info("TEMPORAL COLLECTION: {}", temporalCollection);
            batcher.withTemporalCollection(temporalCollection);
        }

        Optional<ServerTransform> transform = context.getConfiguration().generateServerTransform(
            context.getServerTransform(), context.getServerTransformParams());
        if(transform.isPresent())
        {
            batcher.withTransform(transform.get());
        }

        scheduleThreadToFlushBatcher(context);

        // Set up the metadata to be used for the documents that will be inserted
        // ASSUMPTION: The same metadata will be used for every document to be inserted during the lifetime of this
        // object
        this.metadataHandle = new DocumentMetadataHandle();
        String[] configCollections = context.getOutputCollections().split(",");

        // Set up list of collections that new docs should be put into
        if (!configCollections[0].equals("null"))
        {
            metadataHandle.withCollections(configCollections);
        }
        // Set up quality new docs should have
        metadataHandle.setQuality(context.getOutputQuality());

        // Set up list of permissions that new docs should be granted
        String[] permissions = context.getOutputPermissions().split(",");
        for (int i = 0; i < permissions.length - 1; i++)
        {
            String role = permissions[i];
            String capability = permissions[i + 1];
            switch (capability.toLowerCase())
            {
                case "read":
                    metadataHandle.getPermissions().add(role, DocumentMetadataHandle.Capability.READ);
                    break;
                case "insert":
                    metadataHandle.getPermissions().add(role, DocumentMetadataHandle.Capability.INSERT);
                    break;
                case "update":
                    metadataHandle.getPermissions().add(role, DocumentMetadataHandle.Capability.UPDATE);
                    break;
                case "execute":
                    metadataHandle.getPermissions().add(role, DocumentMetadataHandle.Capability.EXECUTE);
                    break;
                case "node_update":
                    metadataHandle.getPermissions().add(role, DocumentMetadataHandle.Capability.NODE_UPDATE);
                    break;
                default:
                    LOGGER.info("No additive permissions assigned");
            }
        }

        // start the batcher job
        this.jobTicket = dmm.startJob(batcher);
    }

    /**
     * Documents can get "stuck" in the WriteBatcher when not enough are received to meet the batch size.
     *
     * @param context
     */
    private void scheduleThreadToFlushBatcher(InsertionBatcherContext context) {
        // The service will be null in unit tests that don't inject a SchedulerService.
        // Need this toString check as a bit of a dirty hack to prevent executeDeleteDocsStructuredQueryFlow from
        // failing when it tries to dispose of the Mule context.
        if (this.schedulerService != null && !this.schedulerService.toString().contains("SimpleUnitTestSupportSchedulerService")) {
            int secondsBeforeFlush = context.getConfiguration().getSecondsBeforeFlush();
            LOGGER.info("Scheduling thread to flush batcher; will run every {} seconds", secondsBeforeFlush);
            // There's no real penalty to calling flushAsync repeatedly; if there are no documents waiting to be
            // written, the cost of calling flushAsync is negligible.
            this.schedulerService.ioScheduler().scheduleAtFixedRate(() -> {
                if (batcher != null && !batcher.isStopped()) {
                    batcher.flushAsync();
                }
            }, secondsBeforeFlush, secondsBeforeFlush, TimeUnit.SECONDS);
        }
    }

    public void release() {
        if (batcher != null) {
            // finalize all writes
            batcher.flushAndWait();
            dmm.stopJob(this.jobTicket);
        }
    }

    public int getSignature() {
        return this.signature;
    }

    /**
     * Actually does the work of passing the document on to DMSDK to do its
     * thing
     *
     * @param outURI -- the URI to be used for the document being inserted
     * @param documentStream -- the InputStream containing the document to be inserted...comes from Mule
     * @return jobTicketID
     */
    InputStream doInsert(String outURI, InputStream documentStream)
    {
        // Add the InputStream to the DMSDK WriteBatcher object
        batcher.addAs(outURI, metadataHandle, new InputStreamHandle(documentStream));

        // Return the job ticket ID so it can be used to retrieve the document in the future
        String jsonout = "\"" + jobTicket.getJobId() + "\"";
        LOGGER.debug("importDocs getJobId outcome: {}", jsonout);
        
        Charset cs = StandardCharsets.UTF_8;
        return new ByteArrayInputStream(jsonout.getBytes(cs));
    }

    @Override
    public void markLogicConnectionInvalidated()
    {
        LOGGER.info("MarkLogic connection invalidated... reinitializing insertion batcher...");
        batcherRequiresReinit = true;
    }
}