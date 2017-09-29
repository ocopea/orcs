// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import $ from 'jquery';
import SiteConfigActions from '../../../actions/site-config-actions.js';
import InlineError from './dev-inline-error.comp.js';


let AddMavenRegistry = React.createClass({
  render() {
    const baseClass = this.props.baseClass
    return(
      <div>
        <div className={`${baseClass}__title`}>add maven app registry</div>
        <div className={`${baseClass}__inside`}>
          <section className={`${baseClass}__inside__section ${baseClass}__inside__section--name`}>
            <label className={`${baseClass}__inside__section__label`}>name</label>
            <input
              onChange={this.changeName}
              type="text"
              readOnly={this.state.name ? this.state.name.length > 0 : false}
              value={this.props.selectedArtifact.data.name}
              className={`${baseClass}__inside__section__input ${baseClass}__inside__section__input--name input`} />
            <InlineError error={this.props.validations['name']}/>
          </section>
          <section className={`${baseClass}__inside__section ${baseClass}__inside__section--url`}>
            <label className={`${baseClass}__inside__section__label`}>url</label>
            <input
              onChange={this.changeUrl}
              value={this.state.url}
              type="text"
              className={`${baseClass}__inside__section__input input`} />
            <InlineError error={this.props.validations['url']}/>
          </section>
          <section className={`${baseClass}__inside__section ${baseClass}__inside__section--username`}>
            <label className={`${baseClass}__inside__section__label`}>username</label>
            <input
              onChange={this.changeUserName}
              type="text"
              value={this.state.username}
              className={`${baseClass}__inside__section__input input`} />
            <InlineError error={this.props.validations['username']}/>
          </section>
          <section className={`${baseClass}__inside__section ${baseClass}__inside__section--password`}>
            <label className={`${baseClass}__inside__section__label`}>password</label>
            <input
              onChange={this.changePassword}
              type="password"
              className={`${baseClass}__inside__section__input input`} />
            <InlineError error={this.props.validations['password']}/>
          </section>
        </div>
      </div>
    )
  },

  getInitialState() {
    return {
      url : this.props.selectedArtifact.data.url || '',
      username: this.props.selectedArtifact.data.username || '',
      name: this.props.selectedArtifact.data.name
    }
  },

  componentDidMount(){
    this.setState({
      url : this.props.selectedArtifact.data.url || '',
      username: this.props.selectedArtifact.data.username || '',
      name: this.props.selectedArtifact.data.name
    })
    $(`.${this.props.baseClass}__inside__section__input--name`).focus();
  },

  componentWillUnmount(){
    SiteConfigActions.clearValidations();
  },

  changeUserName(e) {
    this.setState({
      username: e.target.value
    });
    SiteConfigActions.changeUserNameMaven(e.target.value);
  },

  changePassword(e) {
    SiteConfigActions.changePasswordMaven(e.target.value);
  },

  changeName(e) {
    SiteConfigActions.changeNameMaven(e.target.value);
  },

  changeUrl(e) {
    SiteConfigActions.changeUrlMaven(e.target.value);
    this.setState({
      url: e.target.value
    })
  }

});

export default AddMavenRegistry;
