// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import webpackIcon from './../../assets/images/webpack.png';
import Actions from '../actions.js';
import Config from '../../../../src/module-production/js/config.js';
import $ from 'jquery';

let MainCard = React.createClass({

    addNewIdea: function(){

        Actions.userClickOnAddNewIdea();

    },

    userClickOnIdea: function(id, index){
        Actions.userClickOnIdea(id, index);
    },

    userClickOnVote: function(id){
        Actions.userClickOnVote(id);
    },

  clickOnVote: function(id, status){
    var statusDict = {
      approved: 'approved',
      submitted: 'submitted',
      rejected: 'rejected'
    }
    switch (status) {
      case statusDict.approved:
        this.userClickOnVote(id);
        break;
      case statusDict.submitted:
        Actions.showErrorDialog('Idea is yet to be approved');
        break;
      case statusDict.rejected:
        Actions.showErrorDialog('Idea was rejected');
        break;
    }
  },

	getIdeas: function(){

        var that = this;

		var ideasTrs = this.props.ideas.map((idea, index)=>{
            var imageUrl = APISERVER + "/html/ideaDoc/"+idea.id;
            var goToIdeaDetails = that.userClickOnIdea.bind(that, idea.id, index+1);
            var disabled = idea.status !== 'approved';

			return  <tr key={index}>
                <td onClick={goToIdeaDetails}><img className="idea-image" src={imageUrl}/></td>
    						<td
                  title={Config.getTitleOrNull(idea.name, 15)}
                  onClick={goToIdeaDetails}>
                    {Config.getShortName(idea.name, 15)}
                </td>
    						<td
                  title={Config.getTitleOrNull(idea.description, 20)}
                  onClick={goToIdeaDetails}>
                    {Config.getShortName(idea.description, 20)}
                </td>
    						<td onClick={goToIdeaDetails}>{idea.status}</td>
    						<td onClick={goToIdeaDetails}>{idea.votes}</td>
    						<td onClick={that.clickOnVote.bind(that, idea.id, idea.status)}>
                  <button className={disabled ? "primary-button vote disabled" : "primary-button vote"}>
                    vote
                  </button>
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
              <th width="10.666%">image</th>
							<th width="18.666%">name</th>
							<th width="22.666%">description</th>
							<th width="10.666%">status</th>
							<th width="7.666%">votes</th>
							<th
                width="22.666%"
								className="add-new-idea"
								onClick={this.addNewIdea}>add new idea
							</th>
						</tr>

					</thead>

					<tbody>

						{

							this.props.ideas.length == 0 ?
								<tr className="no-apps-announce">

									<td colSpan="6">

										<div>
											<span className="row">it's a bit lonely here</span>
											<span className="row">
												start by
												<span className="add-new-idea" onClick={this.addNewIdea}> adding a new idea</span>
											</span>
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
