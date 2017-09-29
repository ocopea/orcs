// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import SetupSiteCard from './wizard.siteSetup.setupTypeCard.comp.js';

let SiteSetup = React.createClass({
    
    getSetupTypesCards: function(){
        
        var selectedSetupType = this.props.selectedSetupType;
        var siteSetupCards = this.props.setupTypes.map(function(setupType, index){
            //console.log(setupType.name)
            return <SetupSiteCard 
                        key={index} 
                        typeName={setupType.name} 
                        iconPath={setupType.iconPath}
                        points={setupType.points}
                        selectedSetupType={selectedSetupType}/>
        });
        
        return siteSetupCards;
    },
    
    render: function(){
        //console.log(this.props)
        return (
            <div id="site-setup-container" className={this.props.currentStepName}>
                <h1 className="general-title">Site Setup</h1>
                <h2 className="subtitle">site setup</h2> 
                
                <div className="cards-container">
                    {this.getSetupTypesCards()}
                </div>
            </div>
        )
    },
});

export default SiteSetup;