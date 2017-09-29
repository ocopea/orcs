// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
export class AppTemplateConfigurationModel {
  appServiceConfigurations = [];
  dataServiceConfigurations = [];

  constructor(appTemplateConfiguration) {
    const serviceConfig = appTemplateConfiguration.appServiceConfigurations;
    const dataConfig = appTemplateConfiguration.dataServiceConfigurations;
    serviceConfig.forEach(serviceConfig => {
      this.appServiceConfigurations.push(new AppServiceConfigurations(serviceConfig));
    });
    dataConfig.forEach(dataConfig => {
      this.dataServiceConfigurations.push(new DataServiceConfigurations(dataConfig));
    });
  }
}

class AppServiceConfigurations {
  appServiceName = '';
  supportedVersions = [];

  constructor(serviceConfig) {
    this.appServiceName = serviceConfig.appServiceName;
    const supportedVersions = serviceConfig.supportedVersions;
    for(let registry in supportedVersions) {
        const version = new Version(supportedVersions[registry], registry);
        this.supportedVersions.push(version);
    }
  }
}

class Version {
  registryName;
  supportedVersions;

  constructor(supportedVersions, registryName) {
    this.registryName = registryName;
    this.supportedVersions = supportedVersions
  }
}

class DataServiceConfigurations {
  dataServiceName = '';
  dsbPlans = [];

  constructor(dataServiceConfig) {
    this.dataServiceName = dataServiceConfig.dataServiceName;
    const dsbPlans = dataServiceConfig.dsbPlans;
    for(let dsbUrn in dsbPlans) {
      const _dsbPlan = dsbPlans[dsbUrn];
      this.dsbPlans.push(new DsbPlan(dsbUrn, _dsbPlan));
    }
  }
}

class DsbPlan {
  dsbUrn;
  name;
  description;
  plans = [];
  protocols = [];

  constructor(dsbUrn, dsbPlan) {
    this.dsbUrn = dsbUrn;
    this.name = dsbPlan.name;
    this.description = dsbPlan.description;
    this.plans = dsbPlan.plans.map(p => {
      return new Plan(p);
    });
    this.protocols = dsbPlan.protocols;
  }
}

class Plan {
  id;
  name;
  description;
  protocols;

  constructor(plan) {
    this.id = plan.id;
    this.name = plan.name;
    this.description = plan.description;
    this.protocols = plan.protocols;
  }
}
