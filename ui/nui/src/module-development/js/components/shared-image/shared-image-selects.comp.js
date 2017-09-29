// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import Select from 'react-select';
import SharedImageSelect from './shared-image-select.comp.js';
import _ from 'lodash';

var SharedImageSelects = React.createClass({

  getInitialState: function(){
    return {
      selectedOption: ""
    }
  },

  getSelects: function(){

    var that = this;

    var selects = _.map(this.props.data, (select, i)=>{
      
      return <SharedImageSelect
                key={i}
                className={select.className + " Naz-select"}
                type={select.type}
                searchable={false}
                clearable={false}
                options={that.getOptions(select.options)}>
             </SharedImageSelect>
    });
    return selects;
  },

  getOptions: function(options){

    return _.map(options, option=>{
      return option.siteId ?
            {value: option.text, label: option.text, siteId: option.siteId} :
            {value: option.text, label: option.text};
    });
  },

  render: function(){
    // console.log(this.props)
    return(
      <div className="Shared-image-selects">
        {this.getSelects()}
      </div>
    )
  }
});

/**
 * Required data structure
 */

// [
//   {
//     className: "",
//     onClick: func,
//     options: [
//       {
//         text: "",
//         onClick: func,
//         className: ""
//       }
//     ]
//   }
// ]

SharedImageSelects.propTypes = {
  data: React.PropTypes.array.isRequired
}

export default SharedImageSelects;
