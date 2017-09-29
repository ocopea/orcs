// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import { action, observable, autorun } from 'mobx';
import DataStore from '../../stores/data-store';
import Request from '../../transportLayer';
import { AppTemplate } from './appTemplate-model';
import AppInstanceHandler from '../AppInstance/appInstance-handler';
import AppTemplateHandler from './appTemplate-handler';
import { hashHistory } from 'react-router';
import Locations from '../../locations.json';
import mockAppTemplates from './mock/mockAppTemplates.json';


class AppTemplateService {

  @observable appTemplates;

  constructor(){
    this.appTemplates = [];
  }

  @action fetchAppTemplates(url) {
    Request(url, {method: 'GET'}, response => {
      const appTemplates = response.map(appTemplate => {
        return new AppTemplate(appTemplate)
      });
      DataStore.receiveAppTemplates(appTemplates);
      AppInstanceHandler.filterInstances(DataStore.testDevInstances);
    }, error => {
      console.log(error);
    });

  }

  deployAppTemplate(url, data) {
    const options = { method: 'POST', data: data, contentType: 'application/json' }
    Request(url, options, response => {
      AppTemplateHandler.fetchAppTemplates();
      hashHistory.push(`${Locations.production.deployingProgress.pathname}/${response}`);
    }, error => { console.log(error) }, true)
  }

  fetchAppTemplateById(url, callback) {
    Request(url, { method: 'GET' }, response => {
      callback(new AppTemplate(response));
    }, error => { callback(error); })
  }

}

const singleton = new AppTemplateService();


autorun(()=>{
  // console.log(singleton.appTemplates)
})

export default singleton;
