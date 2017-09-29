// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hackathon.idea;

import com.emc.microservice.Context;
import com.emc.microservice.MicroServiceApplication;
import com.emc.microservice.datasource.MicroServiceDataSource;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.emc.ocopea.util.database.BasicNativeQueryService;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Created by liebea on 5/29/15.
 * Drink responsibly
 */
public class SubmitIdeaResource implements SubmitIdeaWebAPI {
    private HackathonIdeaService hackathonIdeaService;

    @NoJavadoc
    @javax.ws.rs.core.Context
    public void setApplication(Application application) {

        Context context = ((MicroServiceApplication) application).getMicroServiceContext();
        MicroServiceDataSource dataSource = context.getDatasourceManager()
                .getManagedResourceByName("hackathon-db").getDataSource();

        hackathonIdeaService = new HackathonIdeaService(
                context.getParametersBag().getBoolean("hackathon-pro-mode"),
                new BasicNativeQueryService(dataSource));
    }

    @Override
    public SubmittedIdeaStatus submit(IdeaForSubmission ideaForSubmission) {
        return hackathonIdeaService.submitIdea(ideaForSubmission);
    }

    @Override
    public Collection<SubmittedIdea> list() {
        return hackathonIdeaService.list()
                .stream()
                /*
                .map(i -> new SubmittedIdea(
                        i.getId(),
                        "Michael Dell",
                        i.getDescription(),
                        i.getDocName(),
                        i.getDocKey(),
                        i.getStatus(),
                        i.getVotes()))
                */
                .collect(Collectors.toList());
    }

    @Override
    public String version() {
        return "\"" + getClass().getPackage().getImplementationVersion() + "\"";
    }

    @Override
    public Response vote(VoteInfo voteInfo) {
        hackathonIdeaService.vote(voteInfo.getIdeaId());
        return Response.ok("Vote Received!").build();
    }
}
