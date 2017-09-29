// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import { Line } from 'rc-progress';
import MockImg from '../../../assets/images/quotas/mock.png';
import Config from '../../../../module-production/js/config.js';


var Quota = React.createClass({

    render: function(){
        return(

            <div>

                <div className="icon-container">
                    <img src={MockImg} />
                </div>

                <span title={this.props.name}>
					{Config.getShortName(this.props.name, 9)}
				</span>
                <span>{this.props.precent+"%"}</span>

                <Line
                    percent={this.props.precent}
                    strokeWidth="2"
                    strokeColor={this.props.strokeColor}
                    trailColor="#ebeff5"
                    trailWidth="2"
                    strokeLinecap="square"/>
            
            </div>

        )

    }

});

export default Quota;
