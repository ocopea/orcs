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

		appName: "hack-prod",

        ideas: [],

        version: "",

        mainScreen: {
            isRender: true
        },

        addNewIdea: {
            isRender: false,
            ideaCounter: 1,
            ideaName: "",
            ideaDesc: "",
            imageName: "",
			imageData: "",
			ideaDoc: {}
        },

        errorDialog: {
            isRender: false,
            content: ""
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

        ideaDetails: {
            idea: {},
            image: "",
            isImageEnlarged: false
        }

    },

	init: function(){
    Actions.setLocationHash(this.state.location.options.main);

    this.state.currentLocation =
      this.state.currentLocation == "" ?
      this.state.location.options.main :
      this.state.currentLocation;

    this.trigger(this.state);

		this.fetchIdeas();
    this.fetchVersion();
	},

    onUserClickOnAddNewIdea: function(){

        this.state.mainScreen.isRender = false;
        this.state.addNewIdea.isRender = true;
        Actions.setLocationHash(this.state.location.options.add_idea);
        this.trigger(this.state);

    },

	onGoToMainScreen: function(){

		this.fetchIdeas();
		this.state.mainScreen.isRender = true;
		this.state.addNewIdea.isRender = false;
        Actions.setLocationHash(this.state.location.options.main);
		this.trigger(this.state);
	},

	onUploadImage: function(file, fileData){
		this.state.addNewIdea.imageName = file.name;
		this.state.addNewIdea.imageData = fileData;
		this.trigger(this.state);
	},

	onResetImageName: function(){
		this.state.addNewIdea.imageName = "";
		this.trigger(this.state);
	},

	onSetIdeaName:function(name){
		this.state.addNewIdea.ideaName = name;
		this.trigger(this.state);
	},

	onSetIdeaDescription: function(description){
		this.state.addNewIdea.ideaDesc = description;
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

    onShowErrorDialog: function(content){
        this.state.errorDialog.isRender = true;
        this.state.errorDialog.content = content;
        Actions.setLocationHash(this.getCleanOldURL(this.state.location.newURL) +
                                "/" + this.state.location.options.error);
        this.trigger(this.state);
    },

    onHideErrorDialog: function(){
        this.state.errorDialog.isRender = false;
        Actions.setLocationHash(this.getCleanOldURL(this.state.location.oldURL))
        this.trigger(this.state);
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

    getCleanOldURL: function(oldURL){
        return oldURL.substring(oldURL.indexOf("#")+1);
    },

    onUserClickOnIdea: function(id, index){
        Actions.setLocationHash(this.state.location.options.ideas + "/" + index)
        this.getIdeaById(id)
    },

    getIdeaById: function(id){
        var idea = this.state.ideas.filter((idea)=>{
            return idea.id == id;
        });
        this.state.ideaDetails = idea[0];
        this.trigger(this.state);
    },

    onUserClickOnVote: function(id){
        $.ajax({
            url: `${APISERVER}/vote`,
            type: 'POST',
            data: JSON.stringify({ideaId: id}),
            contentType: "application/json",
            success: function (returndata) {
                console.log(returndata)
                Actions.goToMainScreen();

            },
            error: function(err){
                console.log(err)
            }

        });
    },

    onEnlargeIdeaDetailImage: function(){
        this.state.ideaDetails.isImageEnlarged = true;
        this.trigger(this.state);
    },

    onMinimizeIdeaDetailsImage: function(){
        this.state.ideaDetails.isImageEnlarged = false;
        this.trigger(this.state);
    }

})

export default Store;
