// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import Reflux from 'reflux';
import ConfigurationActions from '../../actions/configurationActions.js';
import SharedImageActions from '../../../../module-development/js/actions/dev-shared-image-actions.js';


var DevImageOriginStore = Reflux.createStore({

  listenables: [ConfigurationActions, SharedImageActions],

  state: {
    sites: []
  },

  getInitialState: function(){
    return this.state;
  },

  init: function(){

  },

  onReceiveSites(sites) {
    // this.sites = sites;
    // this.trigger(this.state);
  }

});

export default DevImageOriginStore;
