// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import Reflux from 'reflux';
import MockData from '../data/dashboard-topology-mockdata.js';
import AppTopologyActions from '../../../module-production/js/actions/appTopologyActions.js';
import DevDashboardActions from '../actions/dev-dashboard-actions.js';
import DevWizardActions from '../actions/dev-wizard-actions.js';
import TopologyParser from '../../../shared-components/js/topology-data-parser.js';
import ProdAppTopologyActions from '../../../module-production/js/actions/appTopologyActions.js';
import ProdWizardStore from '../../../module-production/js/stores/wizard/_wizard-main-store.js';
import Config from '../../../module-production/js/config.js';
import DevNavigationOptions from '../data/devNavigationOptions.js';


var DevDashboardTopologyStore = Reflux.createStore({

  listenables: [AppTopologyActions, DevDashboardActions, DevWizardActions],

  getInitialState: function(){
    return this.state;
  },

  state: {
    isMock: false,
    data: []
  },

  init: function(){
    if(this.state.isMock){
      // this.state.data = MockData;
    }else{
      //fetch app topology data
    }
  },

  onSetSelectedInstance: function(instance){    
    if(Config.getCurrentHash().module == DevNavigationOptions.module &&
       Config.getCurrentHash().location == DevNavigationOptions.dashboard.location.substring(1)){
        //  console.log(ProdWizardStore.state.allApplications[instance.appTemplateId])
         ProdAppTopologyActions.setSelectedApp(
                                 ProdWizardStore.state.allApplications[instance.appTemplateId]
                               );
       }
  },

});

export default DevDashboardTopologyStore;
