// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import ImgTier2 from '../../../assets/images/businessSetup/plan-img-tier2.png';
import ImgBusinessCritical from '../../../assets/images/businessSetup/plan-img-business-critical.png';
import ImgCustom from '../../../assets/images/businessSetup/plan-img-custom.png';
import Actions from '../../actions/actions.js';
import Indicator from './wizard.BusinessSetup.plan.indicator.js';


let Plan = React.createClass({
    
    setIcon: function(){
        
        var icon;
        
        switch(this.props.data.iconPath){
            case "plan-img-tier2.png":
                icon = ImgTier2;
                break;
            case "plan-img-business-critical.png":
                icon = ImgBusinessCritical;
                break;
            case "plan-img-custom.png":
                icon = ImgCustom;
                break;
        }
        
        return icon;
    },
    
    clickOnPlan: function(){
        Actions.userClickOnBusinessSettingsPlan.bind(this, this.props.data)();
    },
    
    setClassName: function(){
        
            if(this.props.data.name == this.props.selectedPlan.name)
                return "business-continuity-plan selected";
            else{
                return "business-continuity-plan";
            }                   
    },
    
    setIconClass: function(){
        var iconClass;
        switch(this.props.data.iconPath){
            case "plan-img-tier2.png":
                iconClass = "icon plan-img-tier2";
                break;
            case "plan-img-business-critical.png":
                iconClass = "icon plan-img-business";
                break;
            case "plan-img-custom.png":
                iconClass = "icon plan-img-custom";
                break;
        }
        
        return iconClass;
    },
    
    render: function(){
        //console.log(this.setClassName())
        return(
            <div className={this.setClassName()} onClick={this.clickOnPlan}>                
                <div className="inside">
                    <div className="details">details > </div>
                    <div className="icon" className={this.setIconClass()}><img src={this.setIcon()}/></div>
                    <div className="title">{this.props.data.name}</div>  
                    <div className="description subtitle">{this.props.data.description}</div>
                    <Indicator data={this.props.data}/>
                </div>                
            </div>
        )
    }
});

export default Plan;