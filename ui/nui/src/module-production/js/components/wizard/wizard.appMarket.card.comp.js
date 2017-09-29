// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import Actions from '../../actions/actions.js';
import Config from '../../config.js';

let Card = React.createClass({

    handleCardSelection: function(id){
        Actions.userClickOnCard(id);
    },

    render: function(){
		//console.log(this.props.data.img)
        var cardClass;
        if(this.props.isSelected === true){
            cardClass = 'card selected';
        }else{
            cardClass = 'card';
        }
        var appName = this.props.data.name;

        return (
            <div className={cardClass}
                onClick={this.handleCardSelection.bind(this, this.props.data.id)}
                id={this.props.data.id}>
                <div className="inside">
                    <div className="info">
                        <div className="inside-section">
                            <h2>{this.props.data.name}</h2>
                            <h3>{this.props.data.description}</h3>
                        </div>
                    </div>

                     <img src={this.props.data.img}/>

                </div>
            </div>
        )
    }
});

export default Card;
