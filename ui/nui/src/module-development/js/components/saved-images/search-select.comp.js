// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import Select from 'react-select';
import DevActions from '../../actions/dev-actions.js';


var SearchSelect = React.createClass({

  getInitialState: function(){
    return {
      value: "",
      options: this.props.options
    }
  },

  getSelectOptions: function(){
    var options = this.props.options.map(option=>{
      return(
        { value: option.name, label: option.name, id: option.id }
      )
    });

    return options;
  },

  onBlur: function(e){
    if(this.state.value === null){
      DevActions.initiateOriginalImages();
    }
  },

  onChange: function(val){
    this.setState({value:val});
    if(val !== null){
      DevActions.filterImages(val.id);
    }else{
      DevActions.initiateOriginalImages()
    }
  },

  render: function(){
    return(
      <Select
        className="Saved-images__search__select"
        placeholder="search image..."
        options={this.getSelectOptions()}
        optionClassName="Saved-images__search__select__option"
        onChange={this.onChange}
        value={this.state.value}
        filterOptions={this.filterOptions}        
        clearable={false}
        onBlur={this.onBlur}/>
    )
  }
});

export default SearchSelect;
