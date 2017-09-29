// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import OpenIcon from '../../../assets/images/open-icon.svg';
import Isvg from 'react-inlinesvg';
import _ from 'lodash';


var InventoryManager = React.createClass({

  getInventory: function(){
    if(!_.isEmpty(this.props.selectedElement)){
        switch(this.props.selectedElement.componentType){
          case 'service':
            return(
              <div className="Dashboard__inside__inventory-manager__inside__container">
                <div className={"Dashboard__inside__inventory-manager__inside__container__circle--version "+
                                "Dashboard__inside__inventory-manager__inside__container__circle"}>
                  <span className="Dashboard__inside__inventory-manager__inside__container__circle__number">
                    {this.props.selectedElement.imageVersion || this.props.selectedElement.version}
                  </span>
                  <span className={"Dashboard__inside__inventory-manager__inside__container__circle--version__span "+
                                   "Dashboard__inside__inventory-manager__inside__container__circle__span"}>
                    version
                  </span>
                </div>
                <div className={"Dashboard__inside__inventory-manager__inside__container__circle--instances "+
                                "Dashboard__inside__inventory-manager__inside__container__circle"}>
                  <span className="Dashboard__inside__inventory-manager__inside__container__circle__number">
                    4
                  </span>
                  <span className={"Dashboard__inside__inventory-manager__inside__container__instances__span "+
                                   "Dashboard__inside__inventory-manager__inside__container__circle__span"}>
                    instances
                  </span>
                </div>
              </div>
            )
            break;
          case 'dependency':
            return(
              <div className="Dashboard__inside__inventory-manager__inside__container">
                <div className={"Dashboard__inside__inventory-manager__inside__container__circle--plan "+
                                "Dashboard__inside__inventory-manager__inside__container__circle"}>
                  <span className={"Dashboard__inside__inventory-manager__inside__container__circle--plan__span "+
                                   "Dashboard__inside__inventory-manager__inside__container__circle__span"}>
                    plan
                  </span>
                </div>
                <div className={"Dashboard__inside__inventory-manager__inside__container__circle--tba "+
                                "Dashboard__inside__inventory-manager__inside__container__circle"}>
                  <span className={"Dashboard__inside__inventory-manager__inside__container__circle--tba__span "+
                                   "Dashboard__inside__inventory-manager__inside__container__circle__span"}>
                    tba
                  </span>
                </div>
              </div>
            )
            break;
        }
    }
  },

  render: function(){
    
    return(
      <div className="Dashboard__inside__inventory-manager">
        <div className="Dashboard__inside__inventory-manager__title">
          <span className="Dashboard__inside__inventory-manager__title__span">
            {this.props.selectedElement.appServiceName || this.props.selectedElement.name || null}
          </span>
        {/* /Dashboard__inside__inventory-manager__title */}
        </div>
        <div className="Dashboard__inside__inventory-manager__inside">
          {this.getInventory()}
        <div className="Dashboard__inside__inventory-manager__inside__footer">
          {

            this.props.selectedElement && !_.isEmpty(this.props.selectedElement) ?
              <div>
                <div className="section link" onClick={this.openServiceEntryPoint}>
                    <span className="icon-link"></span>
                    <label className='open'>open</label>
                </div>
                <div className="section">
                  <span className="Dashboard__inside__inventory-manager__inside__footer__logs__icon">
                    <Isvg src={OpenIcon}
                      className="Dashboard__inside__inventory-manager__inside__footer__logs__icon__image"/>
                  </span>
                  <label>logs</label>
                </div>
              </div>
            : null
          }
        </div>
        {/* /Dashboard__inside__inventory-manager__inside */}
        </div>
      {/* /Dashboard__inside__inventory-manager */}
      </div>
    )
  },

  openServiceEntryPoint() {
    window.open(
      this.props.selectedElement.entryPointUrl,
      "blank"
    )
  }
});

InventoryManager.PropTypes = {
  selectedElement: React.PropTypes.object
}

export default InventoryManager;
