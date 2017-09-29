// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import Actions from '../../actions/actions.js';
import ReactDOM from 'react-dom';

let Summary = React.createClass({
   
    getProps: function(){
        
        var selectedApp, selectedInfrastructure, selectedPlan, selectedSetupType;
        
        if(this.props.selectedApp == undefined){
            selectedApp = "";
        }else{
            selectedApp = this.props.selectedApp;
        }
        
        if(this.props.selectedInfrastructureService == undefined){
            selectedInfrastructure = "";
        }else{
            selectedInfrastructure = this.props.selectedInfrastructureService;
        }
        
        if(this.props.selectedPlan == undefined){
            selectedPlan = "";
        }else{
            selectedPlan = this.props.selectedPlan;
        }
        
        if(this.props.selectedSetupType == undefined){
            selectedSetupType = "";
        }else{
            selectedSetupType = this.props.selectedSetupType;
        }
        
        var data = {
            app: selectedApp,
            plan: selectedPlan,
            infrastructure: selectedInfrastructure,
            selectedSetupType: selectedSetupType,
        }
        return data;
    },
    
    componentDidMount: function(){
       
    },
    
    shouldComponentUpdate: function(nextProps){
        return this.props.currentStepName != nextProps.currentStepName ||
               this.props.isLoading != nextProps.isLoading
    },
    
    render: function(){
        const that = this;
        
        return (
            <div id="summary-container" className={this.props.currentStepName}>
                <h1 className="general-title">Summary</h1>
                <h2 className="subtitle">Deployment Summary</h2>
                <ul>
                    <li>
                        <span>
                            <span>Selected Application: </span> 
                            <span className="bold">
                                {this.getProps().app.name}
                            </span>
                        </span>
    
                        <div className="bullet">
                            <div className="inside"></div>
                        </div>
                    </li>
                    <li>
                        <span>Edited Infrastructure Service: </span>
                        <div className="bullet">
                            <div className="inside"></div>
                        </div>
                        <ul>
                            <li>
                                <span>name: </span>
                                <span className="bold">
                                    {this.getProps().infrastructure.name}
                                </span>
                            </li>
                            <li>
                                <span>type: </span> 
                                <span className="bold">
                                    {this.getProps().infrastructure.type}
                                </span>
                            </li>
                        </ul>
                    </li>
                    <li>
                        <span>Selected Plan: <span className="bold">{this.getProps().plan.name}</span></span>
                        <div className="bullet">
                            <div className="inside"></div>
                        </div>
                    </li>
                    <li>
                        <span>Selected siteSetup: <span className="bold">{this.getProps().selectedSetupType.name}</span></span>
                        <div className="bullet">
                            <div className="inside"></div>
                        </div>
                    </li>
                </ul>
            </div>
        )
    }
});

export default Summary;
