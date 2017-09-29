// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import Actions from '../actions/actions.js';
var GeminiScrollbar = require('react-gemini-scrollbar');

let ErrorDialog = React.createClass({

    dismissDialog: function(){
        Actions.hideErrorDialog();
    },

    render: function(){

        return (

            this.props.data.isRender ?

            <div id="error-dialog">
                <div className="title">system notification</div>
                <div className="inside">
                  <GeminiScrollbar>
                    <div className="content"><span>{this.props.data.content}</span></div>
                  </GeminiScrollbar>
                    <button type="button"
                        className="button button-primary"
                        onClick={ this.dismissDialog }>dismiss</button>
                </div>
            </div>

            :

            null
        )
    }

});

export default ErrorDialog;
