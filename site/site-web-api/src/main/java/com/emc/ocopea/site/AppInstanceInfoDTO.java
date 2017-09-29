// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site;

import com.emc.ocopea.site.app.DeployedAppServiceState;
import com.emc.ocopea.site.app.DeployedApplicationState;
import com.emc.ocopea.site.app.DeployedDataServiceState;

import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

/**
 * Created by liebea on 4/18/16.
 * Drink responsibly
 */
public class AppInstanceInfoDTO {
    private final UUID id;
    private final String name;
    private final String templateName;
    private final String templateVersion;
    private final DeployedApplicationState state;
    private final String stateMessage;
    private final Date stateDate;
    private final Date launched;
    private final String entryPointURL;
    private final Collection<AppServiceInfoDTO> appServices;
    private final Collection<DataServiceInfoDTO> dataServices;

    private AppInstanceInfoDTO() {
        this(null, null, null, null, null, null, null, null, null, null, null);
    }

    public AppInstanceInfoDTO(UUID id,
                              String name,
                              String templateName,
                              String templateVersion,
                              DeployedApplicationState state,
                              String stateMessage,
                              Date stateDate,
                              Date launched,
                              String entryPointURL,
                              Collection<AppServiceInfoDTO> appServices,
                              Collection<DataServiceInfoDTO> dataServices) {
        this.id = id;
        this.name = name;
        this.templateName = templateName;
        this.templateVersion = templateVersion;
        this.state = state;
        this.stateMessage = stateMessage;
        this.stateDate = stateDate;
        this.launched = launched;
        this.entryPointURL = entryPointURL;
        this.appServices = appServices;
        this.dataServices = dataServices;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTemplateName() {
        return templateName;
    }

    public String getTemplateVersion() {
        return templateVersion;
    }

    public DeployedApplicationState getState() {
        return state;
    }

    public String getStateMessage() {
        return stateMessage;
    }

    public Date getStateDate() {
        return stateDate;
    }

    public Date getLaunched() {
        return launched;
    }

    public String getEntryPointURL() {
        return entryPointURL;
    }

    public Collection<AppServiceInfoDTO> getAppServices() {
        return appServices;
    }

    public Collection<DataServiceInfoDTO> getDataServices() {
        return dataServices;
    }

    @Override
    public String toString() {
        return "AppInstanceInfoDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", templateName='" + templateName + '\'' +
                ", templateVersion='" + templateVersion + '\'' +
                ", state=" + state +
                ", stateMessage='" + stateMessage + '\'' +
                ", stateDate=" + stateDate +
                ", launched=" + launched +
                ", entryPointURL='" + entryPointURL + '\'' +
                ", appServices=" + appServices +
                ", dataServices=" + dataServices +
                '}';
    }

    public static class AppServiceInfoDTO {
        private final String serviceName;
        private final String imageName;
        private final String imageType;
        private final String imageVersion;
        private final String publicUrl;
        private final DeployedAppServiceState state;
        private final String stateMessage;
        private final Date stateDate;
        private final Set<String> serviceBindings;

        private AppServiceInfoDTO() {
            this(null, null, null, null, null, null, null, null, null);
        }

        public AppServiceInfoDTO(String serviceName,
                                 String imageName,
                                 String imageType,
                                 String imageVersion,
                                 String publicUrl,
                                 DeployedAppServiceState state,
                                 String stateMessage,
                                 Date stateDate,
                                 Set<String> serviceBindings) {

            this.serviceName = serviceName;
            this.imageName = imageName;
            this.imageType = imageType;
            this.imageVersion = imageVersion;
            this.publicUrl = publicUrl;
            this.state = state;
            this.stateMessage = stateMessage;
            this.stateDate = stateDate;
            this.serviceBindings = serviceBindings;
        }

        public String getServiceName() {
            return serviceName;
        }

        public String getImageName() {
            return imageName;
        }

        public String getImageType() {
            return imageType;
        }

        public String getImageVersion() {
            return imageVersion;
        }

        public String getPublicUrl() {
            return publicUrl;
        }

        public DeployedAppServiceState getState() {
            return state;
        }

        public String getStateMessage() {
            return stateMessage;
        }

        public Date getStateDate() {
            return stateDate;
        }

        public Set<String> getServiceBindings() {
            return serviceBindings;
        }

        @Override
        public String toString() {
            return "AppServiceInfoDTO{" +
                    "serviceName='" + serviceName + '\'' +
                    ", imageName='" + imageName + '\'' +
                    ", imageType='" + imageType + '\'' +
                    ", imageVersion='" + imageVersion + '\'' +
                    ", publicUrl='" + publicUrl + '\'' +
                    ", state=" + state +
                    ", stateMessage='" + stateMessage + '\'' +
                    ", stateDate=" + stateDate +
                    ", serviceBindings=" + serviceBindings +
                    '}';
        }
    }

    public static class DataServiceInfoDTO {
        private final String bindName;
        private final String dsbURN;
        private final String serviceId;
        private final DeployedDataServiceState state;
        private final String stateMessage;
        private final Date stateDate;

        private DataServiceInfoDTO() {
            this(null, null, null, null, null, null);
        }

        public DataServiceInfoDTO(String bindName, String dsbURN, String serviceId, DeployedDataServiceState state,
                                  String stateMessage, Date stateDate) {
            this.bindName = bindName;
            this.dsbURN = dsbURN;
            this.serviceId = serviceId;
            this.state = state;
            this.stateMessage = stateMessage;
            this.stateDate = stateDate;
        }

        public String getBindName() {
            return bindName;
        }

        public String getDsbURN() {
            return dsbURN;
        }

        public String getServiceId() {
            return serviceId;
        }

        public DeployedDataServiceState getState() {
            return state;
        }

        public String getStateMessage() {
            return stateMessage;
        }

        public Date getStateDate() {
            return stateDate;
        }

        @Override
        public String toString() {
            return "DataServiceInfoDTO{" +
                    "bindName='" + bindName + '\'' +
                    ", dsbURN='" + dsbURN + '\'' +
                    ", serviceId=" + serviceId +
                    ", state=" + state +
                    ", stateMessage='" + stateMessage + '\'' +
                    ", stateDate=" + stateDate +
                    '}';
        }
    }

}
