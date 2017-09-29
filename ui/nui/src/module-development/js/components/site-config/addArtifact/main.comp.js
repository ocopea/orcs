// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import ChooseType from './choose-type.comp.js';
import Save from './save.comp.js';
import SiteConfigActions from '../../../actions/site-config-actions.js';


let AddArtifactRegistry = React.createClass({
  render() {
    // console.log(this.props)
    return(
      <div className="Add-artifact">
        {
          this.props.currentStep === this.props.steps.selectArtifact ?
            <ChooseType types={this.props.types}/>
          :
          this.props.currentStep === this.props.steps.save ?
            <Save
              types={this.props.types}
              selectedArtifact={this.props.selectedArtifact}
              mavenValidations={this.props.mavenValidations}/>
          :
          null
        }
        <div className="Add-artifact__inline-error">{this.props.error}</div>

        <div className="Add-artifact__buttons-container">
          <button type="button" onClick={this.userClickOnCancel} className="button button-secondary">cancel</button>
          <button type="button" onClick={this.userClickOnNext} className="button button-primary">next</button>
        </div>
      </div>
    )
  },

  userClickOnNext() {
    SiteConfigActions.setAddArtifactCurrentStep()
  },

  userClickOnCancel() {
    SiteConfigActions.closeAddArtifactDialog();
  }
});

export default AddArtifactRegistry;
