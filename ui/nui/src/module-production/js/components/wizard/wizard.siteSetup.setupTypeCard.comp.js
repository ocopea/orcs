// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import MapSingleSite from '../../../assets/images/siteSetup/map_SINGLE-SITE.png';
import MapMetro from '../../../assets/images/siteSetup/map_METRO.png';
import MapMultiRegion from '../../../assets/images/siteSetup/map_MULTI-REGION.png';

import Point from './wizard.siteSetup.setupTypeCard.point.comp';

import Actions from '../../actions/actions.js';

let SetupSiteCard = React.createClass({
    
    getSetupTypeMap: function(){
        
        var iconPath;
        switch(this.props.typeName){
            case "single site":
                iconPath = MapSingleSite;
                break;
                
            case "multi region":
                iconPath = MapMultiRegion;
                break;
                
            case "metro":
                iconPath = MapMetro;
                break;                            
        }
        return <img src={iconPath} />
    },
    
    getClass: function(){
        
        var className;
        var siteSetupCard = "card site-setup-card";
        //console.log(this.props.selectedSetupType)
        switch(this.props.typeName){
            case "single site":
                className = "single-site";
                break;
                
            case "multi region":
                className = "multi-region";
                break;
                
            case "metro":
                className = "metro";
                break;                            
        }
        
        if(this.props.selectedSetupType != undefined){
            if(this.props.selectedSetupType.name == this.props.typeName){
                return siteSetupCard + " " + className + " selected";
            }else{
                return siteSetupCard + " " + className;    
            }                
        }
    },    
        
    userClickOnSiteSetup: function(typeName){
        Actions.userClickOnSiteSetup.bind(this, typeName)();
    },
    
    getPoints: function(){
        //console.log(this.props.points);
        var points = [];
        
        for(var i=0; i < this.props.points; i++){
            
            points.push(<Point key={i} />)
            
        }
        
        return points;
    },
    
    render: function(){
        //console.log(this.props.selectedSetupType)
        return (
            <div className={this.getClass()} onClick={this.userClickOnSiteSetup.bind(this, this.props.typeName)}>
                <div className="inside">
                    <div className="title">{this.props.typeName}</div> 
                    <div id="setupTypeMap">
                        {this.getSetupTypeMap()}
                        {this.getPoints()}
                    </div>
                </div>
                
            </div>
        )    
    },
    
});

export default SetupSiteCard;