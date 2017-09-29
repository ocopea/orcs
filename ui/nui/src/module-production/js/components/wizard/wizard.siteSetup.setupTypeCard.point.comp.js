// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';


let Point = React.createClass({
    
    render: function(){
        
        //console.log(this.props);
        return(
            <div className="point">
                <div className="point-inside"></div>
            </div>
        )
    },
});

export default Point;