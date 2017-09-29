// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import Reflux from 'reflux';
import Actions from './actions.js';
import $ from 'jquery';

require('es6-promise').polyfill();
require('isomorphic-fetch');

var Store = Reflux.createStore({

  listenables: [Actions],

  getInitialState: function(){
    return this.state;
  },

  state: {

    ideas: [],

    // version: "",

    mainScreen: {
        isRender: true
    },

    currentLocation: "",

    location: {
        options: {
            main: "main",
            add_idea: "add_idea",
            error: "error",
            ideas: "ideas"
        },
        oldURL: "",
        newURL: ""
    },

  },

	init: function(){
    Actions.setLocationHash(this.state.location.options.main);

    this.state.currentLocation =
    this.state.currentLocation == "" ?
    this.state.location.options.main :
    this.state.currentLocation;

    this.trigger(this.state);

		this.fetchIdeas();
    // this.fetchVersion();
	},

	onGoToMainScreen: function(){

		this.fetchIdeas();
		this.state.mainScreen.isRender = true;
    Actions.setLocationHash(this.state.location.options.main);
		this.trigger(this.state);
	},

	fetchIdeas: function(){
    // console.log(APISERVER + "../../idea")
    fetch(`${APISERVER}/idea`)
        .then((response) => response.json())
        .then((data) => {
    			this.state.ideas = data;
    			this.trigger(this.state);
    }).catch(function(err){
	       console.log(err)
    });
	},

  fetchVersion: function(){
    fetch(`${APISERVER}/version`)
        .then((response) => response.json())
        .then((data) => {
          console.log(data)
          this.state.version = data;
          this.trigger(this.state);
    }).catch(function(err){
        console.log(err)
    });
  },

  onSetLocationHash: function(location){

      this.state.currentLocation =
          location == this.state.location.error ?
          this.state.currentLocation + "/" +this.state.location.options.error : location;

      window.location.hash = this.state.currentLocation;
      this.trigger(this.state);
  },

	onPopulateHash: function(e){

		e.preventDefault;
        Actions.navigate(e);

	},

  onNavigate: function(locationEvent){

      this.state.location.oldURL = locationEvent.oldURL;
      this.state.location.newURL = locationEvent.newURL;

      var locationFromHash = locationEvent.newURL.substring(locationEvent.newURL.indexOf("#")+1);
      this.state.currentLocation = locationFromHash;

      this.trigger(this.state);
  },

  onUserClickOnIdea: function(id, index){
      Actions.setLocationHash(this.state.location.options.ideas + "/" + index)
      this.getIdeaById(id)
  },

  getIdeaById: function(id, approve){
      var idea = this.state.ideas.filter((idea)=>{
          return idea.id === id;
      })[0];
      this.state.ideaDetails = idea;
      return idea;
      this.trigger(this.state);
  },

  onUserClickOnReview: function(id, approve){
    console.log(approve)
    var idea = this.getIdeaById(id);
    var data = {
      ideaId: idea.id,
      approved: approve
    };
    var that = this;

    $.ajax({
        url: `${APISERVER}/review`,
        type: 'POST',
        data: JSON.stringify(data),
        contentType: "application/json",
        success: function (returndata) {
          that.fetchIdeas();
            console.log(returndata)
        },
        error: function(err){
            console.log(err)
        }
    });
  },


})

export default Store;
