// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import LabelA from '../../../../assets/images/labels/label-a.png';
import EntryPointIcon from '../../../../assets/images/appTopology/entry-point.png';
import SettingsIcon from '../../../../assets/images/header/settings.png';


var EntryPoint = React.createClass({
   
    render: function(){
        return(
        
            <div className="entry-point-container">								
			
                <div className="label">
                    <img src={LabelA} />
                    <span>Entry Point</span>
                </div>

                <div className="inside">
                    <div className="icon"><img src={EntryPointIcon}/></div>
                    Dynamic Load Balancer
                    <img src={SettingsIcon} className="settings-icon"/>
                </div>

            </div>

            
        )
    }
    
});

export default EntryPoint;