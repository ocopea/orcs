// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';


let AppMarket = React.createClass({
   
    render: function(){
        //console.log(this.props.cards)
        return(
            
               <div id="app-market-container" className={this.props.currentStepName}>
                    <h1 className="general-title">App Market</h1>
                    <h2 className="subtitle">Select an application to deploy</h2>  
                    <div id="cards-container">
                      {this.props.cards}
                    </div>
               </div>                                 
        
        )
    },
});

export default AppMarket;