// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import Plan from './wizard.BusinessSetup.plan.comp.js';
import BusinessContinuitySettings from '../../data/static-info/BusinessContinuitySettings.js';

let BusinessSettings = React.createClass({
    
    createPlans: function(){
        //console.log(this.props.selectedPlan)
        var selectedPlan = this.props.selectedPlan;
        var plans = BusinessContinuitySettings.map(function(plan, index){
            return <li key={index}><Plan key={index} data={plan} selectedPlan={selectedPlan}/></li>
        });
        return plans;
    },
    
    render: function(){
        //console.log(BusinessContinuitySettings)
        return (
            <div id="business-setup-container" className={this.props.currentStepName}>    
                <h1 className="general-title">Business Continuity</h1>
                <h2 className="subtitle">Choose from a set of pre defined business continuity settings</h2>
                
                <div id="plans-container">            
                    <ul>
                        {this.createPlans()}
                    </ul>                    
                </div>
            </div>
        )
    }
});

export default BusinessSettings;