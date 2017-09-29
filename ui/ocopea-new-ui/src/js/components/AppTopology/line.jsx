import React from 'react';
import { observer } from 'mobx-react';

// vendors
import d3 from 'd3';


export default class Line extends React.Component {

  render(){
    this.getLine();
    return(
      <svg
        id={this.props.id || null}
        style={{left: this.props.left, width: this.props.width}}
        className={this.getClassName()}></svg>
    )
  }

  constructor(props) {
    super(props);
  }

  getLine(){
    let line = [this.props.start, this.props.end];
    let className = `.Topology__line--${this.props.className}`;
    this.drawLine(line, className);
  }

  drawLine(line, className){

    var lineFunction = d3.svg.line()
            .x(function(d){ return d.x })
            .y(function(d){ return d.y })
            .interpolate("step-before");

    d3.select(className)
    .append("path")
      .attr({
        class: "line",
        d: lineFunction(line)
      })
  }

  getClassName(){
    const additionalClass = this.props.additionalClass;
    const className = this.props.className
    const componentType = this.props.componentType;
    const baseClass = 'Topology__line';
    let c = additionalClass ?
            `${baseClass} ` +
            `${baseClass}--${className} ` +
            `${baseClass}__${additionalClass} ` +
            `${baseClass}__${additionalClass}__${componentType}` :
            `${baseClass} ${baseClass}--${className}`;
    return this.props.isSelected ? c + ` ${baseClass}--selected` : c;
  }

  componentDidMount(){
    this.getLine();
  }

};

Line.propTypes = {
  start: React.PropTypes.object.isRequired,  // <----- { x: 0, y: 0 }
  end: React.PropTypes.object.isRequired,    // <----- { x: 0, y: 0 }
  className: React.PropTypes.string,
  additionalClass: React.PropTypes.string,
  componentType: React.PropTypes.string
}
