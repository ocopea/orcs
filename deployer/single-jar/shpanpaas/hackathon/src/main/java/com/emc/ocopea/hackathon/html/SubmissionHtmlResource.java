// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hackathon.html;

import com.emc.microservice.Context;
import com.emc.microservice.MicroServiceApplication;
import com.emc.microservice.blobstore.BlobStoreAPI;
import com.emc.microservice.datasource.MicroServiceDataSource;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.emc.ocopea.hackathon.idea.HackathonIdeaService;
import com.emc.ocopea.hackathon.idea.IdeaForSubmission;
import com.emc.ocopea.hackathon.idea.SubmittedIdea;
import com.emc.ocopea.hackathon.idea.SubmittedIdeaStatus;
import com.emc.ocopea.util.database.BasicNativeQueryService;
import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.UUID;

public class SubmissionHtmlResource implements SubmissionHtmlWebApi {

    private static final Logger log = LoggerFactory.getLogger(SubmissionHtmlResource.class);
    private HackathonIdeaService hackathonIdeaService;
    private BlobStoreAPI docsBlobStore;

    @NoJavadoc
    @javax.ws.rs.core.Context
    public void setApplication(Application application) {
        Context context = ((MicroServiceApplication) application).getMicroServiceContext();
        MicroServiceDataSource dataSource =
                context.getDatasourceManager().getManagedResourceByName("hackathon-db").getDataSource();
        hackathonIdeaService = new HackathonIdeaService(
                context.getParametersBag().getBoolean("hackathon-pro-mode"),
                new BasicNativeQueryService(dataSource));

        docsBlobStore = context.getBlobStoreManager().getManagedResourceByName("hack-docs").getBlobStoreAPI();
    }

    private Response readStaticResource(final String resourceName) {
        StreamingOutput streamingOutput = new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(resourceName);
                IOUtils.copy(resourceAsStream, output);
            }
        };

        return Response.ok(streamingOutput).build();
    }

    @Override
    public Response submitNewIdea(MultipartFormDataInput input) {
        final SubmittedIdeaStatus submittedIdeaStatus = submitIdeaNow(input);
        return Response
                .created(URI.create("idea/" + submittedIdeaStatus.getId()))
                .build();
    }

    @Override
    public Response getIdeaDoc(@PathParam("ideaId") UUID ideaId) {
        final SubmittedIdea submittedIdea = hackathonIdeaService.get(ideaId);

        //Put some validations here such as invalid file name or missing file name
        if (submittedIdea == null) {
            Response.ResponseBuilder response = Response.status(Response.Status.BAD_REQUEST);
            return response.build();
        }

        //Prepare a file object with file to return
        Response.ResponseBuilder response = Response.ok((StreamingOutput) outputStream ->
                docsBlobStore.readBlob("DOCS", submittedIdea.getDocKey(), outputStream));
        //Response.ResponseBuilder response = Response.ok((StreamingOutput) outputStream ->
        //        IOUtils.copy(getClass().getResourceAsStream("/hodapp.jpg"), outputStream));
        //Response.ResponseBuilder response = Response.ok((StreamingOutput) outputStream ->
        //        IOUtils.copy(getClass().getResourceAsStream("/dell.jpg"), outputStream));
        return response
                .header("Content-Disposition", "attachment; filename=\"" + submittedIdea.getDocName() + "\"")
                .build();

    }

    @Override
    public Response getNUIResource(@PathParam("path") String path) {
        return readStaticResource("nui/" + path);
    }

    @Override
    public Response welcomeNUI() {
        return readStaticResource("nui/index.html");
    }

    private SubmittedIdeaStatus submitIdeaNow(MultipartFormDataInput input) {
        try {
            log.info("Uploading");
            String name = input.getFormDataMap().get("ideaName").get(0).getBodyAsString();
            String description = input.getFormDataMap().get("ideaDesc").get(0).getBodyAsString();
            String fileInfo = input.getFormDataMap().get("ideaDoc").get(0).getHeaders().getFirst("Content-Disposition");
            String fileName =
                    fileInfo.substring(fileInfo.indexOf("filename=") + "filename=".length() + 1, fileInfo.length() - 1);
            InputStream ideaDoc = input.getFormDataMap().get("ideaDoc").get(0).getBody(InputStream.class, null);

            String docKey = UUID.randomUUID().toString();
            docsBlobStore.create("DOCS", docKey, null, ideaDoc);

            IdeaForSubmission ideaForSubmission = new IdeaForSubmission(name, description, fileName, docKey);
            return hackathonIdeaService.submitIdea(ideaForSubmission);

        } catch (IOException e) {
            throw new WebApplicationException("Error reading idea", e);
        }
    }

}
