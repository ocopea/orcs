// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import Config from '../../../../../module-production/js/config.js';

var TagsTooltip = React.createClass({

  getTags: function(){
      var tags = this.props.data.tags.map((tag, i)=>{
        if(i > 1){
          return  <div
                    className="tag tags-tt"
                    key={i}
                    title={tag.length > 4 ? tag : null}>
                      <span className="label tags-tt">{Config.getShortName(tag, 4)}</span>
                  </div>
        }
      });
      return tags;
  },

  render: function(){

    var style={
      top: this.props.data.position.top + 45,
      left: this.props.data.position.left + 45
    }

    return(
      <div className="tags-tooltip tags-tt" style={style}>
        <div className="inside tags-tt">
          {this.getTags()}
        </div>
      </div>
    )
  }
});

export default TagsTooltip;
