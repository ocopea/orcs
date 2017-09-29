// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import AppInstanceHandler from './appInstance-handler';
import { Quota } from '../Quota/quota-model';
import { AppTemplate } from '../AppTemplate/appTemplate-model';
import AppTemplateHandler from '../AppTemplate/appTemplate-handler';
import DataStores from '../../stores/data-store.js';


export class AppInstance {
  id;
  name;
  appTemplateId;
  appTemplateName;
  creatorUserId;
  webEntryPointURL;
  state;
  stateMessage;
  deploymentType;
  dateCreated;

  constructor(appInstance) {
    const appTemplateId =   appInstance.appTemplateId;
    let appTemplate =       AppTemplateHandler.getAppTemplateById(appTemplateId) ||
                            AppTemplateHandler.fetchAppTemplateById(appTemplateId, response => {
                              return response;
                            });                            
    this.id =               appInstance.id;
    this.name =             appInstance.name || appInstance.appInstanceName;
    this.appTemplateId =    appInstance.appTemplateId;
    this.appTemplate =      appTemplate;
    this.creatorUserId =    appInstance.creatorUserId;
    this.webEntryPointURL = appInstance.webEntryPointURL;
    this.state =            appInstance.state;
    this.stateMessage =     appInstance.stateMessage;
    this.deploymentType =   appInstance.deploymentType;
    this.dateCreated =      appInstance.dateCreated;
    this.appTemplate =      appInstance.appTemplate;
  }
}


export class TestDevInstance {
  appInstance;
  quota;
  numberOfAppServices;
  numberOfInfraServices;

  constructor(instance) {
    this.appInstance = instance;
    this.quota = new Quota(instance.quota);
    this.numberOfAppServices = instance.numberOfAppServices;
    this.numberOfInfraServices = instance.numberOfInfraServices;
  }
}

export class DeploymentStateInstance {

  appInstance;
  appServices;
  dataServices;

  constructor(appInstance) {
    this.appInstance =      new AppInstance(appInstance);
    this.appServices =      appInstance.appServices;
    this.dataServices =     appInstance.dataServices;
  }
}

export class DashboardStats {
  appGeography;
  appInstance;
  appQuotaSummary;
  appTemplate;
  copyDistributionSankey;
  copySummary;

  constructor(stats) {
    this.appGeography = new AppGeography(stats.appGeography);
    this.appInstance = new AppInstance(stats.appInstance);
    this.appTemplate = new AppTemplate(stats.appTemplate);
    this.copySummary = new CopySummary(stats.copySummary);
    this.copyDistributionSankey = new CopyDistributionSankey(stats.copyDistributionSankey);
    this.appQuotaSummary = new AppQuotaSummary(stats.appQuotaSummary);
  }
}

class AppGeography {
  locations;

  constructor(appGeography) {
    const locations = appGeography.locations;

    this.locations = locations.map(location => {
      return new AppGeographyLocation(location);
    })
  }
}

class AppGeographyLocation {
  geometry;
  properties;
  type;

  constructor(location) {
    this.geometry = new Geometry(location.geometry);
    this.properties = new Properties(location.properties);
    this.type = location.type;
  }
}

class Geometry {
  coordinates;
  type;

  constructor(geometry) {
    this.type = geometry.type;
    this.coordinates = new Coordinates(geometry.coordinates)
  }
}

class Coordinates {
  lat;
  lng;

  constructor(coordinates) {
    this.lat = coordinates[0];
    this.lng = coordinates[1];
  }
}

class Properties {
  name;
  siteId;

  constructor(property) {
    this.name = property.name;
    this.siteId = property.siteId;
  }
}

class CopySummary {
  offlineBackup;
  production;

  constructor(copySummary) {
    this.offlineBackup = copySummary.offlineBackup;
    this.production = copySummary.production;
  }
}

class CopyDistributionSankey {
  columns;
  sankeyData;

  constructor(copyDistributionSankey) {
    this.columns = copyDistributionSankey.columns.map(copy => {
      return new Column(copy);
    });
    this.sankeyData = copyDistributionSankey.sankeyData;
  }
}

class Column {
  name;
  type;

  constructor(column) {
    this.name = column.name;
    this.type = column.type;
  }
}

class AppQuotaSummary {
  appServiceQuotas;
  dataServiceQuotas;

  constructor(appQuotaSummray) {
    this.appServiceQuotas = appQuotaSummray.appServiceQuotas.map(appServiceQuota => {
      return new QuotaSummary(appServiceQuota);
    });
    this.dataServiceQuotas = appQuotaSummray.dataServiceQuotas.map(dataServiceQuota => {
      return new QuotaSummary(dataServiceQuota);
    });
  }
}

class QuotaSummary {
  name;
  usage;
  usagePercentage;
  usageTotalCapacity;
  usageUnits;

  constructor(appServiceQuota) {
    this.name = appServiceQuota.name;
    this.usage = appServiceQuota.usage;
    this.usagePercentage = appServiceQuota.usagePercentage;
    this.usageTotalCapacity = appServiceQuota.usageTotalCapacity;
    this.usageUnits = appServiceQuota.usageUnits;
  }
}
