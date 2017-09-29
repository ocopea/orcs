// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import dataStore from '../controllers/topology-data-controller.js';


class HandleState {

  stateDictionary;

  constructor(){
    this.stateDictionary = {
      running:        'running'.toUpperCase(),
      deploying:      'deploying'.toUpperCase(),
      deployed:       'deployed'.toUpperCase(),
      bound:          'bound'.toUpperCase(),
      error:          'error'.toUpperCase(),
      errorBinding:   'errorBinding'.toUpperCase(),
      failedCreating: 'failedCreating'.toUpperCase()
    }
  }

  isLoading(state) {
    const _state = state.toUpperCase();
    return _state !== this.stateDictionary.deployed &&
           _state !== this.stateDictionary.running &&
           _state !== this.stateDictionary.bound &&
           !this.isError(_state);
  }

  isDoneFetching(dataStore) {
    let checked = [];
    // iterate all services
    _.forEach(dataStore.services, service=>{
      checked.push(this.isLoading(service.state))
    });

    // iterate all dependencies
    _.forEach(dataStore.dependencies, dependency=>{
      checked.push(this.isLoading(dependency.state))
    });
    return checked.indexOf(true) === -1;
  }

  isError(state) {
    const _state = state.toUpperCase();
    return _state === this.stateDictionary.error ||
           _state === this.stateDictionary.errorBinding ||
           _state === this.stateDictionary.failedCreating;
  }

}

const singleton = new HandleState();

export default singleton;
