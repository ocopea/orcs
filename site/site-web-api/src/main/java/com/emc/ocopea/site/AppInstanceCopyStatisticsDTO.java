// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site;

import java.util.Collection;
import java.util.Date;
import java.util.UUID;

/**
 * Created by liebea on 1/10/17.
 * Drink responsibly
 */
public class AppInstanceCopyStatisticsDTO {

    public enum SiteAppCopyState {
        creating,
        created,
        failed
    }

    private final UUID copyId;
    private final Date timeStamp;
    private final long size;
    private final SiteAppCopyState state;
    private final Collection<AppInstanceStatisticsDTO.DataServiceCopyStatisticsDTO> dataServiceCopies;
    private final Collection<AppInstanceStatisticsDTO.AppServiceCopyStatisticsDTO> appServiceCopies;

    private AppInstanceCopyStatisticsDTO() {
        this(null, null, 0L, null, null, null);
    }

    public AppInstanceCopyStatisticsDTO(
            UUID copyId,
            Date timeStamp,
            long size,
            SiteAppCopyState state,
            Collection<AppInstanceStatisticsDTO.DataServiceCopyStatisticsDTO> dataServiceCopies,
            Collection<AppInstanceStatisticsDTO.AppServiceCopyStatisticsDTO> appServiceCopies) {

        this.copyId = copyId;
        this.timeStamp = timeStamp;
        this.size = size;
        this.state = state;
        this.dataServiceCopies = dataServiceCopies;
        this.appServiceCopies = appServiceCopies;
    }

    public UUID getCopyId() {
        return copyId;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public long getSize() {
        return size;
    }

    public SiteAppCopyState getState() {
        return state;
    }

    public Collection<AppInstanceStatisticsDTO.DataServiceCopyStatisticsDTO> getDataServiceCopies() {
        return dataServiceCopies;
    }

    public Collection<AppInstanceStatisticsDTO.AppServiceCopyStatisticsDTO> getAppServiceCopies() {
        return appServiceCopies;
    }

    @Override
    public String toString() {
        return "AppInstanceCopyStatisticsDTO{" +
                "copyId=" + copyId +
                ", timeStamp=" + timeStamp +
                ", size=" + size +
                ", state='" + state + '\'' +
                ", dataServiceCopies=" + dataServiceCopies +
                ", appServiceCopies=" + appServiceCopies +
                '}';
    }
}
