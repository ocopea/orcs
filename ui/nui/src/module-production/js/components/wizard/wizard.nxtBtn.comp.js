// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import ArrowRight from '../../../assets/images/main-footer/arrow-right-btn.png';
import Store from '../../stores/main-store.js';
import Actions from '../../actions/actions.js';

let ButtonNext = React.createClass({
    
    handleClickOnNextWizard: function(){
        Actions.userClickOnNextWizard();
    },
    
    getButtonText: function(){
        if(this.props.currentStep == "Summary"){
            return "Deploy";
        }else{
            return "next";
        }
    },
    
    render: function(){
      //console.log(this.props.selectedSetupType)
       var className;

       if(!this.props.data.isActive){
           className = "button button-primary disabled";
       }else{
           className = "button button-primary enabled";
       }
      
       if(this.props.currentStep == "Business Setup"){
           if(this.props.selectedPlan.name == undefined){
               className = "button button-primary disabled";
           }
       }else if(this.props.currentStep == "Site Setup"){
           if(this.props.selectedSetupType.name == undefined){
               className = "button button-primary disabled";
           }
       }
       
       this.getButtonText();
      
       return (
            
            <div id="next-button" className={className} 
             onClick={this.handleClickOnNextWizard}>
                <img src={ArrowRight} className="arrow-img"/> 
                <span>{this.getButtonText()}</span>
            </div>                                   
       )   
   } 
});

export default ButtonNext;