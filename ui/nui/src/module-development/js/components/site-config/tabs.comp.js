// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import _ from 'lodash';

let SiteConfigTabs = React.createClass({
  render() {
    const baseClass = this.props.baseClass;
    return(
      <div className="Site-config__inside__tabs">
        {
          _.map(this.props.list, (tab, key)=>{
            const baseClass = this.props.className;
            let className =   this.isSelected(key) ?
                              `${baseClass} ${baseClass}--selected` : baseClass
            return  <div
                      key={key}
                      onClick =     {tab.onClick}
                      className =   {className}>
                        <span className={`${baseClass}__icon ${tab.icon}`}></span>
                        <span>{tab.name}</span>
                    </div>
          })
        }
      </div>
    )
  },

  isSelected(tabName) {
    return tabName === this.props.selected
  }

});

export default SiteConfigTabs;
