// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';

export default class RangeFilter extends React.Component{

    rangeSelection(range){
      if(range !== this.props.period){
        this.props.onRangeChange(range);
      }
    }

    setClass(range){
        if(range == this.props.period){
            return "filter selected";
        }else{
            return "filter";
        }
    }

    render(){
        //console.log(this.props.period)
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
};
