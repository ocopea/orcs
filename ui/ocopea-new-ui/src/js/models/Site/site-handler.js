// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import { action, observable, autorun } from 'mobx';
import siteApi from './site-api';
import siteService from './site-service';
import DataStore from '../../stores/data-store';


class SiteHandler {

  constructor(){

  }

  fetchSites() {
    siteService.fetchSites(siteApi.sitesTopology);
  }

  getSiteById(id) {
    return DataStore.sites.filter(site => {
      return site.id === id;
    })[0]
  }

}

const singleton = new SiteHandler();

autorun(()=>{
  // console.log(singleton.appTemplates)
})

export default singleton;
