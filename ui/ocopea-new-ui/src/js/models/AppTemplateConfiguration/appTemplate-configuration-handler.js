// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import { action, observable, autorun } from 'mobx';
import appTemplateConfigurationApi from './appTemplate-configuration-api';
import appTemplateConfigurationService from './appTemplate-configuration-service';
// import userService from './user-service';


class AppTemplateConfigurationHandler {

  @observable configuration;

  constructor(){

  }

  fetchAppTemplateConfig(siteId, appTemplateId, hideLoader) {
    const url = appTemplateConfigurationApi.configuration(siteId, appTemplateId);
    appTemplateConfigurationService.fetchAppTemplateConfig(url, hideLoader);
  }

}

const singleton = new AppTemplateConfigurationHandler();

autorun(()=>{
  // console.log(singleton.configuration)
})

export default singleton;
