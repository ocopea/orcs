// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import Tabs from './tabs.comp.js';
import SavedImageTable from './savedImageTable.comp.js';
import Backup from './backup.comp.js';
import DevWizardActions from '../../../actions/dev-wizard-actions.js';
import DevActions from '../../../actions/dev-actions.js';
import $ from 'jquery';
import _ from 'lodash';

var Image = React.createClass({

    componentDidMount: function(){

      DevWizardActions.userSelectedImage(null);
      DevActions.getSavedAppImages();
      DevWizardActions.setImageSelectedTab(this.props.image.tabs.savedImage);
      var that = this;

      //hide more tags tooltip on document click
      $(document).click(function(e){
        if(that.props.image.tooltip.isRender){
          if(!e.target.classList.contains('tags-tt')){
            DevWizardActions.hideTagsTooltip();
          }
        }
      })

    },

    render: function(){

      return(
        <div className="image">
          <div className="title">choose image</div>
          <div className="subtitle">Choose image from the following options</div>
          <Tabs data={this.props.image}/>

          {
            this.props.image.selectedTab == this.props.image.tabs.savedImage ?
              <SavedImageTable
                  image={this.props.image}
                  users={this.props.users}/>
            :
            this.props.image.selectedTab == this.props.image.tabs.backup ?
              <Backup
                image={this.props.image}/>
            :
            null
          }

        </div>
      )
    }
});

export default Image;
