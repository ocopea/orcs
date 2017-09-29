// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import { action, observable, autorun } from 'mobx';
import DataStore from '../../stores/data-store';
import { Site } from '../../models/Site/site-model';
import mockSites from './mock/mockSites.json';
import Request from '../../transportLayer';


class TemplateService {

  constructor(){

  }

  fetchSites(url) {
    Request(url, { method: 'GET' }, response => {
      if(Array.isArray(response)) {
        response.forEach( site => {
          const _site = new Site(site);
          DataStore.sites.push(_site);
        })
      }
    }, error => { console.log(error) })

    // mockSites.forEach( site => {
    //   const _site = new Site(site);
    //   DataStore.sites.push(_site);
    // })

  }


}

const singleton = new TemplateService();

autorun(()=>{
  // console.log(singleton.appTemplates)
})

export default singleton;
