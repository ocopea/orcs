// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import ImgRepeat from '../../../assets/images/businessSetup/repeat.png';

let Indicator = React.createClass({
   
    setClass: function(){
        
        var className;
        
        switch(this.props.data.name){
            case "business critical":
                className = "business-critical indicator";
                break;
            case "tier 2":
                className = "tier-2 indicator";
                break;
            case "custom":
                className = "custom indicator";
                break;                
        }
        return className;
    },
                        
    setImage: function(){
        if(this.props.data.name == "business critical"){
            return <img className="img-repeat" src={ImgRepeat}/>
        }
    },        
    
    setBlocks: function(){
        var blocks = [];
        for(var i=0; i<this.props.data.indicatorBlocksNumber; i++){
            if(i == this.props.data.indicatorBlocksNumber-1){
                blocks.push(<div key={i} className="block">{this.setImage()}</div>)
            }else{
                blocks.push(<div key={i} className="block"></div>)
            }
        }
        return blocks;
    },
    
    render: function(){    
        //console.log(this.setClass()) 
        return (
            <div className={this.setClass()}>
                {this.setBlocks()}
            </div>
        )        
    }
});

export default Indicator;