// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import Select from 'react-select';
import SharedImageActions from '../../actions/dev-shared-image-actions.js';
import DevWizardActions from '../../actions/dev-wizard-actions.js';
import DeployingProgressActions from '../../actions/deploying-progress-actions.js';
import SettingsActions from '../../actions/dev-settings-actions.js';
import SiteConfigActions from '../../actions/site-config-actions.js';
import styles from 'react-select/dist/react-select.min.css';


var SharedImageSelect = React.createClass({

  getInitialState: function(){
    return {
      selectedOption: ""
    }
  },

  setSelectedOption: function(option){

    this.setState({
      selectedOption: option.value
    });
    this.storeSelectionByType(option);
  },

  storeSelectionByType: function(option){

    switch (this.props.type) {
      case 'site':
        DevWizardActions.setSelectedSite(option.siteId);
        break;
      case 'purpose':
        SharedImageActions.setSelectedPuspose(this.state.selectedOption);
        break;
      case 'space':
        SharedImageActions.setSelectedSpace(option.value);
        DevWizardActions.setSelectedSpace(option.value);
        break;
      case 'add-logs-filter':
        DeployingProgressActions.addFilter(option.value);
        break;
      case 'jira-config-issueType':
        SettingsActions.configureJiraIssueType(option.id)
        break;
      case 'jira-config-project':
        SettingsActions.configureJiraProject(option.id)
        break;
      case 'site-config-dsb-plan':
        SiteConfigActions.setSelectedDsbPlan(option.value)
        break;
    }

  },

  render: function(){

    var placeholder;
    if(!this.props.placeholder){
      placeholder = this.props.options[0] === undefined ? "" : this.props.options[0].value;
    }else{
      placeholder = this.props.placeholder;
    }

    return(
      <div className="Naz-select__container">
        {
          this.props.type !== 'add-logs-filter' ?
            <label className={`Naz-select__container__label`}>{this.props.type}</label>
          :
          null
        }

         <Select
            value={!this.props.hideValue ? this.state.selectedOption : placeholder}
            placeholder={placeholder}
            className={this.props.className+" Naz-select"}
            arrowRenderer={
              function(){
                return <span
                          className={"icon-arrow-dropdown-down Naz-select__toggle link"}
                          ></span>
              }
            }
            clearable={false}
            searchable={false}
            onChange={this.setSelectedOption}
            options={this.props.options}>
         </Select>
      </div>
    )
  }
});

SharedImageSelect.propTypes = {
  className: React.PropTypes.string,
  options: React.PropTypes.array.isRequired // <------- [{value: option.'', label: ''}]
}

export default SharedImageSelect;
