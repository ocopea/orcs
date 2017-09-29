import React from 'react';
import { observer, inject } from 'mobx-react';
import styles from './styles-pie.scss';
import d3 from 'd3';


export default class Pie extends React.Component{

  constructor(props){
    super(props)
  }

  componentDidMount() {
      this.init(this.props.data);
  }

  componentDidUpdate(nextProps) {
    if(this.props.data !== nextProps.data) {
      this.init(this.props.data);
    }
  }  

  static propTypes = {
  }

  render(){

    const { t } = this.props;

    return(
      <div className={styles.Quota}>
        <div className="pie"></div>
      </div>
    )
  }

  getCopiesCount() {
    let count = 0;
    this.props.data.forEach(function(element) {
      count += element.value; 
    });
    return count;
  }

  init(dataset) {
    var div = d3.select(".pie").append("div").attr("class", "toolTip");
    d3.select(".pie svg").remove();

    var copiesCount = this.getCopiesCount();    

    var width = 275,
        height = 225,
        radius = Math.min(width, height) / 2 + 25;

    var color = d3.scale.ordinal()
        .range(["#83bd5c", "#91c7e5", "#e37941"]);

    var arc = d3.svg.arc()
        .outerRadius(radius - 75)
        .innerRadius(radius - 70);

    var pie = d3.layout.pie()
        .sort(null)
      .startAngle(1.1*Math.PI)
        .endAngle(3.1*Math.PI)
        .value(function(d) { return d.value; });

    var svg = d3.select(".pie").append("svg").attr('style', 'margin-top: -20px')
        .attr("width", width)
        .attr("height", height)
      .append("g")
        .attr("transform", "translate(" + width / 2 + "," + height / 2 + ")");

    // copies count

    var g = svg.selectAll(".arc")
          .data(pie(dataset))
        .enter().append("g")
          .attr("class", "arc");

      g.append("path")
      .style("fill", function(d) { return color(d.data.name); })
        .transition().delay(function(d,i) {
      return i * 500; }).duration(500)
      .attrTween('d', function(d) {
        var i = d3.interpolate(d.startAngle+0.1, d.endAngle);
        return function(t) {
          d.endAngle = i(t); 
          return arc(d)
          }
        }); 
      g.append("text")
          .attr("transform", function(d) { return "translate(" + arc.centroid(d) + ")"; })
          .attr("dy", ".35em")
        .transition()
        .delay(1000)

      d3.selectAll("path").on("mousemove", function(d) {
          div.style("left", d3.event.pageX+10+"px");
          div.style("top", d3.event.pageY-25+"px");
          div.style("display", "inline-block");
        div.html((d.data.name)+"<br>"+(d.data.value));
    });
        
    d3.selectAll("path").on("mouseout", function(d){
        div.style("display", "none");
    });        
        
    function type(d) {
      d.value = +d.value;
      return d;
    }


    // legend

    var dataL = 0;
    var offset = 100;

    var legend = svg.selectAll('.legend')
    .data(color.domain())
    .enter()
    .append('g')
    .attr('class', 'legend')
    .attr("width", width)
    .attr("height", height)
    .attr('transform', function(d, i) {
      if (i === 0) {
        dataL = d.length + offset 
        return "translate(-100,0)"
      } else { 
        var newdataL = dataL - 80
        dataL +=  d.length + offset
        return "translate(" + (newdataL) + ",0)"
      }
    });

    legend.append('rect')
        .attr("x", 0)
        .attr("y", 91)
        .attr("width", 10)
        .attr("height", 10)
        .style("fill", function (d, i) {
        return color(i)
    })

    legend.append('text')
        .attr("x", 20)
        .attr("y", 100)
    .text(function (d, i) {
        return d
    })

    var copiesCount = this.getCopiesCount();

    g.append("text").text(copiesCount).classed('copies-count', true);

    g.append("text")
      .text(copiesCount.length === 1 ? "copy" : "copies")
      .attr({
        'y': 18,
        'class': 'copies-count'
      })

  }

}