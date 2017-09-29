import React from 'react';
import { observer, inject } from 'mobx-react';
import styles from './styles-sankey.scss';
import autoloader from './static/autoloader.js';
import sankeyScript from './static/sankey.js';


@observer
export default class Sankey extends React.Component{

  constructor(props){
    super(props)
  }

  componentDidMount() {
    this.drawChart();
  }

  render(){

    const {  } = this.props;

    return(
      <div className={styles.Sankey}>
        <div id="sankey_basic"></div>
      </div>
    )
  }

  drawChart(){

    var dashboardAppDataDistribution = this.props.data;
    var data = new google.visualization.DataTable();
    data.addColumn('string', 'From');
    data.addColumn('string', 'To');
    data.addColumn('number', 'Weight');
    data.addRows(this.props.data.sankeyData);

    // Sets chart options.
    var options = {
      width: 887,
      height: 400,
      sankey: {
	     node:{
				label:{
					fontName: "sans-serif",
					fontSize: 14
				}
		   }
			}
		};

    var chart = new google.visualization.Sankey(document.getElementById('sankey_basic'));
    chart.draw(data , options);

  }

}
