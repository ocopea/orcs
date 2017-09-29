// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import Config from '../../../../module-production/js/config.js';
import _ from 'lodash';
import DevActions from '../../actions/dev-actions.js';
import DevWizardActions from '../../actions/dev-wizard-actions.js';
import SharedImageActions from '../../actions/dev-shared-image-actions.js';
import DevSharedImageActions from '../../actions/dev-shared-image-actions.js';
import AppTopology from '../dev-topology.comp.js';
import DevNavigationOptions from '../../data/devNavigationOptions.js';
import EmailIcon from '../../../assets/images/saved-images/E_MAIL.png';
import Selects from './shared-image-selects.comp.js';


var SharedImage = React.createClass({

  getInitialState: function(){
    return {
      projectName: ""
    }
  },

  getHash: function(e){
    var segments = _.split(window.location.hash, '/'),
        sharedImageID = segments[2],
        sharedByUserID = segments[4];
        if(sharedByUserID !== undefined){
          DevActions.goToSharedImage(sharedImageID, sharedByUserID);
        }

  },

  componentDidMount: function(){
    DevActions.getAppInstances();
    this.getHash();
  },

  componentDidUpdate: function(nextProps){
    if(this.props.image !== nextProps.image){
      DevWizardActions.setSelectedImage(0);
    }
  },

  getImageParams: function(){
    var data = {};
    if(!_.isEmpty(this.props.state.sharedImage) &&
       !_.isEmpty(this.props.state.imageDetails)){
      data.imageName = this.props.state.sharedImage.name;
      data.topologyData = this.props.state.imageDetails;
    }

    return data;
  },

  getSharedByUserFullName: function(maxLength){
    if(this.props.state.sharedImage !== undefined && this.props.state.sharedImage.sharedByUser !== undefined){
      var firstName = this.props.state.sharedImage.sharedByUser.firstName,
          lastName = this.props.state.sharedImage.sharedByUser.lastName;

      return {
        shortName: Config.getShortName(`${firstName} ${lastName} `, maxLength),
        fullName: `${firstName} ${lastName}`
      }
    }
  },

  getSharedByUserAvatar: function(){
    if(this.props.state.sharedImage !== undefined && this.props.state.sharedImage.sharedByUser !== undefined){
      return <img
                src={APISERVER+"/hub-web-api/user/" +
                this.props.state.sharedImage.sharedByUser.id + "/avatar"}
                className="Shared-image__inside__title__avatar"/>
    }
  },

  getAppTemplate: function(){
    var appTemplates = this.props.state.appTemplates,
        sharedImageAppTemplate;

    if(this.props.state.appTemplates !== undefined && !_.isEmpty(this.props.state.sharedImage)){
      sharedImageAppTemplate = appTemplates[this.props.state.sharedImage.appTemplateId];
    }
    return sharedImageAppTemplate;
  },

  getTemplateImg: function(){
    if(this.getAppTemplate() !== undefined)
      return this.getAppTemplate().img !== undefined ? this.getAppTemplate().img : ""
  },

  getImageDescription: function(){
    var sharedImage = this.props.state.sharedImage;
    if(sharedImage !== undefined && !_.isEmpty(this.props.state.sharedImage)){
      var description = this.props.state.sharedImage.description;
      return description !== undefined &&
             description.length > 0 ?
             Config.getShortName(description, 150) : 'no description entered'
    }
  },

  getDescriptionTitle: function(){
    var sharedImage = this.props.state.sharedImage;
    if(sharedImage !== undefined && !_.isEmpty(this.props.state.sharedImage)){
      var description = this.props.state.sharedImage.description;
      return description !== undefined &&
             description.length > 149 ? description : null;
    }
  },

  getSelectsData: function(){
    var sites = this.props.state.sites.options,
        purposes = this.props.state.purposes.options,
        spaces = this.props.state.spaces.options;
        
    var sitesSelect = this.parseSelectData(
      "Shared-image-selects__select Shared-image-selects__select--site",
      function(){console.log('sites clicked')},
      sites,
      'sites'
    )

    var purposesSelect = this.parseSelectData(
      "Shared-image-selects__select Shared-image-selects__select--purposes",
      function(){console.log('purposes clicked')},
      purposes,
      'purpose'
    )

    var spacesSelect = this.parseSelectData(
      "Shared-image-selects__select Shared-image-selects__select--spaces",
      function(){console.log('spaces clicked')},
      spaces,
      'space'
    )

    return {sitesSelect, purposesSelect, spacesSelect}
  },

  parseSelectData: function(className, onClick, options, type){
    var parsedOptions = _.filter(options, o=>{
            return o.text === undefined ? o.text = o.name : o.text
          });
    var select = {
        className: className,
        onClick: onClick,
        options: parsedOptions,
        type: type
    };
    return select;
  },

  userChangedProjectName: function(e){
    SharedImageActions.changeProjectName(e.target.value)
  },

  getRunButtonClass: function(){
    var baseClass = "Shared-image__inside__footer__buttons-container__button"+
                     " wizard-next link button";

    if(this.props.state.validation.isValid){
      return baseClass
    }else{
      return baseClass + " disabled";
    }
  },

  render: function(){

    var shortUserName = this.getSharedByUserFullName() !== undefined ?
                        this.getSharedByUserFullName(20).shortName : "";
    var fullUserName = this.getSharedByUserFullName() !== undefined ?
                   this.getSharedByUserFullName().fullName : "";
            // console.log(this.getSelectsData().sites)
    return(
      <div className="Shared-image">
        <div className="Shared-image__inside">
          <div className="Shared-image__inside__title">
            {this.getSharedByUserAvatar()}
            <span className="Shared-image__inside__title__content">
              <span className="Shared-image__inside__title__content--name"
                    title={fullUserName.length > 20 ? fullUserName : null}>
                {shortUserName}
              </span>
              shared an image with you
            </span>
            <div className="Shared-image__inside__title__description">
              <div className="Shared-image__inside__title__description__content">
                <span className="Shared-image__inside__title__description__content__template-logo">
                  <img
                    className="Shared-image__inside__title__description__content__template-logo__img"
                    src={EmailIcon}/>
                </span>
                <span className="Shared-image__inside__title__description__content__text">
                  <span className="Shared-image__inside__title__description__content__text__title">
                    {this.props.state.sharedImage !== undefined ? this.props.state.sharedImage.name : null}
                  </span>
                  <span className="Shared-image__inside__title__description__content__text__paragraph"
                    title={this.getDescriptionTitle()}>
                    {this.getImageDescription()}
                  </span>
                </span>
              {/* ./Shared-image__inside__title__description__content */}
              </div>
            {/* ./Shared-image__inside__title__description */}
            </div>
          {/* ./Shared-image__inside__title */}
          </div>

          <div className="Shared-image__inside__change-project-name">
            <input
              type="text"
              name="project-name"
              placeholder="write your app name"
              className="Shared-image__inside__change-project-name__input input"
              onChange={this.userChangedProjectName}/>

            <span className="Shared-image__inside__change-project-name__icon-edit icon-edit"></span>
          {/* ./Shared-image__inside__change-project-name */}
          </div>

          <Selects data={this.getSelectsData()}/>

          <div className="Shared-image__inside__border-container">
            <div className="Shared-image__inside__border-container__border"></div>
          </div>

          {
            !_.isEmpty(this.props.state.imageDetails) &&
            this.getImageParams().topologyData !== undefined?

              <AppTopology
                module={DevNavigationOptions.module}
                state={this.props.appTopologyState.appTopology}
                selectedApp={this.getImageParams().topologyData}
                selectedService={this.props.appTopologyState.appTopology.services.selectedService.service}
                selectedDependency={this.props.appTopologyState.appTopology.dependencies.selectedDependency.dependency}
                servicesTranslate={this.props.appTopologyState.appTopology.services.translate}
                dependenciesTranslate={this.props.appTopologyState.appTopology.dependencies.translate}
                allServices={this.props.allServices}
                currentStepName="show"
                tooltip={this.props.tooltip}
                selectedElement={this.props.appTopologyState.appTopology.selectedElement}
                activeElements={this.props.activeElements}
                isSelectedElementActive={this.props.appTopologyState.appTopology.selectedElement.isActive}
                configuration={this.props.configuration}
                hasDynamicLoadbalancer={true}
              />
            :
            null
          }
          <div className="Shared-image__inside__footer">
            <div className="Shared-image__inside__footer__buttons-container">
              <button
                className={this.getRunButtonClass()}
                onClick={DevSharedImageActions.deploySharedImage}>run</button>
              <button
                  className={"Shared-image__inside__footer__buttons-container__button"+
                                 " wizard-back link"}>back</button>
            </div>
            <span className="Shared-image__inside__footer__inline-error">{this.props.state.validation.msg}</span>
          {/* Shared-image__inside__footer */}
          </div>
        {/* shared-image__inside*/}
        </div>
      {/* shared-image */}
      </div>
    )
  }
});

export default SharedImage;
