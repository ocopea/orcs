// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import AddMaven from './add-maven.comp.js';
import AddCustom from './add-custom.comp.js';


let SaveArtifactRegistry = React.createClass({
  render() {
    const baseClass = this.state.baseClass;
    return(
      <div className={baseClass}>
        {
          this.props.selectedArtifact.name === this.props.types.maven.name ?
            <AddMaven
              selectedArtifact={this.props.selectedArtifact}
              baseClass={this.state.baseClass}
              validations={this.props.mavenValidations}/>
          :
          this.props.selectedArtifact.name === this.props.types.custom.name ?
            <AddCustom selectedArtifact={this.props.selectedArtifact}/>
          :
          null
        }
      </div>
    )
  },

  getInitialState() {
    return {
      baseClass: "Add-artifact__save"
    }
  }
});

export default SaveArtifactRegistry;
