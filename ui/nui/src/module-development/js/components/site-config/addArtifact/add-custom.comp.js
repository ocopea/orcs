// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import SiteConfigActions from '../../../actions/site-config-actions.js';


let AddCustomRegistry = React.createClass({
  render() {
    const baseClass = "Add-artifact__custom";
    console.log(this.props.selectedArtifact)
    return(
      <div className={baseClass}>
        <div className={`${baseClass}__title dialog-title`}>add custom</div>
        <div className={`${baseClass}__inside`}>
          <section className={`${baseClass}__inside__section`}>
            <div className={`${baseClass}__inside__section__label`}>url</div>
            <input
              type="text"
              value={this.state.url}
              className={`${baseClass}__inside__section__input input`}
              onChange={this.userChangeUrl}/>
          </section>
          <section className={`${baseClass}__inside__section`}>
            <div className={`${baseClass}__inside__section__label`}>name</div>
            <input
              type="text"
              value={this.state.name}
              className={`${baseClass}__inside__section__input input`}
              onChange={this.userChangeName}/>
          </section>
        </div>
      </div>
    )
  },

  getInitialState() {
    return {
      url: this.props.selectedArtifact.data.url,
      name: this.props.selectedArtifact.data.name
    }
  },

  userChangeUrl(e) {
    this.setState({
      url: e.target.value
    });
    SiteConfigActions.userChangedCustomRegistryUrl(e.target.value);
  },

  userChangeName(e) {
    this.setState({
      name: e.target.value
    });
    SiteConfigActions.userChangedCustomRegistryName(e.target.value);
  }

});

export default AddCustomRegistry;
