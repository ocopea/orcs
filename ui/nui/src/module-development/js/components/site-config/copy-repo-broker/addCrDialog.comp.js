// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import SharedActions from '../../../../../shared-actions.js';
import SiteConfigActions from '../../../actions/site-config-actions.js';
import DevNavigationOptions from '../../../data/devNavigationOptions.js';
import _ from 'lodash';


let AddCrDialog = React.createClass({
  render() {

    const baseClass = 'Add-cr-dialog';
    return(
      <div className={`${baseClass}`}>
        {/* Title */}
        <div className={`${baseClass}__title`}>
          <span>add copy repo</span>
          <span
            onClick={this.onDismiss}
            className={`${baseClass}__title__icon-close icon-close link`}>
              <span></span>
              <span></span>
          </span>
        </div>
        {/* Inside */}
        <div className={`${baseClass}__inside`}>
          {this.getForm()}
          <div className={`${baseClass}__inside__footer`}>
            <button onClick={this.onDismiss} type="button" className="button button-secondary">cancel</button>
            <button onClick={this.onSubmit} type="button" className="button button-primary">add</button>
          </div>
        </div>
      </div>
    )
  },

  getInitialState() {
    return {
      types: {
        string: 'text',
        password: 'password',
        int: 'number'
      },
      elements: {
        input: 'input',
        select: 'select'
      },
      crProperties: {},
      body: {}
    }
  },

  getForm() {
    const fields = this.props.form.fields;
    let secctions = fields.map(field=>{
      const fieldType = this.state.types[field.type];
      const label = field.name;
      const section = {type: fieldType, label: label, element: field.element, prop: field.prop};
      this.state.crProperties[field.prop] = field;
      return this.createSection(section);
    });
    return secctions;
  },

  createSection(sectionObject) {
    return <section key={sectionObject.prop}>
      <label>{sectionObject.label}</label>
      {this.getElement(sectionObject)}
    </section>
  },

  getElement(sectionObject) {
    switch (sectionObject.element) {
      case this.state.elements.input:
        return <input
                ref={sectionObject.prop}
                className="input" type={sectionObject.type} />
        break;
      default:
    }
  },

  onDismiss() {
    SharedActions.navigate({
      module: DevNavigationOptions.module,
      location: DevNavigationOptions.siteConfig.location,
      subLocation: ''
    })
  },

  onSubmit() {
    const crProperties = this.state.crProperties;
    const inputs = this.refs;
    _.forEach(crProperties, crProp=>{
      this.state.body[crProp.prop] = inputs[crProp.prop].value;
    })
    const body = this.state.body;
    SiteConfigActions.addCr(body);
  }

});

export default AddCrDialog;
