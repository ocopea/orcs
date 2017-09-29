// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import Actions from '../actions/actions.js';
import SharedActions from '../../../shared-actions.js';
import AppTopologyActions from '../actions/appTopologyActions.js';
import ArrowRight from '../../assets/images/left-menu/arrow-right.png';
import Check from '../../assets/images/left-menu/check.png';
import AddApp from '../../assets/images/left-menu/add-app.png';

import ImgLetsChat from '../../assets/images/cards/lets-chat.png';
import ImgWordPress from '../../assets/images/cards/wordpress.png';
import ImgBuckets from '../../assets/images/cards/buckets.png';
import ImgHackathon from '../../assets/images/cards/hackathon.png';
import ImgLeaderBoard from '../../assets/images/cards/leaderboard.png';

import NavigationOptions from '../data/prodNavigationOptions.js';

import Config from '../config.js';

import $ from 'jquery';


let LeftMenu = React.createClass({

    menuClassName: function(){
        var className;
        var isMenuShown = this.props.leftMenu.isRender;
        if(isMenuShown){
            className="show";
        }else{
            className="hide";
        }
        return className;
    },

    wizardSteps: function() {

        if(this.props.navigation == 'wizard' && this.props.main.currentStep != undefined){

            var currentStep = this.props.main.currentStep.name;
            var wizardSteps = [];
            var counter = 0;

			for(var step in this.props.steps){

				var className;
                var img;
                var thisStep = this.props.steps[step];

                if(currentStep == thisStep.name){
                    className = 'currentStep';
                    img = <img src={ArrowRight}/>
                }
                else if(thisStep.isComplete){
                    img = <img src={Check} className="check-img"/>
                    className = "completed"
                }else{
                    img = <div id="circle"></div>
                    className="step"
                }

                wizardSteps.push(
                    <li key={counter} className={className}>
                        <table>
                            <tbody>
                                <tr>
                                    <td>{img}</td>
                                    <td>{thisStep.name}</td>
                                </tr>
                            </tbody>
                        </table>
                    </li>
                )

                counter++;

            }
            return wizardSteps;
        };
    },

    dashboardMenuClass: function(){

        if(this.props.navigation == 'wizard')
            return "hide";
        else
            return "show";

    },

    clickOnAddApp: function(){
        AppTopologyActions.initialAppTopology();
        Actions.goToWizard();
        Actions.initializeWizardSettings();

		SharedActions.navigate({
			module: NavigationOptions.module,
			location: NavigationOptions.wizard.location,
			subLocation: NavigationOptions.wizard.appMarket.subLocation
		})
    },

	componentDidMount: function(){
		Actions.getAllAppInstances();
	},

	userClickOnApp: function(app){

		if(this.props.selectedAppInstance.name != app.name){

			Actions.userClickOnLeftMenuAppInstance(app)

			SharedActions.navigate({
				module: NavigationOptions.module,
				location: NavigationOptions.dashboard.location,
				subLocation: app.id
			})

		}

	},

  populateLeftMenu: function(){

    var selectedApp = this.props.selectedApp;

		var that = this;

    var allAppInstances = this.props.allAppInstances.map(function(app, index){
			return (
					<li key={index}>
						<ul className={app.id == Config.getCurrentHash().subLocation ?
																		"selected" : null}
							onClick={that.userClickOnApp.bind(that,app)}>
							<li><img src={that.props.applications[app.appTemplateId].img}/></li>
							<li title={app.name}>
								 <span>
									{Config.getShortName(app.name)}
								 </span>
								<span className="description">
									{app.appTemplateName}
								</span>
							</li>
						</ul>
					</li>
				)
      });


    return allAppInstances;
  },

  render: function(){
    //console.log(this.props)
     return(

       <div id="menu-left" className={this.menuClassName()}>
        {
          this.props.navigation == "dashboard" ?

          <div id="dashboard-left-menu" className={this.dashboardMenuClass()}>

              <div className="application-inside" onClick={this.clickOnAddApp}>
                  <div id="add-app" className="link">
                      <span className="plus"><img src={AddApp}/></span>
                      <span>add application</span>
                  </div>
              </div>

              <ul>
                  {this.populateLeftMenu()}
              </ul>

          </div>

          :

          this.props.navigation == "wizard" ?

          <div id="wizard">
              <ul>
                  {this.wizardSteps()}
              </ul>
          </div>

          :

          this.props.navigation == "main" ?

          <div id="main-screen-left-menu">

              <div className="application-inside" onClick={this.clickOnAddApp}>
                  <div id="add-app" className="link">
                      <span className="plus"><img src={AddApp}/></span>
                      <span>add application</span>
                  </div>
              </div>

          </div>
          :
          null
         }

       </div>
     )
   }
});

export default LeftMenu;
