// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import DevDashboardActions from '../../actions/dev-dashboard-actions.js';
import TagsInput from './dialog.createImage.tags-input.comp.js';
var GeminiScrollbar = require('react-gemini-scrollbar');


var DialogCreateImage = React.createClass({

  userClickOnCreateImage: function(){
    DevDashboardActions.createImage();
  },

  userChangedName: function(e){
    DevDashboardActions.userChangedCreateImageName(e.target.value)
  },

  userSelectedTag: function(){},

  userChangedComment: function(e){
    DevDashboardActions.userChangedCreateImageComment(e.target.value)
  },

  render: function(){
    //console.log(this.props.tagsSuggestions)
    return(

        <div className="dialog create-image">
          <GeminiScrollbar>
            <div className="title">
              <span>create image</span>
              <div className="close-btn"
                onClick={DevDashboardActions.hideCreateImageDialog}>
                <span></span>
                <span></span>
              </div>
            </div>

            <div className="inside">

              <section>
                <label>name</label>
                <input
                    type="text"
                    placeholder="Type image name"
                    onChange={this.userChangedName}
                    tabIndex="0"/>
              </section>

              <section>
                <TagsInput
                  tags={this.props.createImageData.tags}
                  suggestions={this.props.tagsSuggestions}
                  tabIndex="0"/>
              </section>

              <section>
                <label>comment</label>
                <textarea onChange={this.userChangedComment}
                tabIndex="0"></textarea>
              </section>

              <div className="footer">
                <button
                    type="button"
                    className="save"
                    onClick={this.userClickOnCreateImage}
                    tabIndex="0">save</button>
                <button
                  type="button"
                  className="cancel"
                  onClick={DevDashboardActions.hideCreateImageDialog}
                  tabIndex="0">cancel</button>
              </div>

            {/* /inside */}
            </div>
          </GeminiScrollbar>
        {/* /dialog create-image */}
      </div>
    )
  }
});

export default DialogCreateImage;
