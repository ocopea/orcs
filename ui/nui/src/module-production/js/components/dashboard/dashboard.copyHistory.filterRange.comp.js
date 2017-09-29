// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import Actions from '../../actions/actions.js';

let RangeFilter = React.createClass({
   
    rangeSelection: function(range){
        if(range !== this.props.range){
			Actions.removeCopyHistoryErrorMsg();
			Actions.userClickOnCopyHistoryRangeFilter.bind(this, range)();
			Actions.hideCopyHistoryRestoreTooltip();
		}
    },
    
    setClass: function(range){
        if(range == this.props.range){
            return "filter selected";
        }else{
            return "filter";
        }
    },
    
    render: function(){
        //console.log(this.props.range)
        return (
            <div id="filter-range">
                <span 
                    onClick={this.rangeSelection.bind(this, "day")}
                    className={this.setClass.bind(this, "day")()}>
                    <span>Day</span>
                </span>
    
                <span 
                    onClick={this.rangeSelection.bind(this, "week")}
                    className={this.setClass.bind(this, "week")()}>
                    <span>Week</span>
                </span>
    
                <span 
                    onClick={this.rangeSelection.bind(this, "month")}
                    className={this.setClass.bind(this, "month")()}>
                    <span>Month</span>
                </span>
                                    
                <span 
                    onClick={this.rangeSelection.bind(this, "year")}
                    className={this.setClass.bind(this, "year")()}>
                    <span>Year</span>
                </span>

            </div>
        )
    }
});

export default RangeFilter;