// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import Actions from '../../actions/actions.js';
import ArrowLeft from '../../../assets/images/main-footer/arrow-left.png';

let PrevBtn = React.createClass({
    handleWizardBackPressed: function(){
        Actions.userClickOnBackWizard();
    },
    render: function() { 
        
        var className;
        
        if(this.props.currentStep == "App Market" || !this.props.data.isActive){
          className = "button button-secondary disabled";
        }else{
          className = "button button-secondary";
        }
        
        return (
            
            <div id="prev-button" className={className} onClick={this.handleWizardBackPressed}>
                <img src={ArrowLeft} className="arrow-img"/>prev
            </div>
        )
    } 
});

export default PrevBtn;

//