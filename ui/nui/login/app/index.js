// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import ReactDOM from 'react-dom';
import LogIn from './js/components/login.comp.js';
import styles from './css/style.scss';
import WallPaper from './assets/images/wallpaper.jpg';
import Reflux from 'reflux';
import Store from './js/store.js';


let App = React.createClass({

	mixins: [
		Reflux.listenTo(Store, "onChangeCallback", "initialCallBack")
	],

    onChangeCallback: function(state){
      this.setState(state);
    },

    initialCallback: function(){
      this.setState(state);
    },

    render: function() {
        return (
            <div>
            	<LogIn />
            </div>
        );
    }
});

ReactDOM.render(<App/>, document.getElementById('app'));

// 				<img src={WallPaper} className="wallpaper"/>
