// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import ReactDOM from 'react-dom';
import styles from './css/style.scss';
import Store from './js/main-store.js';
import Reflux from 'reflux';

import ErrorDialog from './js/components/errorDialog.comp.js';
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

    getStyle: function(){
        var store = Store;
        return {
            opacity: store.state.currentLocation
                        .substring(Store.state.currentLocation.lastIndexOf("/")+1) ==
                        store.state.location.options.error ?
                     0.2 : null
        }

    },

	componentDidMount: function(){
		window.addEventListener("hashchange", Actions.populateHash);
	},

    render: function() {

        return (

            <div className={Store.state.errorDialog.isRender ? "dimmed" : null}>

                <div className="wrapper" style={this.getStyle()}>

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

                {

                   Store.state.currentLocation
                            .substring(Store.state.currentLocation.lastIndexOf("/")+1) ==
                            Store.state.location.options.error ?

                        <ErrorDialog
                            content={Store.state.errorDialog.content}
                        />

                    :

                    null

                }

            </div>
        );
    }
});

ReactDOM.render(<App/>, document.getElementById('app'));
