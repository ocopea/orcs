// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import { action, observable, autorun } from 'mobx';
// import DataStore from '../../stores/data-store';
// import UserModel from '../../models/user/user-model';
// import Request from '../../transportLayer';


class TemplateService {

  constructor(){

  }

}

const singleton = new TemplateService();

autorun(()=>{
  // console.log(singleton.appTemplates)
})

export default singleton;
