// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import Reflux from 'reflux';
import SharedActions from './shared-actions.js';
import DevActions from './module-development/js/actions/dev-actions.js';
import ProductionAppTopologyActions from './module-production/js/actions/appTopologyActions.js';
import ProductionActions from './module-production/js/actions/actions.js';
import DevNavigationOptions from './module-development/js/data/devNavigationOptions.js';
import Config from './module-production/js/config.js';
import _ from 'lodash';


var modes = {
	production: "production",
	development: "development"
}


var SharedStore = Reflux.createStore({

  listenables: [SharedActions],

	state:{

		currentLocation: {
			module: "",
			location: "",
			subLocation: ""
		},

		tableSortBy: "",

		loggedInUser: "",

		simpleTooltip: {
			isRender: false,
			data: {
				position: {x: 0, y: 0},
				element: {}
			}
		}

	},

	init: function(){

		var module = Config.getCurrentHash().module,
			location = Config.getCurrentHash().location,
			subLocation = Config.getCurrentHash().subLocation;

		this.state.currentLocation.module = module;
		this.state.currentLocation.location = "/"+location;
		this.state.currentLocation.subLocation = subLocation;

		this.fetchCurrentUser();

		this.trigger(this.state);
	},

	fetchCurrentUser: function(){
		var that = this;
		var options = {
			url: APISERVER+'/hub-web-api/logged-in-user',
			method: 'GET'
		}
		Config.request(options, function(response){
			that.state.loggedInUser = response;
			that.trigger(that.state);
		}, function(error){
			console.log(error)
		});
	},

  getInitialState: function(){
    return this.state;
  },

	onSetModule: function(module){
		this.state.currentLocation.module = module;
        ProductionAppTopologyActions.initialAppTopology();
		this.trigger(this.state);
	},

	onNavigate: function(navigation){
		var module = navigation.module,
			location = navigation.location,
			subLocation = navigation.subLocation,
			additional = navigation.additional || null;

		if(location !== DevNavigationOptions.sharedImage.location){
			this.state.currentLocation.location = location;
			this.state.currentLocation.subLocation = subLocation;
			this.state.currentLocation.module = module;
			this.state.currentLocation.additional = additional;

			var hash = additional ?
				module + location + subLocation + additional :
				module + location + subLocation

			window.location.hash = hash;

		}else{
			var segments = _.split(window.location.hash, '/');
			this.state.currentLocation.location = location;
			this.state.currentLocation.module = module;

			var hash = module + location +'/'+segments[2]+'/'+segments[3]+'/'+segments[4];

			window.location.hash = hash;
		}
		this.trigger(this.state);
	},

	onSortTable: function(sortBy){
		this.state.tableSortBy = sortBy;
		this.trigger(this.state);
	},

	onShowSimpleTooltip(position, title, error) {
		this.state.simpleTooltip.data.title = title;
		this.state.simpleTooltip.data.error = error;
		this.state.simpleTooltip.isRender = true;
		this.state.simpleTooltip.data.position = position;
		this.trigger(this.state);
	},

	onHideSimpleTooltip() {
		this.state.simpleTooltip.isRender = false;
		this.state.simpleTooltip.data = {};
		this.trigger(this.state);
	}


});

export default SharedStore;
