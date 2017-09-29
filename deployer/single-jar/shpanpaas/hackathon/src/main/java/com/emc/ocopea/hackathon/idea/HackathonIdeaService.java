// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hackathon.idea;

import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.emc.ocopea.util.database.NativeQueryException;
import com.emc.ocopea.util.database.NativeQueryRowBatchConverter;
import com.emc.ocopea.util.database.NativeQueryService;

import javax.ws.rs.BadRequestException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Created by liebea on 5/29/15.
 * Drink responsibly
 */
public class HackathonIdeaService {
    private static final String SQL_INSERT_IDEA =
            "insert into ideas (id, name, description, docName, docKey, status, votes) values (?,?,?,?,?,?,0)";
    private static final String SQL_INCREASE_VOTES =
            "update ideas set votes = votes + 1 where id = ?";
    private final boolean proMode;
    private final NativeQueryService nqs;

    public HackathonIdeaService(boolean proMode, NativeQueryService nqs) {
        this.proMode = proMode;
        this.nqs = nqs;
    }

    @NoJavadoc
    public SubmittedIdeaStatus submitIdea(IdeaForSubmission ideaForSubmission) {
        UUID ideaId = UUID.randomUUID();

        // In pro mode we have approval process, in non-pro all ideas are approved automatically!
        IdeaStatusEnum initialStatus = proMode ?
                IdeaStatusEnum.submitted :
                IdeaStatusEnum.approved;

        nqs.executeUpdate(
                SQL_INSERT_IDEA,
                Arrays.<Object>asList(
                        ideaId.toString(),
                        ideaForSubmission.getName(),
                        ideaForSubmission.getDescription(),
                        ideaForSubmission.getDocName(),
                        ideaForSubmission.getDocKey(),
                        initialStatus.toString()));

        return new SubmittedIdeaStatus(ideaId.toString(), initialStatus);
    }

    public SubmittedIdea get(UUID id) {
        return nqs.getSingleValue(
                "select * from ideas where id = ?",
                new SubmittedIdeaINativeQueryConverter(),
                Collections.singletonList(id.toString()));
    }

    public List<SubmittedIdea> list() {
        return nqs.getList("select * from ideas", new SubmittedIdeaINativeQueryConverter());
    }

    public List<SubmittedIdea> listByStatus(IdeaStatusEnum status) {
        return nqs.getList(
                "select * from ideas where status=?",
                new SubmittedIdeaINativeQueryConverter(),
                Collections.singletonList(status.toString()));
    }

    /***
     * Rejecting submitted idea.
     */
    public void rejectIdea(UUID ideaId) {
        nqs.executeUpdate(
                "update ideas set status=? where id=? and status=?",
                Arrays.asList(
                        IdeaStatusEnum.rejected.toString(),
                        ideaId.toString(),
                        IdeaStatusEnum.submitted.toString()
                ));
    }

    /***
     * Approving a submitted idea.
     */
    public void approveIdea(UUID ideaId) {
        nqs.executeUpdate(
                "update ideas set status=? where id=? and status=?",
                Arrays.asList(
                        IdeaStatusEnum.approved.toString(),
                        ideaId.toString(),
                        IdeaStatusEnum.submitted.toString()
                ));
    }

    public void vote(UUID ideaId) {
        final SubmittedIdea submittedIdea = get(ideaId);
        if (submittedIdea.getStatus() != IdeaStatusEnum.approved) {
            throw new BadRequestException("Only approved ideas can be voted for");
        }
        nqs.executeUpdate(SQL_INCREASE_VOTES, Collections.singletonList(ideaId.toString()));
    }

    private static class SubmittedIdeaINativeQueryConverter implements NativeQueryRowBatchConverter<SubmittedIdea> {
        @Override
        public SubmittedIdea convertRow(ResultSet resultSet, int i) throws SQLException {
            return new SubmittedIdea(
                    UUID.fromString(resultSet.getString(1)),
                    resultSet.getString("name"),
                    resultSet.getString("description"),
                    resultSet.getString("docName"),
                    resultSet.getString("docKey"),
                    IdeaStatusEnum.valueOf(resultSet.getString("status")),
                    resultSet.getLong("votes")
            );
        }

        @Override
        public void finalizeBatch(Collection<SubmittedIdea> collection) throws NativeQueryException {
        }
    }
}
