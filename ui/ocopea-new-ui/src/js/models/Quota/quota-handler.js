// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import { action, observable, autorun } from 'mobx';
import quotaService from './quota-service';
import quotaApi from './quota-api';


class QuotaHandler {

  constructor(){

  }

  fetchQuotas() {
    const dummyOrgId = 1;
    quotaService.fetchQuotas(quotaApi.quota(dummyOrgId));
  }

}

const singleton = new QuotaHandler();

autorun(()=>{
  // console.log(singleton.appTemplates)
})

export default singleton;
