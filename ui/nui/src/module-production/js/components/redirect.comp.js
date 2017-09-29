// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';


let Redirect = React.createClass({
   
    render: function(){
        
        return (
            <div id="redirect-container">
                <div className="inside">
                    <p>The system detected your are using {this.props.browserName} browser.</p>
                    <p>This site is best viewed in google chrome</p>
                    <p>Get <a href="http://www.google.com/chrome">Chrome</a></p>
                </div>
            </div>
        )
    },  
    
});

export default Redirect;