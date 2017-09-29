// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import { computed, action, autorun, observable } from 'mobx';
import i18n from '../i18n.js';
import { hashHistory } from 'react-router';
import modules from '../utils/modules.json';
import _ from 'lodash';


class UiStore {

  @observable pendingRequests;
  @observable lightBox;
  @observable selectedDialogType;
  @observable module;
  @observable currentLocation;
  @observable loggedInUser;
  @observable mainMenu;
  dialogTypes;

  constructor(){
    this.pendingRequests = [];
    this.lightBox =        { isRender: false, optionalMsg: '', props: {} };
    this.dialogTypes =     { quotaList: 'quotaList', restoreCopy: 'restoreCopy' };
    this.mainMenu =        { isRender: true };
  }

  addPendingRequest(request){
    this.pendingRequests.push(request);
  }

  removePendingRequest(request){
    this.pendingRequests.remove(request)
  }

  setLocation(location) {
    if(location){
      this.currentLocation = location;
      const module = _.split(location.pathname, '/')[1];
      const screenName = _.split(location.pathname, '/')[2];
      if(module){
        this.module = module;
      }
    }else{ this.currentLocation = {} }
  }

  @action showLightBox(bool, dialogType, optionalMsg, props) {
    this.lightBox.isRender = bool;
    this.selectedDialogType = this.dialogTypes[dialogType];
    this.lightBox.optionalMsg = optionalMsg;
    this.lightBox.props = props;
  }

  @action showMainMenu(bool) {
    this.mainMenu.isRender = bool;
  }

  @action toggleMainMenu() {
    this.mainMenu.isRender = !this.mainMenu.isRender;
  }

  receiveLoggedInUser(loggedInUser) {
    this.loggedInUser = loggedInUser;
  }

}

const singleton = new UiStore()

autorun(() => {
  // console.log(singleton.wizardDeploy.currentStep)
});

export default singleton;
