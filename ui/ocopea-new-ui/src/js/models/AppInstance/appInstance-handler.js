// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import { action, observable, autorun } from 'mobx';
import appInstanceApi from './appInstance-api';
import appInstanceService from './appInstance-service';
import DataStore from '../../stores/data-store';
import UserHandler from '../../models/User/user-handler';
import User from '../../models/User/user-model';
import AppTemplateHandler from '../../models/AppTemplate/appTemplate-handler';
import Helper from '../../utils/helper';
import _ from 'lodash';


class AppInstanceHandler {

  filterTypes = {
    appType: {name: 'app type', key: 'appType'},
    user: {name: 'users', key: 'user'},
    service: {name: 'services', key: 'service'}
  }

  @observable filters;
  @observable allFilters;
  @observable selectedFilters;
  @observable deploymentState;
  @observable dashboardStats;
  @observable filteredInstances;

  constructor(){
    this.filters = {
      appType: {
        name: this.filterTypes.appType.name,
        items: [],
        selectedItems: [],
        key: this.filterTypes.appType.key
      },
      user: {
        name: this.filterTypes.user.name,
        items: [],
        selectedItems: [],
        key: this.filterTypes.user.key
      },
      service: {
        name: this.filterTypes.service.name,
        items: [],
        selectedItems: [],
        key: this.filterTypes.service.key
      }
    };
    this.selectedFilters = [];
    this.deploymentState = {};
    this.dashboardStats = {};
  }

  repurposeCopy(data) {
    appInstanceService.repurposeCopy(appInstanceApi.repurposeCopy, data);
  }

  receiveDashboardStats(dashboardStats) {
    this.dashboardStats = dashboardStats;
  }

  fetchState(instanceID) {
    appInstanceService.fetchState(appInstanceApi.state(instanceID));
  }

  receiveState(state) {
    this.deploymentState = state;
  }

  fetchAppInstance() {
    appInstanceService.fetchAppInstance(appInstanceApi.appInstance);
  }

  fetchTestDevAppInstances() {
    appInstanceService.fetchTestDevAppInstances(appInstanceApi.testDevInstance);
    this.filterInstances(DataStore.testDevInstances);
  }

  fetchDashboardStats(instanceID, callback) {
    appInstanceService.fetchDashboardStats(appInstanceApi.dashboardStats(instanceID), callback);
  }

  fetchCopyHistory(instanceID, interval, range, callback) {
    const url = appInstanceApi.copyHistory(instanceID, interval, range);
    appInstanceService.fetchCopyHistory(url, callback)
  }

  fetchLogs(instanceID, callback) {
    appInstanceService.fetchLogs(appInstanceApi.logs(instanceID), callback);
  }

  createCopy(instanceID) {
    appInstanceService.createCopy(appInstanceApi.createCopy, instanceID);
  }

  // <--- filters
  @action onFilterClick(item, filter) {
    // console.log(item)
    const filterType = filter.key;
    const filterName = item.name;
    const selectedFilters = this.selectedFilters;
    const allFiltersObject = this.filters;
    const arrayToFilter = DataStore.testDevInstances;
    const handler = this.filterCallback.bind(this);
    const forbidMultiple = false;

    const filtered = Helper.toggleFilter(
      filterType, filterName,
      selectedFilters, allFiltersObject,
      arrayToFilter, handler, forbidMultiple
    )

    this.filteredInstances = filtered;
  }

  filterCallback(object, filter, item, allFilters) {
    switch (filter) {
      case this.filterTypes.user.key:
        const creatorUser = UserHandler.getUserById(object.appInstance.creatorUserId);
        return this.isFilterSelected(creatorUser.fullName, allFilters);
        break;
      case this.filterTypes.service.key:
        return object.quota.dsbQuota.filter(q => {
          return q.name === item;
        })[0];
        break;
      case this.filterTypes.appType.key:
        const appTemplate = AppTemplateHandler.getAppTemplateById(object.appInstance.appTemplateId);
        return this.isFilterSelected(appTemplate.name, allFilters);
        break;
    }
  }

  isFilterSelected(param, allFilters) {
    const selected = _.filter(allFilters, filter=>{
      return filter.name === param;
    })[0];
    return selected;
  }

  filterInstances(instances) {
    const appTypes = this.filters[this.filterTypes.appType.key].items = this.getUniqInstancesNamesList(instances);
    const services = this.filters[this.filterTypes.service.key].items = this.getUniqServicesList(instances);
    const users = this.filters[this.filterTypes.user.key].items = this.getUniqUsersList(instances);
  }

  getUniqInstancesNamesList(instances) {
    const appTemplatesNames = [];
    if(instances){
      const instancesNames = instances.map(instance => {
        const appTemplate = AppTemplateHandler.getAppTemplateById(instance.appInstance.appTemplateId);
        if(appTemplate)
          appTemplatesNames.push({name: appTemplate.name, type: this.filterTypes.appType.key});
      });
      return _.uniqBy(appTemplatesNames, 'name');
    }
  }

  getUniqServicesList(instances) {
    let allServices = [];
    if(instances) {
      instances.map(instance => {
        instance.quota.dsbQuota.map(q => {
          allServices.push({name: q.name, type: this.filterTypes.service.key});
        });
      });
      return _.uniqBy(allServices, 'name');
    }
  }

  getUniqUsersList(instances) {
    const users = [];
    if(instances) {
      const allUsers = instances.map(instance => {
        const user = UserHandler.getUserById(instance.appInstance.creatorUserId);
        return {name: user.fullName, type: this.filterTypes.user.key};
      });
      return _.uniqBy(allUsers, 'name');
    }
  }
  // <--- end filters

  getInstanceById(id) {
    const appInstances = DataStore.appInstances || [];
    return appInstances.filter(instance => {
      return instance.id === id;
    })[0];
  }

  fetchInstanceById(id, callBack) {
    appInstanceService.fetchInstanceById(appInstanceApi.appInstanceByID(id), callBack);
  }

}

const singleton = new AppInstanceHandler();

autorun(()=>{
  // console.log(singleton.deploymentState)
})

export default singleton;
