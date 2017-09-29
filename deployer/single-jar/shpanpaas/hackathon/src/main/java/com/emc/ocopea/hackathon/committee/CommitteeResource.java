// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hackathon.committee;

import com.emc.microservice.Context;
import com.emc.microservice.MicroServiceApplication;
import com.emc.microservice.datasource.MicroServiceDataSource;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.emc.ocopea.hackathon.idea.HackathonIdeaService;
import com.emc.ocopea.hackathon.idea.IdeaStatusEnum;
import com.emc.ocopea.hackathon.idea.SubmittedIdea;
import com.emc.ocopea.util.database.BasicNativeQueryService;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Created by liebea on 3/1/17.
 * Drink responsibly
 */
public class CommitteeResource implements CommitteeWebApi {

    private HackathonIdeaService hackathonIdeaService;

    @NoJavadoc
    @javax.ws.rs.core.Context
    public void setApplication(Application application) {
        Context context = ((MicroServiceApplication) application).getMicroServiceContext();

        MicroServiceDataSource dataSource =
                context.getDatasourceManager().getManagedResourceByName("hackathon-db").getDataSource();

        hackathonIdeaService = new HackathonIdeaService(
                true,
                new BasicNativeQueryService(dataSource));
    }

    @Override
    public Response review(IdeaReviewStatus ideaReviewStatus) {
        if (ideaReviewStatus.isApproved()) {
            hackathonIdeaService.approveIdea(ideaReviewStatus.getIdeaId());
        } else {
            hackathonIdeaService.rejectIdea(ideaReviewStatus.getIdeaId());
        }

        return Response.ok().build();
    }

    @Override
    public Collection<SubmittedIdea> list() {
        return hackathonIdeaService.listByStatus(IdeaStatusEnum.submitted)
                .stream()
                .collect(Collectors.toList());
    }

}
