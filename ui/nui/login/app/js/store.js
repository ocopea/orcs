// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import Reflux from 'reflux';
import Actions from './actions.js'
import $ from 'jquery';


var Store = Reflux.createStore({

    listenables: [Actions],

	state:{

		user: {
			username: "",
			password: ""
		}

	},

	onSetLoginUsername: function(username){
		this.state.user.username = username;
		this.trigger(this.state);
	},

	onSetLoginPassword: function(password){
		this.state.user.password = password;
		this.trigger(this.state);
	},

	onLogin: function(){

		var that = this;

		var data = {
			username: that.state.user.username,
			password: that.state.user.password
		}

		$.ajax({
      beforeSend: function (xhr) {
        xhr.setRequestHeader (
            "Authorization", "Basic " + btoa(
                data.username + ":" + data.password)
        );
      },
			url: APISERVER + "/hub-web-api/commands/sign-in2",
			method: 'POST',
			contentType: "application/json",
			data: JSON.stringify(data),
			success: function(response){
				window.location = `${APISERVER}/hub-web-api/html/nui/index.html${window.location.hash}`;
			},
			error: function(error){
				console.log(error);
			}
		});

	}

});

export default Store;
