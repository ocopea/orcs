// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import ReactDOM from 'react-dom';
import styles from './css/style.scss';
import Store from './js/main-store.js';
import Reflux from 'reflux';

import Card from './js/components/card.comp.js';

import Actions from './js/actions.js';

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

	componentDidMount: function(){
		window.addEventListener("hashchange", Actions.populateHash);
	},

    render: function() {
      
      return (

        <div>

          <div className="wrapper">

              <Card
                  mainScreen={Store.state.mainScreen}
                  ideas={Store.state.ideas}
                  addNewIdea={Store.state.addNewIdea}
                  currentLocation={Store.state.currentLocation}
                  locationOptions={Store.state.location.options}
                  selectedIdea={Store.state.ideaDetails}
                  version={Store.state.version}
              />

          </div>

        </div>
      );
    }
});

ReactDOM.render(<App/>, document.getElementById('app'));
