// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import Actions from '../../actions/actions.js';
import $ from 'jquery'


let ToolTip = React.createClass({

    render: function(){
        // console.log(this.props)

        this.props.data.isRender ? this.closeTooltip() : null;

        {
            return (
               this.props.data.isRender ?

                    <div className="tooltip" style={this.getStyle()}>

                        <div className="section copy-size-container">
            							<div className="tooltip-title">Copy size</div>
            							<div id="copy-size">{this.props.data.copySize} GB</div>
      						      </div>

            						<div className="section time-to-restore-container">
            							<div className="tooltip-title">Estimated time to restore</div>
            							<div id="time-to-restore">{this.props.data.timeToRestore} min</div>
            						</div>

            						<div className="section restore-btn-container">
            							<button
                              type="button"
                              id="btn-restore"
                              className="button-primary"
                              onClick={this.userClickOnTooltipRestore}>restore
                          </button>
                        </div>
                    </div>

               : null
            )
        }
    },
    getStyle: function(){

            var style = {
                top: this.props.data.position.top + 115,
                left: this.props.data.position.left - 122,
            }

        return style;
    },

    userClickOnTooltipRestore: function(){
        Actions.userClickOnTooltipRestore();
    },

    closeTooltip: function(){
        $(document).click(function(e){
            if(e.target.offsetParent == undefined ||
               e.target.offsetParent.className != "tooltip"){
                if(e.target.className.baseVal != "backup"){
                    Actions.hideCopyHistoryRestoreTooltip();
                }
            }
        })
    },

    clickOnFailOver() {
      const failover = true;
      Actions.userClickOnTooltipRestore(failover);
    },

});

export default ToolTip;
