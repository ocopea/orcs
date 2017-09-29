// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import Actions from '../actions.js';

let ErrorDialog = React.createClass({
    
    dismissDialog: function(){
        Actions.hideErrorDialog();
    },
    
    render: function(){
        
        return (
            
            <div id="error-dialog">
                <div className="title">system notification</div>
                <div className="inside">
                    <div className="content"><span>{this.props.content}</span></div>
                    <button type="button" 
                        className="button button-primary" 
                        onClick={ this.dismissDialog }>dismiss</button>
                </div>
                
            </div>
            
        )
    }
    
});

export default ErrorDialog;