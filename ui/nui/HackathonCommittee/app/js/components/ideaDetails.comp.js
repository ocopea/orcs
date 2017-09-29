// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import Actions from '../actions.js';


var IdeaDetails = React.createClass({

    userClickOnImage: function(){
        Actions.enlargeIdeaDetailImage();

    },

    userClickOnMinimizaeImage: function(){
        Actions.minimizeIdeaDetailsImage();
    },

    componentDidMount: function(){
        Actions.minimizeIdeaDetailsImage();
    },

    render: function(){

       var imageUrl = APISERVER + "/html/ideaDoc/"+this.props.selectedIdea.id;

        return (

            <div className="idea-details">

                <div className="title">
                    { this.props.selectedIdea.name }
                </div>

                <div className="inside">

                    <div className={!this.props.selectedIdea.isImageEnlarged ? "image-container" : "image-container enlarged"}
                        style={!this.props.selectedIdea.isImageEnlarged ? {width: "45px"} : null}>

                        <img src={imageUrl}
                            onClick={this.userClickOnImage}
                            className="idea-image"
                            style={!this.props.selectedIdea.isImageEnlarged ? {width: "100%"} : null}
                            title="Click To Maximize Image"/>

                        {

                            this.props.selectedIdea.isImageEnlarged ?

                                <button
                                    className="minimize-image"
                                    type="button"
                                    onClick={this.userClickOnMinimizaeImage}
                                    title="Minimize Image">

                                  <span></span>
                                  <span></span>

                                </button>

                            :

                            null

                        }


                    </div>

                    <p>{ this.props.selectedIdea.description }</p>

                </div>

            </div>

        )

    }

});

export default IdeaDetails;
