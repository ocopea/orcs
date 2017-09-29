// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import FileReaderInput from 'react-file-reader-input';
import Actions from '../actions.js';
import Logo from '../../assets/images/logo.png';
import $ from 'jquery';

$("#submit-idea-form").submit(function(event){
	console.log(this)
})

var AddNewIdeaCard = React.createClass({

	handleImageUpload: function(e, results){
		results.forEach(result => {
			const [e, file] = result;
			Actions.uploadImage(file, e.target.result)
            this.file = file;
		});
	},

    file: {},

	setIdeaName: function(e){
		Actions.setIdeaName(e.target.value)
	},

	setIdeaDescription: function(e){
		Actions.setIdeaDescription();
	},

	submitIdea: function(e){
		e.preventDefault();

		var formData = new FormData();

		var data = {
			ideaName: e.target[0].value,
			ideaDoc: this.file,
			ideaDesc: e.target[3].value,
		}

		formData.append('ideaName', data.ideaName);
		formData.append('ideaDoc', data.ideaDoc);
		formData.append('ideaDesc', data.ideaDesc);


        if(data.ideaName != "" && data.ideaDoc.name != undefined && data.ideaDesc != ""){

            $.ajax({
                url: APISERVER+"/html/submitNewIdea",
                type: 'POST',
                data: formData,
                encType: "multipart/form-data; boundary=----WebKitFormBoundary0BPm0koKA",
                processData: false,
                contentType: false,
                success: function (returndata) {

                    Actions.goToMainScreen();

                }
            });

        }else{

            Actions.showErrorDialog("all fields are required")

        }

		return false;
	},

	componentDidMount: function(){
		Actions.resetImageName();

	},

    render: function(){

        return(

            <div className="add-new-idea">

                <div className="title">

                    <span className="logo">
											<img src={Logo} className="img-logo"/>
										</span>
                    <span>let's define our future</span>

                </div>

                <form
					className="inside"
					id="submit-idea"
					name="frmSubmitIdea"
					onSubmit={this.submitIdea}>

                    <div className="left-container">

                        <section className="idea-name-input">

                            <label className="row">inventor name</label>
                            <input
								className="row"
								type="text"
								name="ideaName"
								onChange={this.setIdeaName}/>

                        </section>

                        <section className="inventor-image">

                            <label className="row">inventor image</label>
							<FileReaderInput as="binary" id="file-input" name="ideaDoc" filename={this.props.data.imageName}
								 onChange={this.handleImageUpload}>
								  <button type="button">choose file</button>
								  <span className="image-name">{this.props.data.imageName}</span>
							</FileReaderInput>

                        </section>

                    </div>

                    <div className="right-container">

                        <label className="row">idea description</label>
                        <textarea
							className="row"
							name="ideaDesc"
							onChange={this.setIdeaDescription}/>

                    </div>

					<div className="submit-btn-container">
						<button
							type="submit"
							className="primary-button">send idea</button>
					</div>

                </form>

            </div>

        )

    }

})

export default AddNewIdeaCard;
