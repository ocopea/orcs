// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import Actions from '../actions/dev-actions.js';
import $ from 'jquery';


var LeftMenuFilter = React.createClass({

    componentDidUpdate: function(nextProps){
        var filters = this.props.allFilters.map(option=>{
            return option.filter;
        });
        this.setSelectedFilters(filters);
    },

  	componentDidMount: function(){
  		this.setSelectedFilters();
  	},

    setSelectedFilters: function(filters){

      var that = this;

      var tableRows = $(".left-menu .filter table tbody tr");
      if(filters !== undefined){
        $(tableRows).each(function(index, row){
            var rowText = $(row).children()[1].textContent;
              if(filters.indexOf(rowText) !== -1){
                $(row).addClass("selected");
              }else{
                $(row).removeAttr("class");
              }
        });
      }
    },

    userSelectedFilter: function(creatorInstanceId, e){
      var filterContainer = $(e.target).closest(".filter"),
			title = $(filterContainer).children()[0],
			span = $(title).children()[0],
			type = $(span)[0].textContent,
			selectedFilter = $(e.target).closest("tr").children()[1].innerHTML;

      var options = {
          filterType: type,
          filter: selectedFilter,
          creatorInstanceId: creatorInstanceId
      };

      Actions.userClickOnFilter(options);
    },

    populateRows: function(){
      // console.log(this.props.instances)
        var rows = this.props.instances.map((instance, i)=>{

            var filter = "";

            switch(this.props.type){
                case "appType":
                    filter = instance.appTemplateName;
                    break;
                case "users":
                    var firstName = this.props.users[instance.creatorUserId].firstName,
                        lastName = this.props.users[instance.creatorUserId].lastName
                    filter = firstName + " " + lastName;
                    break;
                case "services":
                    filter = instance;
                    break;
            }

            return  <tr
                        onClick={this.userSelectedFilter.bind(this, instance.creatorUserId)}
                        key={i}>

                        <td width="20" className={this.props.type}>
                            <div className="filter-icon">
                                <div className="point"></div>
                            </div>
                        </td>
                        <td>{filter}</td>
                    </tr>
        });
        return rows;
    },

    render: function(){

        return(

                <div className="filter link">
                    <div
                        onClick={Actions.userClickOnFilterTitle.bind(this, this.props.type)}
                        className="title">

                        <span>
                                {this.props.title != undefined ?
                                this.props.title : this.props.type}
                        </span>

                        {
                            !this.props.isRender ?
                                <div className="open-button">
                                    <span></span>
                                    <span></span>
                                </div>
                            :
                                <div className="close-button">
                                    <span></span>
                                    <span></span>
                                </div>
                        }

                    </div>

                    {
                        this.props.isRender ?
                                <div>
                                    <table>
                                        <tbody>
                                            {this.populateRows()}
                                        </tbody>
                                    </table>
                                </div>
                        :
                        null
                    }

                </div>

        )

    }

});

export default LeftMenuFilter;
