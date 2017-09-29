// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import Reflux from "reflux";
import Actions from "../../actions/actions.js";
import MainStore from "../main-store.js";
import Config from "../../config.js";
import CopyHistoryErrors from "../../data/copyHistoryErrorMsgs.js";
import ProdNavigationOptions from '../../data/prodNavigationOptions.js';
import stateOptions from '../../../../module-development/js/data/deploying-state-options.js';

require('es6-promise').polyfill();
require('isomorphic-fetch');


let DashboardMainStore = Reflux.createStore({

  listenables: [Actions],

  state: {
    allAppInstances: {},
    leftMenu:{
        isRender: true
    },
		rightMenu: {
			isRender: false
		},
    dashboard:{
      header:{},
      main: {
      leftMenu:{},
      cards: {
        quotaSummary:{
            title: "quota summary"
        },
        statistics:{
            title: "statistics",
            isLoading: false
        },
        availabilityZones:{
            title: "app availability zones",
            appGeography: {},
            isLoading: false
        },
      },
      copyHistory:{
        title: "copy history",
        range: "week",
        interval: 0,
				intervalStart: null,
				intervalEnd: null,
        isLoading: false,
				isValid: true,
				inlineError: {
				error: "",
				isRender: false,
        count: 0
			},

      copies: [],
      selectedCopy: {},
			prevCopies: [],
        footer: {
            backups: 0,
            failed: 0
        }
      },
			sankey: {
				isLoading: false
			},
      appCopiesSummary:[],
      appCopiesSummaryStaticJSON:[],
      appDataDistribution:[]
      },
    },
    tooltip:{
      isRender: false,
      position:{
          top: 0,
          left: 0,
      },
      copySize: 0,
      timeToRestore: 1
    },
    restorePopup: {
			purpose:["Test/Dev", "Analytics", "Other"],
			appName: "",
      defaultAppName: "",
			site:{},
			isRender: false
		},
    isWizard: true,
    selectedBusinessContinuitySettings:{},
    selectedInfrastructureService:{},
    allAppInstances: {},
    appInstance: {},
    appInstanceCount: 1
  },

	getInitialState: function(){
    return this.state;
  },

  onShowComponentLoadingGif: function(){
      this.state.dashboard.main.copyHistory.isLoading = true;
      this.trigger(this.state);
  },

  onHideComponentLoadingGif: function(){
      this.state.dashboard.main.copyHistory.isLoading = false;
      this.trigger(this.state);
  },

	onShowStatisticsPieLoadingGif: function(){
		this.state.dashboard.main.cards.statistics.isLoading = true;
		this.trigger(this.state);
	},

	onHideStatisticsPieLoadingGif: function(){
		this.state.dashboard.main.cards.statistics.isLoading = false;
		this.trigger(this.state);
	},

  onGoToDashboard: function(appInstance){

  	Actions.removeCopyHistoryErrorMsg();
    var range = this.state.dashboard.main.copyHistory.range;
    var interval = this.state.dashboard.main.copyHistory.interval;
  	var appName = appInstance == undefined ?
  					this.state.appInstance.appInstanceName : appInstance.name;

  	Actions.navigateToDashboard(appName);

    if(appInstance != undefined){
      this.state.appInstance = appInstance;
      this.fetchDashboardState(this.state.appInstance);
  	}

  },

  fetchDashboardState: function(appInstance){

    var that = this;
    var dashboardStatsOptions = {
      url: APISERVER + '/hub-web-api/app-instance/'+ appInstance.id +'/dashboard-stats',
      contentType: 'application/json',
      method: 'GET'
    }

    Config.request(dashboardStatsOptions, function(response){
      if(appInstance.state.toUpperCase() === stateOptions.running.toUpperCase()){
        Actions.setAppInstanceNameFromDialog(response.appInstance.name);
        that.state.dashboard.main.cards.availabilityZones.appGeography = response.appGeography;
        that.state.dashboard.main.appCopiesSummary =
        that.formatAppCopySummary.bind(that, response)();
        that.state.dashboard.main.appDataDistribution = response.copyDistributionSankey;
        that.state.dashboard.main.cards.statistics.isLoading = false;
        that.state.dashboard.main.sankey.isLoading = false;
        that.state.dashboard.main.cards.availabilityZones.isLoading = false;
        that.updateCopyHistory();
        that.trigger(that.state);
      }else{
        Actions.showErrorDialog("app instance " + appInstance.appInstanceName + " is not running")
      }
    }, function(error){
        that.state.dashboard.main.cards.statistics.isLoading = false;
        that.state.dashboard.main.sankey.isLoading = false;
        Actions.showErrorDialog("App Instance: "+ appInstance.appInstanceName  + ", not found !");
    });

    this.trigger(this.state);
  },

  formatAppCopySummary: function(response){

      var summary = [];
      var count = 0;
      var colors = ["#83bd5c", "#91c7e5", "#e37941"];
      var categories = ['production', 'test-dev', 'offlineBackup'];

      //format response from server
      for( var key in response.copySummary ){
          summary.push(
              {
                  name: key,
                  y: response.copySummary[key],
              }
          );
      }

      //add colors for statistics pie display
      summary.forEach(function(sum, index){
          sum.color = colors[index]
      });

      return summary;

  },


  formatCopyHistory: function(copyHistory){

    var intervalStart = this.state.dashboard.main.copyHistory.intervalStart;
  	var intervalEnd = this.state.dashboard.main.copyHistory.intervalEnd;
  	var range = this.state.dashboard.main.copyHistory.range;

    var copies = copyHistory.copies.map(function(copy){
        var timeStamp = new Date(copy.timeStamp);
        copy.timeStamp = timeStamp;
        copy.timeToRestore = 1;
        copy.size = 1;
        return copy
    });

  	var startCopy = {timeStamp: intervalStart};
  	var endCopy = {timeStamp: intervalEnd};

  	copies.push(startCopy)
  	copies.push(endCopy)
    return copies;
  },

	init: function(){

    var that = this;

    //fetch all production app instances
    var options = {
      url: APISERVER + '/hub-web-api/app-instance',
      method: 'GET'
    }

    if(Config.getCurrentHash().module == ProdNavigationOptions.module &&
       Config.getCurrentHash().location == 'dashboard'){

        Config.request(options, function(response){

          response.forEach(instance=>{
            that.state.allAppInstances[instance.id] = instance;
          });

          that.trigger(that.state);
          var appInstance = that.state.allAppInstances[Config.getCurrentHash().subLocation];

          Actions.goToDashboard(appInstance);

      }, function(error){});
    }

	},

  createCopies: function(){
    //mock data
    var allApps = [
        {timeStamp: new Date(), status: "success", size: 1, timeToRestore: 1}
    ];

    for(var i=1; i<=6; i++){
        allApps.push(
            {timeStamp: new Date(new Date().setDate(new Date().getDate() - i))}
        )
    }
    return allApps;
  },

  getAppDistributionData: function(selectedApp){

    var appServiceTemplates = selectedApp.appServiceTemplates

    var dataServices = []
    appServiceTemplates.forEach(function(currTemplate){
        currTemplate.dependencies.forEach(function(currDep){
            dataServices.push(currDep);
        });
    });

    var sankeyData = []

    var total = 0;

    this.state.dashboard.main.appCopiesSummary.forEach(function(currCopySummary){

      switch(currCopySummary.name){
          case 'Production':
              sankeyData.push([currCopySummary.name, "Cloud Block Storage", currCopySummary.y * 3 * dataServices.length]);
               dataServices.forEach(function (currDataService){
                   sankeyData.push([currDataService.type, currCopySummary.name, currCopySummary.y * 3]);
               });
               total += currCopySummary.y * 3 * dataServices.length;
               break;
           case 'Offline Backup':
              if (currCopySummary.y * 3 > 0){
                  sankeyData.push([currCopySummary.name, "DDVE", currCopySummary.y * 3 * dataServices.length])
                   dataServices.forEach(function (currDataService){
                       sankeyData.push([currDataService.type, currCopySummary.name, currCopySummary.y * 3]);
                   });
                   total += currCopySummary.y * 3 * dataServices.length;
               }
               break;
           case 'Test/Dev':
              if (currCopySummary.y * 3 > 0){
                  sankeyData.push([currCopySummary.name, "Cloud Ephemeral Storage", currCopySummary.y * 3 * dataServices.length])
                   dataServices.forEach(function (currDataService){
                       sankeyData.push([currDataService.type, currCopySummary.name, currCopySummary.y * 3]);
                   });
                   total += currCopySummary.y * 3 * dataServices.length;
              }
              break;
          }
      });

     dataServices.forEach(function (currDataService, idx){
         var size = total / dataServices.length;
         if (idx == 0){
            size += 1;
         }
         sankeyData.push([selectedApp.name, currDataService.type, size]);
     });

     sankeyData.push([selectedApp.name, "Logs", 1]);
     sankeyData.push(["Logs", "Analytics", 1]);
     sankeyData.push([dataServices[0].type, "Analytics", 1]);
     sankeyData.push(['Analytics', 'HDFS', 2]);

     return sankeyData;
  },


  getDashboardStatisticsData: function(){
    var colors = ["#83bd5c", "#91c7e5", "#e37941"];
    var data = [];

    this.state.dashboard.main.appCopiesSummaryStaticJSON.forEach(function(appCopy, index){
        var name = appCopy.name;
        var count = appCopy.count;
        var color = colors[index];
        var obj = {name: name, y: count, color: color};
        data.push(obj);
    });

    return data;
  },

  onUserClickOnNextCompleteWizard: function(){
    this.state.dashboard.main.appCopiesSummary = this.getDashboardStatisticsData();
    this.trigger(this.state);
  },

	onUserClickOnRePurposeCopy: function(){
    this.rePurposeCopy();
    this.trigger(this.state);
  },

  rePurposeCopy: function(){

		var that = this;

		if(this.state.restorePopup.appName == ""){
        this.state.restorePopup.appName = this.state.restorePopup.defaultAppName;
    }

    var repurposeAppOptions = {
			url: APISERVER + '/hub-web-api/commands/repurpose-app',
			method: 'POST',
			contentType: 'application/json',
			data: {
				appInstanceName: this.state.restorePopup.appName,
        originAppInstanceId: this.state.appInstance.id,
        purpose: "test-dev",
        copyId: this.state.dashboard.main.copyHistory.selectedCopy.copyId
			}
		}

		var dashboardStatsOptions = {
			url: APISERVER + '/hub-web-api/app-instance/'+ this.state.appInstance.id +'/dashboard-stats',
			method: 'GET',
			contentType: 'application/json',
		}

    Config.request(repurposeAppOptions, function(response){
        Config.request(dashboardStatsOptions, function(res){
          that.state.dashboard.main.appCopiesSummary = that.formatAppCopySummary.bind(that, res)();
          that.state.dashboard.main.appDataDistribution = res.copyDistributionSankey;
          console.log(response)
          Actions.getAllAppInstances();
          that.trigger(that.state);
        }, function(error){
            console.log(error) ;
        });
    }, function(error){
        Actions.showErrorDialog.bind(that, error.responseText)();
    });

  },

	onUserClickOnCopyHistoryRangeFilter: function(range){
        Actions.removeCopyHistoryErrorMsg();
        this.state.dashboard.main.copyHistory.range = range;
        this.state.dashboard.main.copyHistory.interval = 0;
        this.updateCopyHistory();
    },

    firsttime: false,

    onUserClickOnCopyHistoryPrevBtn: function(){

		var intervalEnd = this.state.dashboard.main.copyHistory.intervalEnd;
		var now = new Date();
		var that = this;
		var isValid = Config.compareDates(intervalEnd, now);

		if(isValid){
			this.state.dashboard.main.copyHistory.interval--;
			this.state.dashboard.main.copyHistory.isValid = true;
			this.trigger(this.state);

			this.updateCopyHistory();
		}else{

            //handle attemp to access future backups error masseges
			var state = this.state;
            var timer = 0;

			if(this.firsttime) return;
			this.firsttime = true;

			timer = setTimeout(function(){

                state.dashboard.main.copyHistory.inlineError.isRender = false;
				that.trigger(state);
				that.firsttime = false;

			}, 2500);

			this.state.dashboard.main.copyHistory.isValid = false;
			this.state.dashboard.main.copyHistory.inlineError.isRender = true;
            var count = this.state.dashboard.main.copyHistory.inlineError.count;
			this.state.dashboard.main.copyHistory.inlineError.error = CopyHistoryErrors[count];
            this.state.dashboard.main.copyHistory.inlineError.count++;
            if(this.state.dashboard.main.copyHistory.inlineError.count >= CopyHistoryErrors.length){
                this.state.dashboard.main.copyHistory.inlineError.count = 0;
            }
			this.trigger(this.state);
		}
//        console.log(this.state.dashboard.main.copyHistory.isValid)
    },

	onRemoveCopyHistoryErrorMsg: function(){
		this.state.dashboard.main.copyHistory.inlineError.isRender = false;
		this.state.dashboard.main.copyHistory.isValid = true;
		this.trigger(this.state);
	},

  onUserClickOnCopyHistoryNextBtn: function(){
      Actions.removeCopyHistoryErrorMsg();
      this.state.dashboard.main.copyHistory.interval++;
      this.trigger(this.state);
      this.updateCopyHistory();

  },

  updateCopyHistory: function(){

		var that = this;
		var domain = APISERVER;
    var appName;

    var range = this.state.dashboard.main.copyHistory.range;
    var interval = this.state.dashboard.main.copyHistory.interval;

		this.state.dashboard.main.copyHistory.prevCopies = this.state.dashboard.main.copyHistory.copies;

		var options = {
			url: APISERVER + '/hub-web-api/app-instance/'+
				 this.state.appInstance.id +'/copy-history?interval=' +
				 interval + '&period=' + range,
			contentType: 'application/json',
			method: 'GET'
		}

		Config.request(options, function(response){

			that.state.dashboard.main.copyHistory.intervalStart =
				new Date(response.intervalStart);

			that.state.dashboard.main.copyHistory.intervalEnd =
				new Date(response.intervalEnd);

			that.state.dashboard.main.copyHistory.copies =
				that.formatCopyHistory.bind(that, response)();

			that.state.dashboard.main.copyHistory.isLoading = false;

			that.trigger(that.state);

		}, function(error){
      Actions.showErrorDialog(error.responseText);
		});

    this.state.dashboard.main.copyHistory.isLoading = true;

    this.trigger(this.state);

  },

	onInitializeCopyHistorySettings: function(){
		//initialize copyHistory
		this.state.dashboard.main.copyHistory.interval = 0;
		this.state.dashboard.main.copyHistory.range = 'week';
		this.trigger(this.state);
	},

  onUserClickOnCopyHistoryBackup: function(element, data){
    this.state.dashboard.main.copyHistory.selectedCopy = data;
    this.state.tooltip.isRender = true;
    this.state.tooltip.position.top = element.y.baseVal.value;
    this.state.tooltip.position.left = element.x.baseVal.value;
  	this.state.tooltip.copySize = data.size;
  	Actions.removeCopyHistoryErrorMsg();
    this.trigger(this.state);
  },

  onUserClickOnTooltipRestore: function(failover){

    const REMOTE = 'remote';
    if(!failover){
      this.state.restorePopup.isRender = true;
      this.state.restorePopup.defaultAppName = this.state.appInstance.name + this.state.appInstanceCount;
      this.state.restorePopup.appName = this.state.restorePopup.defaultAppName;
    }else {
      this.state.restorePopup.defaultAppName = `${this.state.appInstance.name}-${REMOTE}`;
      this.state.restorePopup.appName = `${this.state.appInstance.name}-${REMOTE}`;
      this.onUserClickOnRestoreFromPopup();
    }

    this.state.tooltip.isRender = false;
    this.state.appInstanceCount++;
    this.trigger(this.state);
  },

  onHideCopyHistoryRestoreTooltip: function(){
      this.state.tooltip.isRender = false;
      this.trigger(this.state);
  },

	onUserClickOnRestoreFromPopup: function(){

    this.state.restorePopup.isRender = false;
    Actions.showLoadingGif();
		var that = this;

		this.trigger(this.state);

		setTimeout(function(){
			that.rePurposeCopy();
      Actions.hideLoadingGif();
			that.trigger(that.state);
		}, 3000);
	},

  onUserClickOnCloseRetorePopup: function(){
      this.state.restorePopup.isRender = false;
      this.trigger(this.state);
  },

  onSetAppInstanceName: function(appInstanceName){
      this.state.appInstance.appInstanceName = appInstanceName;
      this.trigger(this.state);
  },

  onSetAppInstance: function(appInstance){

      this.state.dashboard.main.appCopiesSummary = [];
      this.state.dashboard.main.copyHistory.copies = [];
      this.state.appInstance = appInstance;
      this.trigger(this.state);
  },

  onSetAppInstanceNameFromDialog: function(appName){

      if(appName != undefined){
          this.state.restorePopup.appName = appName;
          this.state.restorePopup.defaultAppName = appName;
          this.trigger(this.state);
      }
  },

	onUserClickOnLeftMenuAppInstance: function(appInstance){

        this.state.appInstance = appInstance;
        Actions.initializeCopyHistorySettings();
        Actions.goToDashboard(appInstance);

	},

	onCreateAppCopy: function(){

        var that = this;

		var options = {
			url: APISERVER + "/hub-web-api/commands/create-app-copy",
			contentType: 'application/json',
			method: 'POST',
			data:{
				appInstanceId: this.state.appInstance.id
			}
		};

		Config.request(options, function(response){

			Actions.goToDashboard(that.state.appInstance);

		}, function(error){

			console.log(error)

		});

	},

});

export default DashboardMainStore;
