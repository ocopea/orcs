// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import Actions from '../actions.js';
import Logo from '../../assets/images/EMC_DELL_LOGO.svg';
import LogoShield from '../../assets/images/logo.svg';
import Isvg from 'react-inlinesvg';

var LogIn = React.createClass({

	setUsername: function(e){
		Actions.setLoginUsername.bind(this, e.target.value)();
	},

	setPassword: function(e){
		Actions.setLoginPassword.bind(this, e.target.value)();
	},

	userClickOnLogin: function(){
		Actions.login();
	},

	componentDidMount: function(){
		var container = document.querySelector('.login-container');
		container.addEventListener('keydown', function(event) {
    	if(event.keyCode === 13){
				Actions.login();
			}
    }, false);
	},

	render: function(){

		return(

			<div className="login-container">

				<div className="title">
					<div className="logo-circle">
						<Isvg className="logo-shield" src={LogoShield} />
					</div>
					<div className="content">
						cloud native data protection
					</div>
					<div className="emc-logo">
						<Isvg className="emc-logo-img" src={Logo} />
					</div>
				</div>

				<div className="inside">
					<div className="intro">
						please sign in with the credentials provided to you when you purchased the product
					</div>
					<div className="inputs">

						<input
							autoComplete='off'
							type="text"
							name="username"
							onChange={this.setUsername}
							placeholder="Admin username"/>

						<input
							type="password"
							name="password"
							onChange={this.setPassword}
							placeholder="Admin password"/>

					</div>

					<span className="forgot-password">in case you forgot your password, contact
						<a href="http://www.emc.com" target="blank">emc support</a></span>

					<button
						className="button button-primary"
						onClick={this.userClickOnLogin}>login</button>

				</div>

			</div>

		)

	}

});

export default LogIn;

// <div className="forgot-password">
// 	forgot password
// </div>
