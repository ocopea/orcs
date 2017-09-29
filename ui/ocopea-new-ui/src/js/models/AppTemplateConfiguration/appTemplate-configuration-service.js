// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import { action, observable, autorun } from 'mobx';
import UiStore from '../../stores/ui-store';
import { AppTemplateConfigurationModel } from '../../models/AppTemplateConfiguration/appTemplate-configuration-model';
import AppTemplateConfigurationHandler from '../../models/AppTemplateConfiguration/appTemplate-configuration-handler';
import Request from '../../transportLayer';


class AppTemplateConfigurationService {

  constructor(){

  }

  fetchAppTemplateConfig(url, hideLoader) {
    Request(url, {method: 'GET'}, response => {
    AppTemplateConfigurationHandler.configuration = new AppTemplateConfigurationModel(response);
  }, error => { console.log(error) }, hideLoader)
  }

}

const singleton = new AppTemplateConfigurationService();

autorun(()=>{
  // console.log(singleton.appTemplates)
})

export default singleton;
