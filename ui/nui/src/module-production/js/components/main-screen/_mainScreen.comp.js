// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import Actions from '../../actions/actions.js';

//components
import MainScreenCard from './mainScreen.card.comp.js';

let MainScreen = React.createClass({

    getCards: function(){

        var that = this;
        var cards = this.props.allAppInstances.map(function(app, index){

            var originApp = {};

            that.props.allApps.filter((originapp)=>{
                if(originapp.name == app.appTemplateName){
                    originApp = originapp;
                }

            });

            return <MainScreenCard
						key={index}
						appInstance={app}
						originApp={originApp}
                        selectedUser={app.creator}/>
        });
        return cards;
    },

    render: function(){

		return(
            <div id="main-screen-container">
                {this.getCards()}
            </div>
        )
    }

});

export default MainScreen;
