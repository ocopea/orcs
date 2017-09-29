// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import Reflux from 'reflux';
import Config from '../../../module-production/js/config.js';
import mockQuotas from '../data/mockQuotas.js';
import Actions from '../actions/dev-actions.js';
import SharedActions from '../../../shared-actions.js';
import NavigationOptions from '../data/devNavigationOptions.js';


var DevDashboardStore = Reflux.createStore({

	listenables:[Actions],

    state: {

        quota:{
            appQuota: 0,
            infrastructureQuotas: {}
        },

    		quotasList: {
    			isRender: false
    		},

    		isMockQuotas: false,

				sortByMenu: {
					isRender: false
				},

				view: "grid"

    },

  getInitialState: function(){
    return this.state;
  },

  init: function(){
		this.populateQuotas();
  },

	onToggleSortByMenu: function(){
		this.state.sortByMenu.isRender = !this.state.sortByMenu.isRender;
		this.trigger(this.state);
	},

	onCloseSortByMenu: function(){
		this.state.sortByMenu.isRender = false;
		this.trigger(this.state);
	},

	onUserChangedView: function(view){
		this.state.view = view;
		this.trigger(this.state);
	},

	populateQuotas: function(){

		!this.state.isMockQuotas ?
        	this.getQuota()
		:
			this.state.quota = mockQuotas;

		this.trigger(this.state)

	},

  getQuota: function(){

      var options = {
          url: APISERVER + "/hub-web-api/test-dev/quota/1",
          method: 'GET'
      },
          that=this;

      Config.request(options, function(response){
          that.state.quota.appQuota = response.psbQuota;

		var infraQuotas = that.parseInfraQuotasToArray(response.dsbQuota);

          that.state.quota.infrastructureQuotas = infraQuotas;
          that.trigger(that.state);
      }, function(error){})

  },

	parseInfraQuotasToArray: function(obj){

		var infraQuotas = [];

		for(let key in obj){

			infraQuotas.push(
				{
					name: key,
					precent: obj[key]
				}
			)
		}

		return infraQuotas;

	},

	onShowQuotasList: function(){

		this.state.quotasList.isRender = true;

		SharedActions.navigate({
			module: NavigationOptions.module,
			location: NavigationOptions.main.location,
			subLocation: NavigationOptions.main.subLocations.quotasList
		});

		this.trigger(this.state);
	},

	onHideQuotasList: function(){

		this.state.quotasList.isRender = false;

		SharedActions.navigate({
			module: NavigationOptions.module,
			location: NavigationOptions.main.location,
			subLocation: ""
		});

		this.trigger(this.state);
	}

});

export default DevDashboardStore;
