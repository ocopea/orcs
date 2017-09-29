// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import ImgError from '../../../assets/images/main-footer/inline-error.png';

let InlineError = React.createClass({
    
    getImg: function(){
        if(this.props.error != ""){
            return <img src={ImgError} id="inline-error-img"/>
        }else{
            return 
        }
    },
    
    render: function(){
        //console.log(this.props.error)
        return (
            <div className="inline-error">
                {this.getImg()}
                {this.props.error}
            </div>
        )
    }    
});

export default InlineError;