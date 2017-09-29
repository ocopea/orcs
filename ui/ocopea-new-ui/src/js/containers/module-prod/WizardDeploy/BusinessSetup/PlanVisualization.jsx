import React, { Component } from 'react';
import { observer } from 'mobx';
import ImgRepeat from './assets/repeat.png';
import styles from 'style!./styles-plans-visualization.css';


export default class PlanVisualization extends Component {

  constructor(props) {
    super(props)
  }

  static propTypes = {
    data: React.PropTypes.shape({
      id: React.PropTypes.number.isRequired,
      title: React.PropTypes.string
    })
  }

  render(){
      return (
          <div className={this.setClass()}>
              {this.setBlocks()}
          </div>
      )
  }

  setClass(){
    const data = this.props.data;
    const name = data ? data.name : null;
    switch(name){
        case "business critical":
            return "business-critical indicator";
            break;
        case "tier 2":
            return "tier-2 indicator";
            break;
        case "custom":
            return "custom indicator";
            break;
    }
  }

  setImage(){
      if(this.props.data.name == "business critical"){
          return <img className="img-repeat" src={ImgRepeat}/>
      }
  }

  setBlocks(){
    let blocks = [];
    const data = this.props.data;
    const blocksCount = data ? data.indicatorBlocksNumber : [];

    for(let i = 0; i < blocksCount; i++){
        if(i === blocksCount - 1){
          blocks.push(<div key={i} className="block">{this.setImage()}</div>);
        }else{
          blocks.push(<div key={i} className="block"></div>);
        }
    }
    return blocks;
  }

}
