// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import dataStore from '../controllers/topology-data-controller.js';
import { observable } from 'mobx';

class HandleState {

  stateDictionary;
  @observable status;

  constructor(){
    this.stateDictionary = {
      running:        'running'.toUpperCase(),
      deploying:      'deploying'.toUpperCase(),
      deployed:       'deployed'.toUpperCase(),
      bound:          'bound'.toUpperCase(),
      error:          'error'.toUpperCase(),
      errorBinding:   'errorBinding'.toUpperCase(),
      errorCreating:  'errorCreating'.toUpperCase(),
      failedCreating: 'failedCreating'.toUpperCase(),
      pending:        'pending'.toUpperCase(),
      creating:       'creating'.toUpperCase(),
      queued:         'queued'.toUpperCase()
    }
    this.status = {
      done: [],
      inProgress: []
    }
  }

  isLoading(state, instanceState) {
    const _state = state.toUpperCase();
    const _instanceState = instanceState ? instanceState.toUpperCase() : '';

    return _state !== this.stateDictionary.deployed &&
           _state !== this.stateDictionary.running &&
           _state !== this.stateDictionary.bound &&
           !this.isError(_state, _instanceState);
  }

  isDoneFetching(dataStore, instanceState) {
    let checked = [];
    instanceState = instanceState ? instanceState.toUpperCase() : '';

    // iterate all services
    _.forEach(dataStore.services, service=>{
      const state = service.state;
      let isLoading = this.isLoading(state);
      const isPendingWhileError = instanceState === this.stateDictionary.error &&
                                  state === this.stateDictionary.pending;
      isLoading = !isPendingWhileError ? isLoading : false;
      this.populateStatus(isLoading, service);
      checked.push(isLoading);
    });

    // iterate all dependencies
    _.forEach(dataStore.dependencies, dependency=>{
      const isLoading = this.isLoading(dependency.state);
      this.populateStatus(isLoading, dependency);
      checked.push(isLoading)
    });

    return checked.indexOf(true) === -1;
  }

  populateStatus(isLoading, element) {
    const id = element.name;
    if(isLoading) {
      if(this.status.inProgress.indexOf(id) === -1) {
          this.status.inProgress.push(id)
      }
    }else {
      this.status.inProgress.remove(id);
      if(this.status.done.indexOf(id) === -1) {
          this.status.done.push(id)
      }
    }
    // console.log(this.status)
  }

  isError(state, instanceState) {

    const _state = state.toUpperCase();
    const _instanceState = instanceState ? instanceState.toUpperCase() : null;

    if(_instanceState === this.stateDictionary.error) {
      if(_state === this.stateDictionary.pending) { return true; }
    }
    // console.log('_instanceState: ', _instanceState)
    // console.log('_state: ', _state)
    return _state === this.stateDictionary.error ||
           _state === this.stateDictionary.errorBinding ||
           _state === this.stateDictionary.failedCreating ||
           _state === this.stateDictionary.errorCreating
  }

  clearState() {
    this.status = {
      done: [],
      inProgress: []
    }
  }

}

const singleton = new HandleState();

export default singleton;
