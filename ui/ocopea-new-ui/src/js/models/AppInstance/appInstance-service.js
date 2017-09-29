// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import { action, observable, autorun } from 'mobx';
import DataStore from '../../stores/data-store';
import { AppInstance } from '../../models/AppInstance/appInstance-model';
import { TestDevInstance } from '../../models/AppInstance/appInstance-model';
import mockAppInstances from '../../models/AppInstance/mock/mockAppInstances';
import mockTestDevInstances from '../../models/AppInstance/mock/mockTestDevInstances';
import { DeploymentStateInstance, DashboardStats } from '../../models/AppInstance/appInstance-model';
import AppInstanceHandler from './appInstance-handler';
import AppInstanceApi from './appInstance-api';
import AppTemplateHandler from '../AppTemplate/appTemplate-handler';
import Request from '../../transportLayer';


class AppInstanceService {

  fetchAppInstance(url) {
    Request(url, {method: 'GET'}, response => {
      const appInstances = response.map(appInstance => {
        const appTemplate = AppTemplateHandler.getAppTemplateById(appInstance.appTemplateId);
        appInstance.appTemplate = appTemplate;
        return new AppInstance(appInstance);
      });
      DataStore.receiveAppInstances(appInstances)
    }, error => {
      console.log(error)
    })
  }

  fetchState(url) {
    Request(url, {method: 'GET'}, response => {
      AppInstanceHandler.receiveState(response);
    }, error => { console.log(error) }, true)
  }

  fetchTestDevAppInstances(url) {
    Request(url, {method: 'GET'}, response => {
      const instances = response.map(appInstance => {
        return new TestDevInstance(appInstance);
      });
      AppInstanceHandler.filteredInstances = instances;
      DataStore.receiveTestDevInstances(instances)
    }, error => { console.log(error) })
  }

  fetchDashboardStats(url, callback) {
    Request(url, {method:'GET'}, response => {
      callback(new DashboardStats(response));
    }, error => { console.log(error) })
  }

  fetchInstanceById(url, callBack) {
    Request(url, {method: 'GET'}, response => {
      callBack(response);
    }, error => { console.log(error) })
  }

  fetchCopyHistory(url, callBack) {
    Request(url, {method: 'GET'}, response => {
      callBack(response);
    }, error => {
      callBack(error);
    }, true)
  }

  fetchLogs(url, callback) {
    Request(url, {method: 'GET'}, response => {
      callback(response);
    }, error => { console.log(error) })
  }

  createCopy(url, instanceID) {
    const options = {
      contentType: 'application/json',
			method: 'POST',
			data:{
				appInstanceId: instanceID
			}
    }
    Request(url, options, response => {
      const api = AppInstanceApi.dashboardStats(instanceID);
      this.fetchDashboardStats(api, res => {
        AppInstanceHandler.receiveDashboardStats(res);
      });
    }, error => { console.log(error); }, true)
  }

  repurposeCopy(url, data) {
    const options = { method: 'POST', data: data, contentType: 'application/json' }
    Request(url, options, response => {
      AppInstanceHandler.fetchAppInstance();
      const api = AppInstanceApi.dashboardStats(data.originAppInstanceId);
      this.fetchDashboardStats(api, res => {
        AppInstanceHandler.receiveDashboardStats(res);
      });
    }, error => console.log(error) )
  }

}

const singleton = new AppInstanceService();

autorun(()=>{
  // console.log(singleton.appTemplates)
})

export default singleton;
