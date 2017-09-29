// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import webpackIcon from './../../assets/images/webpack.png';
import Actions from '../actions.js';
import Config from '../../../../src/module-production/js/config.js';
import $ from 'jquery';

let MainCard = React.createClass({

  userClickOnIdea: function(id, index){
      //Actions.userClickOnIdea(id, index);
  },

  componentDidUpdate: function(nextProps){
    if(this.props.ideas !== nextProps.ideas){
      this.setState({
        ideas: this.props.ideas
      })
    }
  },

  userClickOnVote: function(id, approve){
      Actions.userClickOnReview(id, approve);
  },

	getIdeas: function(){

    var that = this;

		var ideasTrs = this.props.ideas.map((idea, index)=>{
      if(idea.status === 'submitted')
      var imageUrl = `${APISERVER}/hackathon-api/html/ideaDoc/${idea.id}`;
      var goToIdeaDetails = that.userClickOnIdea.bind(that, idea.id, index+1);
			return  <tr key={index}>
    						<td
                  title={Config.getTitleOrNull(idea.description, 20)}
                  onClick={goToIdeaDetails}>
                    {Config.getShortName(idea.description, 20)}
                </td>
    						<td onClick={goToIdeaDetails}>{idea.status}</td>
                <td className="buttons-container">
                  <button
                    className="primary-button vote"
                    onClick={this.userClickOnVote.bind(that, idea.id, true)}>approve</button>
                  <button
                    className="primary-button vote"
                    onClick={this.userClickOnVote.bind(that, idea.id, false)}>reject</button>
                </td>
    					</tr>
		});

		return ideasTrs;

	},

	getTableStyle: function(){
		return {
			height: this.props.ideas.length == 0 ? 256 : null
		}
	},

	render: function() {

		return (
			<div className="main-screen">

				<table style={this.getTableStyle()}>

					<thead>
						<tr>
							<th width="30%">description</th>
							<th width="20%">status</th>
              <th width="20%"></th>
						</tr>

					</thead>

					<tbody>

						{

							this.props.ideas.length == 0 ?
								<tr className="no-apps-announce">

									<td colSpan="6">

										<div>
											<span className="row">Welcome commitee</span>
										</div>

									</td>

								</tr>
							:

							this.getIdeas()

						}

					</tbody>

				</table>

      </div>
		)
	}
});

export default MainCard;
