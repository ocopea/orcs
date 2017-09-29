// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import MainScreen from './mainScreen.comp.js';
import AddNewIdea from './addNewIdea.comp.js';
import IdeaDetails from './ideaDetails.comp.js';

var Card = React.createClass({

  render: function(){
    // console.log(this.props)
    return (

      <div className="card">
        {
          this.props.currentLocation == this.props.locationOptions.main ?
            <MainScreen
                ideas={this.props.ideas}/>
          :
          this.props.currentLocation == this.props.locationOptions.add_idea ?
            <AddNewIdea
            		data={this.props.addNewIdea}/>
          :
          this.props.currentLocation
              .substring(0, this.props.currentLocation.indexOf("/")) ==
              this.props.locationOptions.ideas ?

              <IdeaDetails
                  selectedIdea={this.props.selectedIdea}/>
          :
          null
        }
        <div className="version">{this.props.version}</div>
      </div>
    )
  }
})

export default Card;
