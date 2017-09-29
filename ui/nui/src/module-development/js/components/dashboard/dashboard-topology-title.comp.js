// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';


var DashboardTopologyTitle = React.createClass({
  render: function(){
    return(
      <div className="Dashboard__inside__topology__title">
        <span className="Dashboard__inside__topology__title__span">topology</span>
        <div className="Dashboard__inside__topology__title__legend">
          <div className={"Dashboard__inside__topology__title__legend__application "+
                         "Dashboard__inside__topology__title__legend__box"}>
            <span className="Dashboard__inside__topology__title__legend__box__span">application</span>
            <span className="app-i"></span>
          </div>
          <div className={"Dashboard__inside__topology__title__legend__service "+
                         "Dashboard__inside__topology__title__legend__box"}>
            <span className="Dashboard__inside__topology__title__legend__box__span">service</span>
            <span className="service-i"></span>
          </div>
        {/* /Dashboard__inside__topology__title__legend */}
        </div>
      {/* /Dashboard__inside__topology__title */}
      </div>      
    )
  }
});

export default DashboardTopologyTitle;
