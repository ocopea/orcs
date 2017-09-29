// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import { action, observable, autorun } from 'mobx';
import DataStore from '../../stores/data-store';
import { Quota } from '../../models/Quota/quota-model';
import Request from '../../transportLayer';
import mockQuota from './mock/mockQuota.json';
import _ from 'lodash';


class TemplateService {

  constructor(){

  }

  fetchQuotas(url) {
    // Request(url, {method: 'GET'}, response => {
    //   const quota = new Quota(response);
    //   DataStore.receiveQuota(quota);
    // }, error => {console.log(error);})

    const quota = new Quota(mockQuota);
    DataStore.receiveQuota(quota);
  }

}

const singleton = new TemplateService();

autorun(()=>{
  // console.log(singleton.appTemplates)
})

export default singleton;
