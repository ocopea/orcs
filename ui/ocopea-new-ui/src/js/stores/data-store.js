// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import { autorun, observable } from 'mobx';

class DataStore {

  @observable appTemplates;
  @observable appTemplatesMap;
  @observable appInstances;
  @observable testDevInstances;
  @observable savedImages;
  @observable users;
  @observable usersMap;
  @observable quota;
  @observable sites;

  constructor() {
    this.receiveAppTemplates([]);
    this.receiveUsers([]);
    this.receiveSites([]);
    this.receiveSavedImages([]);
    this.receiveQuota([]);
    this.receiveAppInstances([]);
    this.receiveTestDevInstances([]);
  }

  receiveAppTemplates(appTemplates) {
    this.appTemplates = appTemplates;
    this.appTemplatesMap = appTemplates.reduce((accumulator, template) => {
      const acc = accumulator;
      acc[template.id] = template;
      return acc;
    }, {});
  }

  receiveUsers(users) {
    this.users = users;
    this.usersMap = users.reduce((accumulator, user) => {
      const acc = accumulator;
      acc[user.id] = user;
      return acc;
    }, {});
  }

  receiveQuota(quota) {
    this.quota = quota;
  }

  receiveSavedImages(savedImages) {
    this.savedImages = savedImages;
  }

  receiveAppInstances(appInstances) {
    this.appInstances = appInstances;
  }

  receiveTestDevInstances(instances) {
    this.testDevInstances = instances;
  }

  receiveSites(sites) {
    this.sites = sites;
  }

}

const singleton = new DataStore();

autorun(() => {
  // console.log(singleton.appInstances)
});

export default singleton;
