// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';


let DsbInfoMenu = React.createClass({
  render() {

    const plan = this.props.selectedPlan;
    // console.log(plan)
    return (
      <div className="Dsb-info-menu info-menu">
        <div className="Dsb-info-menu__title info-menu">{plan.name}</div>
        <ul className="Dsb-info-menu__description info-menu">
          <li>{plan.description}</li>
        </ul>
      </div>
    )
  }
});

export default DsbInfoMenu;
