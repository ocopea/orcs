// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import _ from 'lodash';
import SiteConfigActions from '../../../actions/site-config-actions.js';
import IconMaven from '../../../../assets/images/site-config/maven.png';
import IconDocker from '../../../../assets/images/site-config/docker.png';
import IconGit from '../../../../assets/images/site-config/git.png';


let TypeArtifactRegistry = React.createClass({
  render() {
    return(
      <div className={this.state.baseClass}>
        <div className={`${this.state.baseClass}__title`}>
          <span>choose app repository type</span>
          <span onClick={this.closeDialog} className="icon-close link"></span>
        </div>
        <div className={`${this.state.baseClass}__inside`}>
          <table>
            <tbody>
              { this.getRows() }
            </tbody>
          </table>

        </div>
      </div>
    )
  },

  getInitialState() {
    return {
      baseClass: "Add-artifact__type",
      types: {
        maven: 'maven',
        git: 'git',
        docker: 'docker',
        custom: 'custom'
      }
    }
  },

  closeDialog() {
    SiteConfigActions.closeAddArtifactDialog();
  },

  userSelectedArtifact(artifactName) {
    SiteConfigActions.setSelectedArtifact(artifactName)
  },

  getRows() {
    return _.map(this.props.types, (type, i)=>{
      const baseClass = "Naz-checkbox";
      let className = type.isSelected ? `${baseClass} selected` : baseClass;
      let rowClassName = `${this.state.baseClass}__inside__row`;
      type.disabled ? rowClassName = `${rowClassName} ${rowClassName}--disabled` : rowClassName;

      return  <tr
               key ={i}
               className={rowClassName}
               onClick={this.userSelectedArtifact.bind(this, type.name)}>

                <td className="Naz-checkbox-container">
                  <div className={className}>
                    {
                      type.isSelected ?
                        <span className="icon-check"></span>
                      : null
                    }
                  </div>
                </td>
                <td className={`${this.state.baseClass}__inside__row__logo-container`}>
                  <div className={`${this.state.baseClass}__inside__row__logo-container__logo`}>
                    <img
                      src={this.getIcon(type.name)}
                      className={`${this.state.baseClass}__inside__row__logo-container__logo__img ${type.name}`}/>
                  </div>
                </td>
                <td>
                  <div className={`${this.state.baseClass}__inside__row__description`}>
                    {type.name}
                  </div>
                </td>

              </tr>
    })
  },

  getIcon(type) {
    switch (type) {
      case this.state.types.maven:
        return IconMaven;
        break;
      case this.state.types.docker:
        return IconDocker;
        break;
      case this.state.types.git:
        return IconGit;
        break;
      default:

    }
  }
});

export default TypeArtifactRegistry;
