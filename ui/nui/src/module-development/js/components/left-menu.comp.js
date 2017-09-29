// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import SharedActions from '../../../shared-actions.js';
import LeftMenuFilter from './left-menu-filter.comp.js';
import SettingsLeftMenu from './left-menu-settings.comp.js';
import SiteConfigLeftMenu from './left-menu-site-config.comp.js';
import DevNavigationOptions from '../data/devNavigationOptions.js';
var GeminiScrollbar = require('react-gemini-scrollbar');


var DevLeftMenu = React.createClass({

    populateAppTypes: function(){
        var rows = this.props.filters.appType.instances.map((instance, i)=>{
            return  <tr>
                        <td width="20">
                            <div className="filter-icon"></div>
                        </td>
                        <td>{instance.name}</td>
                    </tr>
        });
        return rows;
    },

	userClickOnCreateNew: function(){
		SharedActions.navigate({
			module: DevNavigationOptions.module,
			location: DevNavigationOptions.wizard.location,
			subLocation: DevNavigationOptions.wizard.subLocation.appMarket
		});
	},

	userClickOnNavigateToMain: function(){
		SharedActions.navigate({
			module: DevNavigationOptions.module,
			location: DevNavigationOptions.main.location,
			subLocation: ""
		});
	},

	render: function(){

		return(

			<div className="left-menu">
        	{
					this.props.currentLocation.location == DevNavigationOptions.main.location ?
					<div>
            <GeminiScrollbar>

  						<div className="top-container">

  							<button className="btn-create-new" onClick={this.userClickOnCreateNew}>
  								<span>create new</span>
  							</button>

  						</div>

  						<div className="title filter-by">
  							filter by
  						</div>

  						<LeftMenuFilter
  							type="appType"
  							title="app type"
  							instances={this.props.filters.appType.instances}
  							isRender={this.props.filters.appType.isRender}
  							allFilters={this.props.allFilters}/>

  						<LeftMenuFilter
  							type="users"
  							instances={this.props.filters.users.instances}
  							isRender={this.props.filters.users.isRender}
  							users={this.props.users}
  							allFilters={this.props.allFilters}/>

  						<LeftMenuFilter
  							type="services"
  							instances={this.props.filters.services.instances}
  							isRender={this.props.filters.services.isRender}
  							allFilters={this.props.allFilters}/>

            </GeminiScrollbar>
					</div>
					:
					this.props.currentLocation.location == DevNavigationOptions.wizard.location ||
          this.props.currentLocation.location == DevNavigationOptions.dashboard.location ?

						<div className="top-container">

							<button
								className="btn-create-new"
								onClick={this.userClickOnNavigateToMain}>
									<span>go to main</span>
							</button>

						</div>

					:
          this.props.currentLocation.location === DevNavigationOptions.settings.location ?
            <SettingsLeftMenu />
          :

          this.props.currentLocation.location === DevNavigationOptions.siteConfig.location ?
            <SiteConfigLeftMenu
              sites={this.props.sites}
              selectedSite={this.props.selectedSite}/>
          :
					null
				}

			</div>

		)

	}

});

export default DevLeftMenu;
