// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import SiteConfigActions from '../../actions/site-config-actions.js';
import RegistryTypes from './registryTypes.js';
import IconMaven from '../../../assets/images/site-config/maven.png';
import IconCustom from '../../../assets/images/site-config/custom.png';


let AppRepositories = React.createClass({
  render() {
    
    const baseClass = "Site-config__inside__repositories";
    return(
      <div className={baseClass}>
        {
          this.props.registries.map((registry, i)=>{

            return  <div className={`${baseClass}__registry`} key={i}>
                      <div className={`${baseClass}__registry__left-container`}>
                        <span className={`${baseClass}__registry__left-container__logo`}>
                          <img
                            src={this.getIcon(registry.type)}
                            className={`${baseClass}__registry__left-container__logo__img ${registry.type}`}/>
                        </span>
                        <span className={`${baseClass}__registry__left-container__name`}>{registry.name}</span>
                        <span className={`${baseClass}__registry__left-container__description`}></span>
                      </div>
                      <span className={`${baseClass}__registry__icons`}>
                        <span
                          onClick={this.userClickOnEditRegistry.bind(this, registry)}
                          className={`icon-edit ${baseClass}__registry__icons__icon link`}></span>
                        <span
                          onClick={this.userClickOnDeleteRegistry.bind(this, registry)}
                          className={`icon-delete ${baseClass}__registry__icons__icon link`}></span>
                      </span>
                    </div>
          })
        }

        <div
         onClick={this.addArtifact}
         className={`${baseClass}__registry ${baseClass}__registry--last`}>
          <span
            className={`${baseClass}__registry__left-container__logo Plus`}>
            <span></span>
            <span></span>
          </span>
          <span className={`${baseClass}__registry__left-container__name`}>Add New</span>
        </div>

      </div>
    )
  },

  userClickOnEditRegistry(registry) {
    SiteConfigActions.editRegistry(registry);
  },

  userClickOnDeleteRegistry(registry) {
    SiteConfigActions.showCofirmRemoveDialog(registry.name);
  },

  addArtifact() {
    SiteConfigActions.addArtifact();
  },

  getInitialState() {
    return {
      artifactTypes: {
        custom: "customRest"
      }
    }
  },

  getIcon(type) {

    switch (type) {
      case RegistryTypes.maven:
        return IconMaven;
        break;
      case RegistryTypes.custom:
        return IconCustom;
        break;
      default:

    }
  }
});

export default AppRepositories;
