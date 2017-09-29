// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import Reflux from 'reflux';
import Config from '../../../module-production/js/config.js';
import DevNavigationOptions from '../data/devNavigationOptions.js';
import siteConfigActions from '../actions/site-config-actions.js';
import SettingsActions from '../actions/dev-settings-actions.js';
import SharedActions from '../../../shared-actions.js';
import mockAddCrForm from '../data/mockAddCrForm.js';
import _ from 'lodash';


var SettingsStore = Reflux.createStore({

  listenables: [siteConfigActions, SettingsActions],

  getInitialState: function(){
    return this.state;
  },

  state: {
    selectedTab: null,
    sites: [],
    registries: {},
    dataServiceBroker: {
        dsbsByURN: {},
        selectedDsb: {},
        selectedPlan: {}
    },
    selectedRegistry: null,
    selectedSite: null,
    tabNames: {
      appRepositories: { key: 'appRepositories', name: 'app repositories' },
      dataServiceBroker: { key: 'dataServiceBroker', name: 'data service broker' },
      copyRepositoryBroker: { key: 'copyRepositoryBroker', name: 'copy repository broker' }
    },
    dsbDetailsDialog: {
      isRender: false,
      infoMenu: {
        isRender: false,
        description: ''
      }
    },
    addDsbDialog: {
      isRender: false
    },
    confirmRemoveCrbDialog: {
      isRender: false,
      selectedCrb: {}
    },
    addCrbDialog: {
      isRender: false
    },
    addCrDialog: {
      isRender: false,
      form: {},
      repo: {},
      isMock: true
    },
    confirmRemoveDsbDialog: {
      isRender: false,
      selectedDsb: {}
    },
    addArtifactRegistry: {
      isRender: false,
      stepNames: {selectArtifact: 'selectArtifact', save: 'save'},
      steps: {
        selectArtifact: {
          isRender: false,
          types: {
            maven: {
              name: 'maven',
              isSelected: false,
              data: {
                siteUrn: "site",
                url: null,
                password: null,
                name: null,
                username: null
              }
            },
            docker: {
              name: 'docker',
              isSelected: false,
              disabled: true
            },
            git: {
              name: 'git',
              isSelected: false,
              disabled: true
            },
            custom: {
              name: 'custom',
              isSelected: false,
              data: {
                url: '',
                name: ''
              }
            }
          },
          selectedArtifact: {}
        },
        save: {
          isRender: false,
          validation: {}
        }
      },
      error: "",
      currentStepIndex: 0,
      currentStep: null,
    },
    cofirmRemoveDialog: {
      isRender: false,
      registryName: ""
    }
  },

  init: function(){
    this.initiateAddArtifactWizard();

    // handle refresh when dialog render
    if(this.isAddArtifactDialogRender()){
      this.navigateToAddArtifact();
    }

    // on site config refresh restore url to origin
    if(Config.getCurrentHash().location ===
        DevNavigationOptions.siteConfig.location.substring(1)){
      SharedActions.navigate({
        module: DevNavigationOptions.module,
        location: DevNavigationOptions.siteConfig.location,
        subLocation: ''
      });
    }

    // add cr form from mock until api returns
    // object to create the form by
    if(this.state.addCrDialog.isMock){
      this.state.addCrDialog.form = mockAddCrForm;
    }else{
      // fetch form object to create add cr dialog from
    }

  },

  onShowAddCrbDialog(bool) {
    this.state.addCrbDialog.isRender = bool;
    this.trigger(this.state);
  },

  onShowAddCrDialog(bool, repo) {
    this.state.addCrDialog.isRender = bool;
    this.state.addCrDialog.repo = repo;
    this.trigger(this.state);
  },

  onShowConfirmRemoveDsbDialog(bool, dsb) {
    this.state.confirmRemoveDsbDialog.isRender = bool;
    if(dsb){
      this.state.confirmRemoveDsbDialog.selectedDsb = dsb;
    }
    this.trigger(this.state);
  },

  onAddCr(body) {
    const data = {};
    const repo = this.state.addCrDialog.repo;
    data.siteId = this.state.selectedSite.id;
    data.crbUrn = repo.urn;
    data.crName = repo.name;
    data.crProperties = body;

    const options = {
      url: `${APISERVER}/hub-web-api/commands/add-copy-repository`,
      contentType: 'application/json',
      data: data,
      method: 'POST'
    }

    Config.request(options, response => {
      console.log(response)
      this.onShowAddCrDialog(false);
    }, error => {
      console.log(error)
    })
  },

  onAddCrb(urn, url) {
    const options = {
      url: `${APISERVER}/hub-web-api/commands/add-crb`,
      method: 'POST',
      contentType: 'application/json',
      data: { crbUrn: urn, crbUrl: url, siteId: this.state.selectedSite.id }
    }

    Config.request(options, response=>{
      this.getRegistries();
      this.onShowAddCrbDialog(false);
    }, error=>{})
  },

  onShowConfirmDeleteCrbDialog(bool ,crb) {
    if(bool){
      this.state.confirmRemoveCrbDialog.isRender = true;
      this.state.confirmRemoveCrbDialog.selectedCrb = crb;
    }else{
      this.state.confirmRemoveCrbDialog.isRender = false;
    }
    this.trigger(this.state);
  },

  onConfirmDeleteCrb(urn, url) {
    const options = {
      url: `${APISERVER}/hub-web-api/commands/remove-crb`,
      method: 'POST',
      contentType: 'application/json',
      data: { crbUrn: urn, siteId: this.state.selectedSite.id }
    }

    Config.request(options, response=>{
      this.getRegistries();
      this.onShowConfirmDeleteCrbDialog(false);
    }, error=>{})
  },

  onAddDsb(urn, url) {
    const options = {
      url: `${APISERVER}/hub-web-api/commands/add-dsb`,
      method: 'POST',
      contentType: 'application/json',
      data: { dsbUrn: urn,  dsbUrl: url, siteId: this.state.selectedSite.id }
    }

    Config.request(options, response=>{
      this.getRegistries();
      this.onShowAddDsbDialog(false);
    }, error=>{})
  },

  onDeleteDsb(dsb) {
    const options = {
      url: `${APISERVER}/hub-web-api/commands/remove-dsb`,
      method: 'POST',
      contentType: 'application/json',
      data: { dsbUrn: dsb.urn, siteId: this.state.selectedSite.id }
    }

    Config.request(options, response=>{
      this.getRegistries();
      this.onShowConfirmRemoveDsbDialog(false);
    }, error=>{
      console.log(error)
    })

  },

  onShowAddDsbDialog(bool){
    this.state.addDsbDialog.isRender = bool;
    this.trigger(this.state);
  },

  onShowDsbDialog(dsbURN) {
    this.state.dsbDetailsDialog.isRender = true;
    this.state.dataServiceBroker.selectedDsb = this.state.dataServiceBroker.dsbsByURN[dsbURN];
    this.trigger(this.state);
  },

  onHideDsbDialog() {
    this.state.dsbDetailsDialog.isRender = false;
    this.trigger(this.state);
  },

  onShowDsbPlanInfoMenu() {
    this.state.dsbDetailsDialog.infoMenu.isRender = true;
    this.trigger(this.state);
  },

  onHideDsbPlanInfoMenu() {
    this.state.dsbDetailsDialog.infoMenu.isRender = false;
    this.trigger(this.state);
  },

  navigateToAddArtifact() {
    SharedActions.navigate({
      module: DevNavigationOptions.module,
      location: DevNavigationOptions.siteConfig.location,
      subLocation: DevNavigationOptions.siteConfig.subLocation.addArtifact
    });
  },

  onSetSelectedArtifact(artifactName) {
    this.clearSelectedArtifacts();
    const artifact =   this.state.addArtifactRegistry.steps.selectArtifact.types[artifactName];
    artifact.isSelected = true;
    this.state.addArtifactRegistry.steps.selectArtifact.selectedArtifact = artifact;
    this.removeInlineError();
    this.trigger(this.state);
  },

  clearSelectedArtifacts() {
    _.map(this.state.addArtifactRegistry.steps.selectArtifact.types, artifact=>{
      artifact.isSelected = false;
    });
  },

  isAddArtifactDialogRender() {
    const currentLocation = Config.getCurrentHash().subLocation;
    const addArtifact = DevNavigationOptions.siteConfig.subLocation.addArtifact.substring(1);
    const save = DevNavigationOptions.siteConfig.subLocation.save.substring(1);
    return currentLocation === addArtifact || currentLocation === save;
  },

  onAddArtifact() {
    this.state.addArtifactRegistry.isRender = true;
    SharedActions.navigate({
      module: DevNavigationOptions.module,
      location: DevNavigationOptions.siteConfig.location,
      subLocation: DevNavigationOptions.siteConfig.subLocation.addArtifact
    })
    this.trigger(this.state);
  },

  onSetAddArtifactCurrentStep() {
    const steps = this.state.addArtifactRegistry.steps;
    const keys = Object.keys(steps);
    const isValid = this.validateStep(this.state.addArtifactRegistry.currentStep);
    if(isValid && this.state.addArtifactRegistry.currentStepIndex < _.size(steps) - 1){
      this.state.addArtifactRegistry.currentStepIndex++;
      this.state.addArtifactRegistry.currentStep = keys[this.state.addArtifactRegistry.currentStepIndex];
      this.removeInlineError();
      this.trigger(this.state);
    }
  },

  validateStep(step){
    switch (step) {
      case this.state.addArtifactRegistry.stepNames.selectArtifact:
        this.state.addArtifactRegistry.error = 'select an artifact';
        this.trigger(this.state);
        return !_.isEmpty(this.state.addArtifactRegistry.steps.selectArtifact.selectedArtifact);
        break;
      case this.state.addArtifactRegistry.stepNames.save:
        const selectedRegistryType = this.state.addArtifactRegistry.steps.selectArtifact.selectedArtifact.name;
        const typeMaven = this.state.addArtifactRegistry.steps.selectArtifact.types.maven.name;
        const typeCustom = this.state.addArtifactRegistry.steps.selectArtifact.types.custom.name;
        switch (selectedRegistryType) {
          case typeMaven:
            this.addMaven();
            break;
          case typeCustom:
            this.addCustomRegistry();
            break;

          default:

        }

        return true;
        break;
      default:
    }
  },

  removeInlineError() {
    this.state.addArtifactRegistry.error = '';
    this.trigger(this.state);
  },

  onCloseAddArtifactDialog() {
    this.state.addArtifactRegistry.isRender = false;
    SharedActions.navigate({
      module: DevNavigationOptions.module,
      location: DevNavigationOptions.siteConfig.location,
      subLocation: ''
    });
    this.initiateAddArtifactWizard();
    this.trigger(this.state);
  },

  onEditRegistry(registry) {
    if(registry.type === "mavenRepository"){
      this.onSetSelectedArtifact("maven");
    }
    if(registry.type === "customRest"){
      this.onSetSelectedArtifact("custom");
    }

    // mave
    this.state.addArtifactRegistry.steps.selectArtifact.types.maven.data.name = registry.name;
    this.state.addArtifactRegistry.steps.selectArtifact.types.maven.data.url = registry.parameters.url;
    this.state.addArtifactRegistry.steps.selectArtifact.types.maven.data.username = registry.parameters.username;

    // custom
    this.state.addArtifactRegistry.steps.selectArtifact.types.custom.data.name = registry.name;
    this.state.addArtifactRegistry.steps.selectArtifact.types.custom.data.url = registry.parameters.url;
    console.log()

    const steps = this.state.addArtifactRegistry.steps;
    const keys = _.keys(steps);
    this.state.addArtifactRegistry.currentStepIndex = keys.indexOf('save');
    this.state.addArtifactRegistry.currentStep = keys[this.state.addArtifactRegistry.currentStepIndex];
    this.trigger(this.state);
    this.navigateToAddArtifact();
  },

  initiateAddArtifactWizard() {

    const types = this.state.addArtifactRegistry.steps.selectArtifact.types;
    _.forEach(types, type=>{
      type.data = {}
    });

    this.state.addArtifactRegistry.currentStepIndex = 0;
    const steps = this.state.addArtifactRegistry.steps;
    this.state.addArtifactRegistry.currentStep = _.keys(steps)[0];
    this.state.addArtifactRegistry.steps.selectArtifact.selectedArtifact = {};
    this.clearSelectedArtifacts();
    this.trigger(this.state);
  },

  onReceiveSites(sites) {

    this.state.sites = sites;
    const firstSiteId = _.keys(this.state.sites)[0];
    // set default site
    this.setSelectedSite(firstSiteId);
    // set default registry
    this.setSelectedRegistry(firstSiteId);
    // set default tab
    this.onUserSelectedTab(_.keys(this.state.tabNames)[0]);

    this.getRegistries();

  },

  onUserSelectedSite(siteId) {
    this.state.selectedSite = this.state.sites[siteId];
    this.state.selectedRegistry = this.state.registries[siteId];
    this.trigger(this.state);
  },

  onUserSelectedTab(tab) {
    this.state.selectedTab = tab;
    this.trigger(this.state);
  },

  getRegistries() {

    const that = this;
    const options = {
      url: `${APISERVER}/hub-web-api/site-config`,
      method: 'GET'
    };
    Config.request(options, response=>{
      response.forEach(registry=>{
        that.state.registries[registry.id] = registry;
      });
      that.setSelectedRegistry(that.state.selectedSite.id);
      that.trigger(that.state)
    }, error=>{
      console.log('error: ', error)
    });

  },

  // HANDLE MAVEN

  onChangeUserNameMaven(username) {
    this.state.addArtifactRegistry.steps.save.validation['username'] = undefined
    this.state.addArtifactRegistry.steps.selectArtifact.types.maven.data.username = username;
    this.trigger(this.state);
  },
  onChangePasswordMaven(password) {
    this.state.addArtifactRegistry.steps.save.validation['password'] = undefined
    this.state.addArtifactRegistry.steps.selectArtifact.types.maven.data.password = password;
    this.trigger(this.state);
  },
  onChangeNameMaven(name) {
    this.state.addArtifactRegistry.steps.save.validation['name'] = undefined
    this.state.addArtifactRegistry.steps.selectArtifact.types.maven.data.name = name;
    this.trigger(this.state);
  },
  onChangeUrlMaven(url) {
    this.state.addArtifactRegistry.steps.save.validation['url'] = undefined
    this.state.addArtifactRegistry.steps.selectArtifact.types.maven.data.url = url;
    this.trigger(this.state);
  },

  addMaven() {

    const url = this.state.addArtifactRegistry.steps.selectArtifact.types.maven.data.url;
    const username = this.state.addArtifactRegistry.steps.selectArtifact.types.maven.data.username;
    const name = this.state.addArtifactRegistry.steps.selectArtifact.types.maven.data.name;
    const password = this.state.addArtifactRegistry.steps.selectArtifact.types.maven.data.password;

    const data = {
      name: name,
      siteId: this.state.selectedSite.id,
      url: url,
      password: password,
      username: username
    }

    var isValid = this.validateMaven(data);

    if(isValid){
      const options = {
        url: `${APISERVER}/hub-web-api/commands/add-maven-artifact-registry`,
        method: 'POST',
        contentType: 'application/json',
        data: data
      }

      Config.request(options, response=>{
        this.onCloseAddArtifactDialog();
        this.getRegistries();
        this.setSelectedRegistry(this.state.selectedSite.id);
      }, error=>{
        console.log(error)
      });
    }
  },

  onClearValidations(){
    this.state.addArtifactRegistry.steps.selectArtifact.types.maven.data.url = '';
    this.state.addArtifactRegistry.steps.selectArtifact.types.maven.data.username = '';
    this.state.addArtifactRegistry.steps.selectArtifact.types.maven.data.name = '';
    this.state.addArtifactRegistry.steps.selectArtifact.types.maven.data.password = '';

    this.state.addArtifactRegistry.steps.save.validation = {};
  },

  // HANDLE INLINE ERRORS
  validateMaven(data) {
    // this.validateMavenUsername(data.username);
    // this.validateMavenPassword(data.password);
    this.validateMavenUrl(data.url);
    this.validateMavenName(data.name);

    // return validation boolian
    return !this.isEmpty(data.url) &&
           !this.isEmpty(data.name);
  },

  validateMavenUsername(username){
    const isEmpty = this.isEmpty(username);
    if(isEmpty){
      this.populateMavenValidation({
        name: 'username',
        value: 'missing user name'
      });
    }
  },

  validateMavenPassword(password){
    const isEmpty = this.isEmpty(password);
    if(isEmpty){
      this.populateMavenValidation({
        name: 'password',
        value: 'missing password'
      });
    }
  },

  validateMavenUrl(url){
    const isEmpty = this.isEmpty(url);
    if(isEmpty){
      this.populateMavenValidation({
        name: 'url',
        value: 'missing url'
      });
    }
  },

  validateMavenName(name){
    const isEmpty = this.isEmpty(name);
    if(isEmpty){
      this.populateMavenValidation({
        name: 'name',
        value: 'enter name'
      });
    }
  },

  isEmpty(string){
    return string === null ||
           string === undefined ||
           string.length === 0;
  },

  populateMavenValidation(object){
    const name = object.name;
    this.state.addArtifactRegistry.steps.save.validation[name] = object.value;
    this.trigger(this.state);
  },

  // HANDLE CUSTOM REGISTRY
  onUserChangedCustomRegistryUrl(url) {
    this.state.addArtifactRegistry.steps.selectArtifact.types.custom.data.url = url;
    this.trigger(this.state);
  },

  onUserChangedCustomRegistryName(name) {
    this.state.addArtifactRegistry.steps.selectArtifact.types.custom.data.name = name;
    this.trigger(this.state);
  },

  addCustomRegistry() {

    // POST TO commands/add-custom-registry
    const options = {
      url: `${APISERVER}/hub-web-api/commands/add-custom-artifact-registry`,
      method: 'POST',
      contentType: 'application/json',
      data: {
        url: this.state.addArtifactRegistry.steps.selectArtifact.types.custom.data.url,
        name: this.state.addArtifactRegistry.steps.selectArtifact.types.custom.data.name,
        siteId: this.state.selectedSite.id
      }
    }

    Config.request(options, response=>{
      this.onCloseAddArtifactDialog();
      this.getRegistries();
      this.setSelectedRegistry(this.state.selectedSite.id);
    }, error=>{
      console.log(error)
    })

  },

  onShowCofirmRemoveDialog(registryName) {
    this.state.cofirmRemoveDialog.isRender = true;
    this.state.cofirmRemoveDialog.registryName = registryName;
    this.trigger(this.state);
  },

  onHideCofirmRemoveDialog() {
    this.state.cofirmRemoveDialog.isRender = false;
    this.trigger(this.state);
  },

  onRemoveMavenRegistry(registryName) {

    const options = {
      url: `${APISERVER}/hub-web-api/commands/remove-artifact-registry`,
      method: 'POST',
      contentType: 'application/json',
      data: {
        siteId: this.state.selectedSite.id,
        name: registryName
      }
    }

    Config.request(options, response=>{
      this.getRegistries();
      this.setSelectedRegistry(this.state.selectedSite.id);
    }, error=>{
      console.log(error)
    })
  },

  setSelectedRegistry(siteId) {
    this.state.selectedRegistry = this.state.registries[siteId];
    if(this.state.selectedRegistry){
      this.setDsbsByRUN(this.state.selectedRegistry.dsbs);
    }
    this.trigger(this.state);
  },

  setDsbsByRUN(dsbs) {
    dsbs.forEach(dsb=>{
      return this.state.dataServiceBroker.dsbsByURN[dsb.urn] = dsb;
    });
  },

  onSetSelectedDsbPlan(planName) {
    let selectedPlan = this.state.dataServiceBroker.selectedDsb.plans.filter(plan=>{
      return plan.name === planName
    })[0]
    this.state.dataServiceBroker.selectedPlan = selectedPlan;
    this.trigger(this.state);
  },

  setSelectedSite(siteId) {
    this.state.selectedSite = this.state.sites[siteId];
    this.trigger(this.state);
  }

});

export default SettingsStore;
