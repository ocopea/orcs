// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import gridIcon from '../../../assets/images/filters/grid-icon.svg';
import listIcon from '../../../assets/images/filters/list-icon.svg';
import DevActions from '../../actions/dev-actions.js';
import Isvg from 'react-inlinesvg';


var Filters = React.createClass({

    removeTag: function(options){
      var options = {
        filterType: options.filterType,
        filter: options.filter
      }
      DevActions.userClickOnFilter(options);
    },

    getFilters: function(){
      var filters = this.props.filters.map((options, i)=>{
        return  <span
                  key={i}
                  className={`tag ${options.filterType}`}>
                    <span className="tag-inside">
                      {options.filter}
                      <div className="close-btn" onClick={this.removeTag.bind(this, options)}>
                        <span></span>
                        <span></span>
                      </div>
                    </span>
                </span>
      });
      return filters;
    },

    getIconClassName: function(view){
      var currentView = this.props.view;
      return currentView == view ? "link selected" : "link"
    },

    userChangedView: function(view){
      DevActions.userChangedView(view);
    },

    render: function(){
        //console.log(this.props.sortByMenu)
        return(

            <div className="filters">

                <div className="tags">{this.getFilters()}</div>

                <section className="section-right">

                    <div
                      className="sort-by title link sort-by-menu"
                      onClick={DevActions.toggleSortByMenu.bind(this)}>

                        <span className="sort-by-menu">sort by</span>
                        {
                          !this.props.isSortByMenuRender ?
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

                    <div className="icons">
                        <span onClick={this.userChangedView.bind(this, 'grid')}>
                          <Isvg
                            src={gridIcon}
                            className={this.getIconClassName('grid')}
                            title="grid view"/>
                        </span>
                        <span onClick={this.userChangedView.bind(this, 'list')}>
                          <Isvg
                            src={listIcon}
                            className={this.getIconClassName('list')}
                            title="list view"/>
                        </span>
                    </div>

                </section>

            </div>

        )

    }

});

export default Filters;
