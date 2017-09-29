// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import _ from 'lodash';
import $ from 'jquery';


var SortableTable = React.createClass({

  componentDidUpdate: function(nextProps){
      $(this.refs[this.props.sortBy])
        .addClass('selected')
        .siblings()
        .removeClass('selected')
  },

  getHeader: function(){
    var THs = this.props.header.map((th, i)=>{

      return  <th
                colSpan={th.colSpan !== undefined ? th.colSpan : null}
                ref={th.ref !== undefined ? th.ref : th.content}
                style={th.style}
                onClick={th.onClick}
                className={`${this.props.rootClassName}__thead__tr__th Sortable-table__thead__tr__th`}
                key={i}>
                  {th.content}
              </th>
    });
    return THs;
  },

  getBody: function(){
    var TRs = this.props.body.map((row, i)=>{

      var trClassName = this.getClassName(`${this.props.rootClassName}__tbody__tr Sortable-table__tbody__tr`,
                         this.props.body.length-1,
                         i);
      return(
        <tr
          key={i}
          className={trClassName}
          onClick={row.tr.onClick}>
          {
            _.map(row.td, (o, i)=>{
                return <td
                          colSpan={
                            o.cell.attr !== undefined &&
                            o.cell.attr.colSpan !== undefined ? o.cell.attr.colSpan : null
                          }
                          style={
                            o.cell.attr !== undefined ? o.cell.attr.style : null
                          }
                          className={`${this.props.rootClassName}__tbody__tr__td Sortable-table__tbody__tr__td`}
                          key={i}>
                            {o.cell.content}
                       </td>
            })
          }
        </tr>
      )
    });
    return TRs;
  },

  getClassName: function(className, arraySize, index){
    if(index == arraySize){
      return `${className} ${className}--last`
    }else{
      return className;
    }
  },

  render: function(){
    return(
        <table className={`${this.props.rootClassName} Sortable-table`}>
          <thead className={`${this.props.rootClassName}__thead Sortable-table__thead`}>
            <tr className={`${this.props.rootClassName}__thead__tr Sortable-table__thead__tr`}>
              {this.getHeader()}
            </tr>
          </thead>
          <tbody className={`${this.props.rootClassName}__tbody  Sortable-table__tbody`}>
            {this.getBody()}
          </tbody>
        </table>
    )
  }
});

SortableTable.propTypes = {
  header: React.PropTypes.array.isRequired,
  body: React.PropTypes.array.isRequired,
  rootClassName: React.PropTypes.string.isRequired,
  sortBy: React.PropTypes.string.isRequired
}

export default SortableTable;

// var exampleProps = {
//   header: [
//     {
//       'content':'name',
//       'onClick':function(){},
//       style: {},
//       colSpan:0
//     },
//     {'content':'owner', 'onClick':function(){}, style: {}},
//     {'content':'date-created', 'onClick':function(){}, style: {}}
//   ],
//   'tr': {},
//   'td': [
//     {
//       'cell':{
//         'content':'test',
//         'attr':{
//           'style':{'width':'10%'},
//           'colSpan':2
//         }
//       }
//     }
//   ],
//   rootClassName: 'sortable-table-test'
// }
