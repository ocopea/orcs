// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import _ from 'lodash';
import $ from 'jquery';
import IconSavedImage from '../../../../assets/images/wizard/image/saved-image-icon.svg';
import IconBackup from '../../../../assets/images/wizard/image/backup-icon.svg';
import Isvg from 'react-inlinesvg';
import DevWizardActions from '../../../actions/dev-wizard-actions.js';

var DevWizardImageTabs = React.createClass({

  userClickOnTab: function(e){
    var selectedTab;
    if(e.target.classList.contains("tab")){
      selectedTab = e.target.innerText;
    }else{
      $(e.target).parents().each((index, parent)=>{
        if(parent.classList.contains("tab")){
          selectedTab = parent.innerText;
        }
      });
    }
    DevWizardActions.setImageSelectedTab(selectedTab);
  },

  getTabClassName: function(key){
      var className = this.props.data.tabs[key] ==
                   this.props.data.selectedTab ?
                   "tab selected " + this.props.data.tabs[key] :
                   "tab " + this.props.data.tabs[key]
     return className;
  },

  getTabs: function(){

    var tabs = [];
    for (var key in this.props.data.tabs){
      var tab = <div key={key}
                  className={this.getTabClassName(key)}
                  onClick={this.userClickOnTab}>
                    <span className="label">{this.props.data.tabs[key]}</span>
                    <span className="tab-icon">
                      <Isvg src={this.props.data.tabs[key] ==
                        "SAVED IMAGE" ? IconSavedImage : IconBackup} />
                    </span>
                </div>
      tabs.push(tab);
    }
    return tabs;
  },

  userClickOnBlankImage: function(){
    DevWizardActions.userClickOnBlankImage();
  },

  render: function(){
    //console.log(this.props.data.tabs)
    return (
      <div className="tabs">
        {this.getTabs()}
        <button className="create-blank"
          onClick={this.userClickOnBlankImage}>
          <label>blank</label>
          <div className="plus">
            <span></span>
            <span></span>
          </div>
        </button>
      </div>
    )
  }
});

export default DevWizardImageTabs;
