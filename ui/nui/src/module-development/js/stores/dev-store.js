// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import Reflux from 'reflux';
import Config from '../../../module-production/js/config.js';
import _ from 'lodash';
import MockAppInstances from '../data/mockAppInstances.js';
import DevNavigationOptions from '../data/devNavigationOptions.js';

//stores
import ImageOriginStore from './dev-image-origin-store.js';
import SharedStore from '../../../shared-store.js';

//actions
import DevActions from '../actions/dev-actions.js';
import DevDashboardActions from '../actions/dev-dashboard-actions.js';
import SharedActions from '../../../shared-actions.js';
import SharedImageActions from '../actions/dev-shared-image-actions.js';
import SettingsActions from '../actions/dev-settings-actions.js';


var filtersTypes = {
    appType: "appType",
    users: "users",
    services: "services"
};

var DevStore = Reflux.createStore({

	listenables: [DevActions, DevDashboardActions, SharedImageActions],

	state: {

        appInstances: {},

        selectedInstance: {},

        instancesSortedBy: "",

        filteredInstances: [],

        isMockAppInstances: false,

        header: {
          userMenu: {
            isRender: false
          }
        },

        leftMenu: {
            isRender: true,
            filters: {
                appType: {
                    isRender: false,
                    instances: [],
                    selectedFilters: []
                },
                users: {
                    isRender: false,
                    instances: [],
                    selectedFilters: []
                },
                services: {
                    isRender: false,
                    instances: [],
                    selectedFilters: []
                }
            }
        },

        shareImageDialog:{
          isRender: false,
          imageToShare: {},
          shareWith: "",
          shareOptions: {
            'jira': 'jira',
            'trello': 'trello',
            'email': 'email',
            'custom': 'custom',
            'pivotalTracker': 'Pivotal Tracker'
          },
          jira: {},
          config: {
            selectedPlatform: null
          }
        },

        users: {},

        allFilters: []
	},

  getInitialState: function(){
    return this.state;
  },

  isSettings: function(){
    return  Config.getCurrentHash().module === DevNavigationOptions.module &&
            SharedStore.state.currentLocation.location === DevNavigationOptions.settings.location;
  },

  isIntegrationsConfig: function() {
    return  this.isSettings() &&
            SharedStore.state.currentLocation.subLocation ===
              DevNavigationOptions.settings.subLocation.integrationsConfig.location.substring(1);
  },

	init: function() {

    this.getShareDetails();

    if(Config.getCurrentHash().module == DevNavigationOptions.module){

      DevActions.getAppInstances();
  		this.trigger(this.state);

      // handle load on integrations config
      if(this.isSettings()){
        if(!Config.getCurrentHash().additional){
          this.onGoToIntegrationsHub()
        }else{
          this.onGoToConfigurePlatform(Config.getCurrentHash().additional)
        }
      }

    }
	},

  onGetIntegrations() {
    this.getShareDetails();
  },

  onGoToSettings(platform) {
    SharedActions.navigate({
      module: DevNavigationOptions.module,
      location: DevNavigationOptions.settings.location,
      subLocation: DevNavigationOptions.settings.subLocation.integrationsConfig.location
    });
  },

  onGoToIntegrationsHub() {
    SharedActions.navigate({
      module: DevNavigationOptions.module,
      location: DevNavigationOptions.settings.location,
      subLocation: DevNavigationOptions.settings.subLocation.integrationsConfig.location
    });
  },

  onGoToConfigurePlatform(platform) {
    this.state.shareImageDialog.config.selectedPlatform = platform;
    SettingsActions.showIntegrationDialog(platform);

    SharedActions.navigate({
      module: DevNavigationOptions.module,
      location: DevNavigationOptions.settings.location,
      subLocation: DevNavigationOptions.settings.subLocation.integrationsConfig.location,
      additional: `/${platform}`
    });

    this.trigger(this.state);
  },

  getShareDetails() {
    var options = {
      url: `${APISERVER}/hub-web-api/share-image-integrations`,
      method: 'GET'
    }

    const that = this;
    Config.request(options, response=>{
      if(Array.isArray(response) && response.length){
        that.jiraDetails(response);
      }else{
        console.log('share-image-integrations response is empty')
      }
    }, error=>{console.log(error)})
  },

  jiraDetails: function(integrationsDetails){
    const shareImageDialog = this.state.shareImageDialog;
    const shareOptions = shareImageDialog.shareOptions;
    const that = this;

    let jiraConnection = integrationsDetails.filter(o=>{
      return o.integrationName === shareOptions.jira
    })[0];

    shareImageDialog.jira = jiraConnection;
    SettingsActions.receiveData(jiraConnection, shareOptions.jira);
    SharedImageActions.receiveDetails(jiraConnection);
    that.trigger(that.state);

  },

  onShareImage: function(shareWith, data){
    switch (shareWith) {
      case this.state.shareImageDialog.shareOptions.jira:
        data.shareURL     = this.state.shareImageDialog.imageToShare.shareURL;
        data.description  = this.state.shareImageDialog.imageToShare.description;
        SharedImageActions.createJiraIssue(data)
        break;

      case this.state.shareImageDialog.shareOptions.trello:
      alert(`share with ${shareWith}`)
        break;
      case this.state.shareImageDialog.shareOptions.email:
      alert(`share with ${shareWith}`)
        break;
      default:
        alert('not found')
    }
  },

  onShowShareImageDialog: function(image){
    // console.log(image)
    this.state.shareImageDialog.isRender=true;
    this.state.shareImageDialog.imageToShare=image;
    this.trigger(this.state);
  },

  onHideShareImageDialog: function(){
    this.state.shareImageDialog.isRender=false;
    this.trigger(this.state);
  },

  onSortInstances: function(sortBy){
    var that = this;
    switch (sortBy) {
      case 'owner':
          this.state.instancesSortedBy = 'user';
          this.state.filteredInstances.sort(function(a, b){
            var paramA = that.state.users[a.creatorUserId].firstName.toUpperCase();
            var paramB = that.state.users[b.creatorUserId].firstName.toUpperCase();
            return (paramA < paramB) ? -1 : (paramA > paramB) ? 1 : 0;
          });
          this.state.filteredInstances = _.sortBy(this.state.filteredInstances,
                                                  (o)=>{return true;})
        break;
      case 'name':
          this.state.instancesSortedBy = 'app type'
          this.state.filteredInstances = _.sortBy(this.state.filteredInstances, 'name')
        break;
      case 'quota':
          this.state.instancesSortedBy = 'quota';
          this.state.filteredInstances = _.sortBy(this.state.filteredInstances,
                                                  (o)=>{return o.quota.psbQuota})
        break;
      case 'created':
          this.state.instancesSortedBy = 'created'
          this.state.filteredInstances.sort(function(a, b) {
              var aa = new Date(a.dateCreated),
                  bb = new Date(b.dateCreated);

              if (aa !== bb) {
                  if (aa > bb) { return 1; }
                  if (aa < bb) { return -1; }
              }
              return aa - bb;
          });
          this.state.filteredInstances = _.sortBy(this.state.filteredInstances,
                                                  (o)=>{return true;})
          break;
    }

    SharedActions.sortTable(sortBy);
    DevActions.closeSortByMenu();
    this.trigger(this.state);
  },

  onToggleUserMenu: function(){
    this.state.header.userMenu.isRender = !this.state.header.userMenu.isRender;
    this.trigger(this.state);
  },

  onCloseUserMenu: function(){
    this.state.header.userMenu.isRender = false;
    this.trigger(this.state);
  },

  onHandleLeftMenuOnInit: function(){
    this.state.leftMenu.isRender = this.isLeftMenuRender();
    this.trigger(this.state);
  },

  isLeftMenuRender: function(){
    var currentLocation = SharedStore.state.currentLocation.location;
    return currentLocation !== DevNavigationOptions.wizard.location &&
           currentLocation !== DevNavigationOptions.dashboard.location &&
           currentLocation !== DevNavigationOptions.savedImages.location &&
           currentLocation !== DevNavigationOptions.sharedImage.location;
  },

  onUserClickOnHamburger: function(){
      this.state.leftMenu.isRender = !this.state.leftMenu.isRender;
      this.trigger(this.state);
  },

  onUserClickOnFilterTitle: function(filterType){
      switch(filterType){
          case filtersTypes.appType:
              this.state.leftMenu.filters.appType.isRender =
                  !this.state.leftMenu.filters.appType.isRender;
              break;
          case filtersTypes.users:
              this.state.leftMenu.filters.users.isRender =
                  !this.state.leftMenu.filters.users.isRender;
              break;
          case filtersTypes.services:
              this.state.leftMenu.filters.services.isRender =
                  !this.state.leftMenu.filters.services.isRender;
              break;
      }
      this.trigger(this.state);

  },

  /*****
   * @params: options {filterType: "", filter:"",creatorUserId:""}
   ****/
  onUserClickOnFilter: function(options){
      DevActions.sortInstances(this.state.instancesSortedBy)
      switch(options.filterType){
          case 'app type':
              this.state.leftMenu.filters.appType.selectedFilters =
                  this.addFilterIfNotExists(
                  this.state.leftMenu.filters.appType.selectedFilters,
                  options
              )
              break;

          case 'users':
              this.state.leftMenu.filters.users.selectedFilters =
                  this.addFilterIfNotExists(
                  this.state.leftMenu.filters.users.selectedFilters,
                  options
              )
              break;

          case 'services':
              this.state.leftMenu.filters.services.selectedFilters = this.addFilterIfNotExists(
                  this.state.leftMenu.filters.services.selectedFilters,
                  options
              )
              break;
      }

      this.state.allFilters = _.concat(this.state.leftMenu.filters.appType.selectedFilters,
                                       this.state.leftMenu.filters.users.selectedFilters,
                                       this.state.leftMenu.filters.services.selectedFilters);

      this.filterInstances();

      this.trigger(this.state);

  },

  addFilterIfNotExists: function(array, options){

      var isExists = _.findIndex(array, (o)=>{
        return o.filter === options.filter
      })

      if(isExists == -1){
          array.push({filter: options.filter, filterType: options.filterType});
      }else{
          this.removeFilter(array, options.filter)
      }
      return Object.assign([], array);
  },

  removeFilter: function(array, filter){
      _.remove(array, function(n){
          return n.filter === filter
      });
  },

  onGetAppInstances: function(){

      var that = this;

      //real test/dev app instances
      if(!this.state.isMockAppInstances){

          var options = {
              url: APISERVER + '/hub-web-api/test-dev/app-instance',
              method: 'GET'
          };

          Config.request(options, function(response){
              that.state.filteredInstances = response;
              that.filterAppInstances(response);
              that.trigger(that.state);
              that.populateUsers();
              response.forEach(instance=>{
                  that.state.appInstances[instance.id] = instance;
              })

              //determin current app if initiating on DevDashboard
              var location = Config.getCurrentHash().location,
                  currentInstanceID = Config.getCurrentHash().subLocation;

              var currentInstance = that.state.appInstances[currentInstanceID];
              DevDashboardActions.setSelectedInstance(currentInstance);
              that.trigger(that.state);

          }, function(error){})

      }else{
          this.populateUsers();
      }

  },

  populateUsers: function(){

      var that = this;

      var options = {
          url: APISERVER + '/hub-web-api/user',
          method: 'GET'
      };

      Config.request(options, function(response){
          //mock test/dev app instances
          if(that.state.isMockAppInstances){
              MockAppInstances[0].creatorUserId = response[0].id;
              MockAppInstances[1].creatorUserId = response[0].id;
              MockAppInstances[2].creatorUserId = response[2].id;
              MockAppInstances[3].creatorUserId = response[4].id;
              MockAppInstances[4].creatorUserId = response[4].id;
              that.state.appInstances = MockAppInstances;
              that.state.filteredInstances = MockAppInstances;
              that.filterAppInstances(MockAppInstances);
          }

          response.forEach((user)=>{
              that.state.users[user.id] = user;
          });

          var appInstances = that.state.appInstances;

          for(let key in appInstances){
            //add username property to each instance
            var creatorUser = that.state.users[appInstances[key].creatorUserId];
            appInstances[key].username = creatorUser.firstName + " " + creatorUser.lastName;
            //add services array to each instance
            var dsb = [];
            _.pickBy(appInstances[key].quota.dsbQuota, function(value, key){
                dsb.push(key);
            })
            appInstances[key].services = dsb;
          }

          DevDashboardActions.receiveUsers(that.state.users)

          that.trigger(that.state);

      }, function(error){})

  },

  filterAppInstances: function(appInstances){

      //users filters
      var uniqueUsers = _.uniqBy(appInstances, "creatorUserId");
      this.state.leftMenu.filters.users.instances = uniqueUsers;

      //app type filters
      var uniqueAppType = _.uniqBy(appInstances, "appTemplateName");
      this.state.leftMenu.filters.appType.instances = uniqueAppType;

      //services filters
      var dsb = [];
      appInstances.forEach(instance=>{
          _.pickBy(instance.quota.dsbQuota, function(value, key){
              dsb.push(key)
          })
      });
      this.state.leftMenu.filters.services.instances = _.uniq(dsb);
      this.trigger(this.state);

  },

  filterInstances: function(){
      this.state.filteredInstances = _.filter(this.state.appInstances, this.filter);
      this.trigger(this.state);
  },

  filter: function(instance){
      var check_category = false;
      // Walk through categories
      for (filtertype in this.state.leftMenu.filters){
          if (this.state.leftMenu.filters[filtertype].selectedFilters.length > 0){
              // Walks through all the selected filters
              this.state.leftMenu.filters[filtertype].selectedFilters.forEach(options=>{
                  // if string
                  if(typeof instance[this.associate(filtertype)] !== 'object'){
                      if( instance[this.associate(filtertype)] === options.filter){
                          check_category = true
                      }
                  }else{
                      // if array
                      if( instance[this.associate(filtertype)].indexOf(options.filter) > -1){
                          check_category = true
                      }
                  }
              });
              if (!check_category){
                  return false
              }
          }
              check_category = false
          }

      return true

  },

  associate: function(string){
      var dictionary = {
          'appType': 'appTemplateName',
          'users': 'username',
          'services': 'services'
      }
      return dictionary[string];
  }

});

export default DevStore;
