// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import appTemplateService from './appTemplate-service';
import appTemplateApi from './appTemplate-api';
import DataStore from '../../stores/data-store';

export default class AppTemplateHandler {

  static fetchAppTemplates() {
    return appTemplateService.fetchAppTemplates(appTemplateApi.appTemplates);
  }

  static deployAppTemplate(data) {
    appTemplateService.deployAppTemplate(appTemplateApi.deploy, data);
  }

  static deployAppTemplate(data) {
    appTemplateService.deployAppTemplate(appTemplateApi.deploy, data);
  }

  static getAppTemplateById(id) {
    return DataStore.appTemplatesMap[id];
  }

  static fetchAppTemplateById(id, callback) {
    appTemplateService.fetchAppTemplateById(appTemplateApi.appTemplate(id), callback);
  }
}
