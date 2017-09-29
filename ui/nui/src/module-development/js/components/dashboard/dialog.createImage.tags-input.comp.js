// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import TagsInput from 'react-tagsinput';
import 'react-tagsinput/react-tagsinput.css'
import Autosuggest from 'react-autosuggest'
import DevDashboardActions from '../../actions/dev-dashboard-actions.js';
import $ from 'jquery';
import _ from 'lodash';

var InputTags = React.createClass({

  componentDidMount: function(){
    DevDashboardActions.initiateImageTags();
  },

  componentDidUpdate: function(nextProps){
    this.handleSuggestionsStyle();
  },

  handleSuggestionsStyle: function(){
    var inputPosition = $(".react-autosuggest__input").position();
    $(".react-autosuggest__suggestions-list").css("top", inputPosition.top+33);
    $(".react-autosuggest__suggestions-list").css("left", inputPosition.left-6);
  },

  handleChange: function(e, value, index){
    //remove tag
    this.state.tags.splice(index, 1);
    this.forceUpdate();
  },

  onChange: function(e){
    //add tag
    if(e.target.value != ','){
      this.setState({
        newTag: e.target.value
      })
    }
  },

  getSuggestions: function(e){
    const inputValue = (e.value).trim().toLowerCase();
    const inputLength = inputValue.length;
    return inputLength === 0 ? [] : this.props.suggestions.filter(suggestion =>
      suggestion.toLowerCase().slice(0, inputLength) === inputValue
    );
  },

  onSuggestionsFetchRequested: function ( value ) {
    this.setState({
      suggestions: this.getSuggestions(value)
    })
  },

  onSuggestionsClearRequested: function(){
    this.setState({
      suggestions: []
    })
  },

  addTag: function(tag){
    if(tag.trim().length > 0 && tag != ',' &&
       this.state.tags.indexOf(tag) == -1){
      DevDashboardActions.createImageAddTag(tag);
      this.state.tags.push(tag);
      this.setState({newTag: ""});
    }
  },

  getInitialState: function(){
    return this.state = {newTag: "", tags:[], suggestions:[]}
  },

  onBlur: function(e){
    var container = $(e.target).parents()[2];
    $(container).removeClass('react-tagsinput--focused')
    if(this.state.newTag.trim().length > 0){
      this.addTag(this.state.newTag)
    }
  },

  onKeyDown: function(e){
    switch (e.keyCode) {
      //enter key pressed
      case 13:
        if($(".react-autosuggest__suggestions-list").length === 0){
          this.addTag(this.state.newTag)
        }
        break;
      //comma key pressed
      case 188:
        this.addTag(this.state.newTag)
      break;
      //backspace key pressed
      case 8:
        if(this.state.newTag.length === 0){
          DevDashboardActions.createImageRemoveTag(_.last(this.state.tags));
          this.state.tags.splice(this.state.tags.length-1, 1);
          this.forceUpdate();
        }
        break;
      default:

    }(e.keyCode)
  },

  onSuggestionSelected: function(e, {suggestion}){
    this.addTag(suggestion);
    $('.react-autosuggest__input').val('');
  },

  onFocus: function(e){
    var container = $(e.target).parents()[2];
    $(container).addClass('react-tagsinput--focused')
  },

  render: function(){

    $(".react-tagsinput").click(function(){
      $(".react-autosuggest__input").focus();
    })

    var that = this;
    var value = this.state.newTag;

    var getSuggestionValue = function(suggestedValue){
      return suggestedValue;
    }
    var renderSuggestion = function(suggestion, input){
      return(
        <div>{suggestion}</div>
      )
    }

    var inputProps = {
      placeholder: 'Type a new tag',
      value,
      onChange: this.onChange,
      onBlur: this.onBlur,
      onFocus: this.onFocus,
      onKeyDown: this.onKeyDown
    };

    var autocompleteRenderInput = function(props){
      //console.log(that.state.suggestions)
      return(
        <Autosuggest
          shouldRenderSuggestions={(value) => value && value.trim().length > 0}
          suggestions={that.state.suggestions}
          onSuggestionsFetchRequested={that.onSuggestionsFetchRequested}
          onSuggestionsClearRequested={that.onSuggestionsClearRequested}
          onSuggestionSelected={that.onSuggestionSelected}
          getSuggestionValue={getSuggestionValue}
          renderSuggestion={renderSuggestion}
          inputProps={inputProps}
        />
      )
    }

    return(
      <TagsInput
        value={this.state.tags}
        renderInput={autocompleteRenderInput}
        onChange={this.handleChange}/>
    )
  }
});

export default InputTags;
